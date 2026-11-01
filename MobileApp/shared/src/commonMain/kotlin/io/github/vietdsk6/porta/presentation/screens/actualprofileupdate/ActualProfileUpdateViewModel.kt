package io.github.vietdsk6.porta.presentation.screens.actualprofileupdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.OpportunityPackRepository
import io.github.vietdsk6.porta.data.repository.PlanRepository
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.RuleAssertionRepository
import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.PlanItem
import io.github.vietdsk6.porta.domain.model.PlanStatus
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.displayValue
import io.github.vietdsk6.porta.domain.model.findAtomicRule
import io.github.vietdsk6.porta.domain.model.fingerprint
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActualProfileUpdateViewModel(
    private val planRepository: PlanRepository,
    private val profileRepository: ProfileRepository,
    private val ruleAssertionRepository: RuleAssertionRepository,
    private val opportunityPackRepository: OpportunityPackRepository,
) : ViewModel() {
    private val formState = MutableStateFlow(FormState())

    val uiState: StateFlow<ActualProfileUpdateUiState> = combine(
        planRepository.observeCurrentPlan(),
        profileRepository.observeProfile(),
        opportunityPackRepository.observeActive(),
        formState,
    ) { plan, profile, packResult, form ->
        plan.toUiState(profile, packResult.getOrNull(), form)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ActualProfileUpdateUiState(),
    )

    init {
        viewModelScope.launch {
            planRepository.observeCurrentPlan().collect { plan ->
                if (plan != null && formState.value.planId != plan.id) {
                    formState.value = FormState.from(plan)
                }
            }
        }
    }

    fun onUiEvent(event: ActualProfileUpdateUiEvent) {
        when (event) {
            is ActualProfileUpdateUiEvent.AchievedValueChanged -> formState.update {
                it.copy(
                    achievedValue = event.value.filterValue(it.inputKind),
                    error = null,
                )
            }

            is ActualProfileUpdateUiEvent.AchievedBooleanChanged -> formState.update {
                it.copy(achievedBoolean = event.value, error = null)
            }

            is ActualProfileUpdateUiEvent.IncludeEvidenceChanged -> formState.update {
                if (uiState.value.isEvidenceRequired && !event.value) {
                    it
                } else {
                    it.copy(includeEvidence = event.value, error = null)
                }
            }

            is ActualProfileUpdateUiEvent.EvidenceLabelChanged -> formState.update {
                it.copy(evidenceLabel = event.value, evidenceLabelTouched = true, error = null)
            }

            is ActualProfileUpdateUiEvent.EvidenceReferenceChanged -> formState.update {
                it.copy(evidenceReference = event.value, error = null)
            }

            ActualProfileUpdateUiEvent.Confirm -> confirmActualValue()

            ActualProfileUpdateUiEvent.SaveHandled -> formState.update { it.copy(isSaved = false) }
        }
    }

    private fun confirmActualValue() {
        val state = uiState.value
        if (!state.canConfirm) {
            formState.update { it.copy(submitted = true) }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, error = null) }
            val plan = planRepository.observeCurrentPlan().first()
            if (
                plan == null ||
                plan.status != PlanStatus.COMPLETED ||
                formState.value.planId != plan.id
            ) {
                formState.update {
                    it.copy(
                        isSaving = false,
                        error = "This plan changed. Return to the active plan and review it again.",
                    )
                }
                return@launch
            }

            if (!plan.scopeIsCurrent(opportunityPackRepository.getActive().getOrNull())) {
                formState.update {
                    it.copy(
                        isSaving = false,
                        error = "This programme check changed. Review the current source before confirming a result.",
                    )
                }
                return@launch
            }

            val form = formState.value
            val value = form.toFactValue(plan.targetValue)
            if (value == null) {
                formState.update {
                    it.copy(isSaving = false, error = "Enter a valid achieved value.")
                }
                return@launch
            }

            val profile = profileRepository.getProfile().getOrElse {
                formState.update {
                    it.copy(
                        isSaving = false,
                        error = "Porta could not read your local profile. Please try again.",
                    )
                }
                return@launch
            }
            val timestamp = nowEpochMillis()
            val evidence = form.evidence(timestamp)
            saveConfirmedTarget(
                plan = plan,
                profile = profile,
                value = value,
                evidence = evidence,
                timestamp = timestamp,
            )
                .onSuccess {
                    formState.update { it.copy(isSaving = false, isSaved = true) }
                }
                .onFailure {
                    formState.update {
                        it.copy(
                            isSaving = false,
                            error = "Porta could not update this fact locally. Please try again.",
                        )
                    }
                }
        }
    }

    private suspend fun saveConfirmedTarget(
        plan: PlanItem,
        profile: ProfileSnapshot,
        value: FactValue,
        evidence: EvidenceMetadata?,
        timestamp: Long,
    ): Result<Unit> = when (val target = plan.target) {
        is ActionTarget.ProfileFactTarget -> {
            val existing = profile.facts[target.factKey]
            val updatedFact = existing?.copy(
                value = value,
                provenance = if (evidence == null) {
                    FactProvenance.SELF_REPORTED
                } else {
                    FactProvenance.EVIDENCE_BACKED
                },
                evidence = evidence,
                updatedAtEpochMillis = timestamp,
            ) ?: ProfileFact(
                id = "${profile.id}:${target.factKey.name.lowercase()}",
                profileId = profile.id,
                key = target.factKey,
                value = value,
                provenance = if (evidence == null) {
                    FactProvenance.SELF_REPORTED
                } else {
                    FactProvenance.EVIDENCE_BACKED
                },
                actionability = target.factKey.actionability(),
                evidence = evidence,
                updatedAtEpochMillis = timestamp,
            )
            profileRepository.saveFacts(listOf(updatedFact))
        }

        is ActionTarget.OpportunityRuleTarget -> ruleAssertionRepository.saveAssertions(
            listOf(
                RuleAssertion(
                    profileId = profile.id,
                    ruleRef = RuleRef(target.opportunityId, target.ruleId),
                    ruleFingerprint = target.ruleFingerprint,
                    factKey = target.factKey,
                    value = value,
                    evidence = evidence,
                    updatedAtEpochMillis = timestamp,
                ),
            ),
        )
    }

    private fun PlanItem?.toUiState(
        profile: ProfileSnapshot,
        pack: OpportunityPackDocument?,
        form: FormState,
    ): ActualProfileUpdateUiState {
        if (this == null) return ActualProfileUpdateUiState(isLoading = false)

        val scopedTarget = target as? ActionTarget.OpportunityRuleTarget
        val scopedOpportunity = pack?.opportunities?.firstOrNull { it.id == scopedTarget?.opportunityId }
        val scopedRule = scopedTarget?.let { scopedOpportunity?.eligibilityRule?.findAtomicRule(it.ruleId) }
        val scopeIsCurrent = scopeIsCurrent(pack)
        val currentValue = when (val actionTarget = target) {
            is ActionTarget.ProfileFactTarget -> profile.facts[actionTarget.factKey]?.value

            is ActionTarget.OpportunityRuleTarget -> profile.ruleAssertions[
                RuleRef(actionTarget.opportunityId, actionTarget.ruleId),
            ]?.takeIf { it.ruleFingerprint == actionTarget.ruleFingerprint }?.value
        }
        return ActualProfileUpdateUiState(
            isLoading = false,
            hasCompletedPlan = status == PlanStatus.COMPLETED,
            planTitle = title,
            factLabel = when (target) {
                is ActionTarget.ProfileFactTarget -> targetFactKey.label()
                is ActionTarget.OpportunityRuleTarget -> "Programme-specific ${targetFactKey.label().lowercase()}"
            },
            scopeDescription = scopedTarget?.let {
                val programme = scopedOpportunity?.title ?: "This programme"
                val requirement = scopedRule?.explanation ?: targetFactKey.label()
                "$programme · $requirement"
            },
            scopeWarning = if (scopeIsCurrent) {
                null
            } else {
                "This programme check has changed or is unavailable. Review the current source before confirming a result."
            },
            currentValueLabel = currentValue?.displayValue() ?: "Not confirmed",
            plannedTargetLabel = targetValue.displayValue(),
            inputKind = form.inputKind,
            achievedValue = form.achievedValue,
            achievedBoolean = form.achievedBoolean,
            unitLabel = (targetValue as? FactValue.NumberValue)?.unit.orEmpty(),
            includeEvidence = form.includeEvidence,
            isEvidenceRequired = target is ActionTarget.OpportunityRuleTarget,
            evidenceLabel = form.evidenceLabel,
            evidenceReference = form.evidenceReference,
            showEvidenceLabelError = (form.evidenceLabelTouched || form.submitted) &&
                form.includeEvidence && form.evidenceLabel.isBlank(),
            isSaving = form.isSaving,
            isSaved = form.isSaved,
            error = form.error,
            achievedValueError = form.validationError(targetFactKey, targetValue),
        )
    }

    private fun PlanItem.scopeIsCurrent(pack: OpportunityPackDocument?): Boolean {
        val scopedTarget = target as? ActionTarget.OpportunityRuleTarget ?: return true
        val rule = pack?.opportunities
            ?.firstOrNull { it.id == scopedTarget.opportunityId }
            ?.eligibilityRule
            ?.findAtomicRule(scopedTarget.ruleId)
            ?: return false
        return rule.factKey == scopedTarget.factKey && rule.fingerprint() == scopedTarget.ruleFingerprint
    }

    private fun FormState.toFactValue(targetValue: FactValue): FactValue? = when (targetValue) {
        is FactValue.BooleanValue -> achievedBoolean?.let { FactValue.BooleanValue(it) }
        is FactValue.NumberValue -> achievedValue.toDoubleOrNull()?.let { FactValue.NumberValue(it, targetValue.unit) }
        is FactValue.EnumValue -> achievedValue.trim().takeIf(String::isNotEmpty)?.let { FactValue.EnumValue(it) }
        is FactValue.TextValue -> achievedValue.trim().takeIf(String::isNotEmpty)?.let { FactValue.TextValue(it) }
        else -> null
    }

    private fun FormState.evidence(timestamp: Long): EvidenceMetadata? = if (includeEvidence) {
        EvidenceMetadata(
            label = evidenceLabel.trim(),
            reference = evidenceReference.trim().ifEmpty { null },
            checkedAtEpochMillis = timestamp,
        )
    } else {
        null
    }

    private fun FormState.validationError(key: FactKey, targetValue: FactValue): String? {
        if (inputKind != ActualValueInputKind.NUMBER || achievedValue.isBlank()) return null
        val number = achievedValue.toDoubleOrNull() ?: return "Enter a number"
        return when (key) {
            FactKey.GPA -> {
                val scale = (targetValue as? FactValue.NumberValue)
                    ?.unit
                    ?.removeSuffix(" scale")
                    ?.toDoubleOrNull()
                    ?: 4.0
                if (number in 0.0..scale) null else "Use the ${scale.toInputText()} GPA scale"
            }

            FactKey.IELTS_SCORE -> if (number in 0.0..9.0) null else "IELTS bands range from 0 to 9"

            FactKey.EXPERIENCE_MONTHS -> if (number in 0.0..600.0 && number % 1.0 == 0.0) {
                null
            } else {
                "Use whole months from 0 to 600"
            }

            else -> if (number >= 0.0) null else "Use zero or a positive number"
        }
    }

    private fun String.filterValue(inputKind: ActualValueInputKind): String = when (inputKind) {
        ActualValueInputKind.NUMBER -> filter { it.isDigit() || it == '.' }
            .let { value ->
                val firstDot = value.indexOf('.')
                if (firstDot == -1) value else value.take(firstDot + 1) + value.drop(firstDot + 1).replace(".", "")
            }

        ActualValueInputKind.BOOLEAN -> this

        ActualValueInputKind.TEXT -> this
    }

    private fun FactKey.label(): String = when (this) {
        FactKey.GPA -> "GPA"
        FactKey.IELTS_SCORE -> "IELTS score"
        FactKey.EXPERIENCE_MONTHS -> "Experience"
        FactKey.DEGREE_COMPLETED -> "Degree completed"
        FactKey.ENROLLED -> "Enrollment"
        FactKey.AVAILABLE_FULL_PERIOD -> "Full-period availability"
        FactKey.STUDY_LEVEL -> "Study level"
        FactKey.FIELD_OF_STUDY -> "Field of study"
        FactKey.NATIONALITY -> "Nationality"
        FactKey.RESIDENCY -> "Residency"
        FactKey.COMMUNITY_PROJECT -> "Community project"
        FactKey.LEADERSHIP -> "Leadership"
        FactKey.AGE -> "Age"
        FactKey.DATE_OF_BIRTH -> "Date of birth"
    }

    private fun FactKey.actionability(): Actionability = when (this) {
        FactKey.GPA,
        FactKey.IELTS_SCORE,
        FactKey.AVAILABLE_FULL_PERIOD,
        -> Actionability.CONTROLLABLE

        FactKey.STUDY_LEVEL,
        FactKey.ENROLLED,
        FactKey.DEGREE_COMPLETED,
        FactKey.EXPERIENCE_MONTHS,
        -> Actionability.TIME_DEPENDENT

        FactKey.AGE,
        FactKey.DATE_OF_BIRTH,
        -> Actionability.IMMUTABLE

        FactKey.FIELD_OF_STUDY,
        FactKey.NATIONALITY,
        FactKey.RESIDENCY,
        -> Actionability.CONTEXT_ONLY

        FactKey.COMMUNITY_PROJECT,
        FactKey.LEADERSHIP,
        -> Actionability.UNKNOWN
    }

    private data class FormState(
        val planId: String? = null,
        val inputKind: ActualValueInputKind = ActualValueInputKind.NUMBER,
        val achievedValue: String = "",
        val achievedBoolean: Boolean? = null,
        val includeEvidence: Boolean = false,
        val evidenceLabel: String = "",
        val evidenceReference: String = "",
        val evidenceLabelTouched: Boolean = false,
        val submitted: Boolean = false,
        val isSaving: Boolean = false,
        val isSaved: Boolean = false,
        val error: String? = null,
    ) {
        companion object {
            fun from(plan: PlanItem): FormState = FormState(
                planId = plan.id,
                inputKind = when (plan.targetValue) {
                    is FactValue.BooleanValue -> ActualValueInputKind.BOOLEAN
                    is FactValue.NumberValue -> ActualValueInputKind.NUMBER
                    else -> ActualValueInputKind.TEXT
                },
                includeEvidence = plan.target is ActionTarget.OpportunityRuleTarget,
            )
        }
    }
}

private fun Double.toInputText(): String = if (this % 1.0 == 0.0) toInt().toString() else toString()
