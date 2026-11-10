package io.github.vietdsk6.porta.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.vietdsk6.porta.data.repository.ProfileRepository
import io.github.vietdsk6.porta.data.repository.SavedOpportunityRepository
import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.data.source.preferences.UserPreferences
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.util.extensions.nowEpochMillis
import io.github.vietdsk6.porta.util.logging.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OnBoardingViewModel(
    private val userPreferences: UserPreferences,
    private val eligibilityEvaluator: EligibilityEvaluator,
    private val profileRepository: ProfileRepository? = null,
    private val savedOpportunityRepository: SavedOpportunityRepository? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OnBoardingUiState(
            demoDoors = buildOnboardingDemoDoors(
                eligibilityEvaluator = eligibilityEvaluator,
                sampleIelts = SAMPLE_IELTS_BASELINE,
                selectedGoal = null,
            ),
            isLoading = true,
        ),
    )
    val uiState: StateFlow<OnBoardingUiState> = _uiState.asStateFlow()

    init {
        restoreOnboardingState()
    }

    fun onUiEvent(event: OnBoardingUiEvent) = viewModelScope.launch {
        when (event) {
            is OnBoardingUiEvent.GoalSelected -> {
                userPreferences.putString(UserPreferences.KEY_OPPORTUNITY_GOAL, event.goal.storageValue)
                _uiState.update { state ->
                    state.copy(
                        selectedGoal = event.goal,
                        demoDoors = buildOnboardingDemoDoors(
                            eligibilityEvaluator = eligibilityEvaluator,
                            sampleIelts = state.sampleIelts,
                            selectedGoal = event.goal,
                        ),
                    )
                }
            }

            OnBoardingUiEvent.Next -> _uiState.update { state ->
                val mayAdvance = when (state.currentStep) {
                    0 -> state.selectedGoal != null
                    1 -> state.isDemoComplete
                    else -> true
                }
                if (mayAdvance) {
                    state.copy(currentStep = (state.currentStep + 1).coerceAtMost(LAST_STEP))
                } else {
                    state
                }
            }

            OnBoardingUiEvent.Back -> _uiState.update { state ->
                state.copy(currentStep = (state.currentStep - 1).coerceAtLeast(0))
            }

            is OnBoardingUiEvent.SampleIeltsChanged -> _uiState.update { state ->
                val halfBandValue = (event.value * 2).roundToInt() / 2.0
                val sampleIelts = halfBandValue.coerceIn(SAMPLE_IELTS_BASELINE, SAMPLE_IELTS_TARGET)
                state.copy(
                    sampleIelts = sampleIelts,
                    demoDoors = buildOnboardingDemoDoors(
                        eligibilityEvaluator = eligibilityEvaluator,
                        sampleIelts = sampleIelts,
                        selectedGoal = state.selectedGoal,
                    ),
                )
            }

            OnBoardingUiEvent.ResetDemo -> _uiState.update { state ->
                state.copy(
                    sampleIelts = SAMPLE_IELTS_BASELINE,
                    demoDoors = buildOnboardingDemoDoors(
                        eligibilityEvaluator = eligibilityEvaluator,
                        sampleIelts = SAMPLE_IELTS_BASELINE,
                        selectedGoal = state.selectedGoal,
                    ),
                )
            }

            OnBoardingUiEvent.ReceiptToggled -> _uiState.update {
                it.copy(isReceiptExpanded = !it.isReceiptExpanded)
            }

            OnBoardingUiEvent.ExploreSample,
            OnBoardingUiEvent.SkipDemo,
            -> complete(OnboardingDestination.SAMPLE_MAP)

            OnBoardingUiEvent.UseDemoProfile -> installDemoProfile()

            OnBoardingUiEvent.BuildMyMap -> complete(OnboardingDestination.PROFILE_SETUP)
        }
    }

    fun onFinishHandled() = _uiState.update { it.copy(completion = null) }

    private suspend fun complete(destination: OnboardingDestination) {
        userPreferences.putBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN, true)
        _uiState.update {
            it.copy(
                completion = OnboardingCompletion(destination = destination, isNewUser = true),
                isInstallingDemoProfile = false,
                demoProfileError = null,
            )
        }
    }

    private suspend fun installDemoProfile() {
        _uiState.update { state ->
            state.copy(isInstallingDemoProfile = true, demoProfileError = null)
        }
        val repository = profileRepository
        if (repository == null) {
            showDemoProfileError()
            return
        }
        val currentProfile = repository.getProfile().getOrElse {
            showDemoProfileError()
            return
        }
        if (currentProfile.facts.isNotEmpty()) {
            if (PortaSampleData.isPersistedDemoProfile(currentProfile)) {
                configureJudgeDemo()
                complete(OnboardingDestination.SAMPLE_MAP)
            } else {
                _uiState.update { state ->
                    state.copy(
                        isInstallingDemoProfile = false,
                        demoProfileError = "A local profile already exists, so Porta left it unchanged.",
                    )
                }
            }
            return
        }
        repository.saveFacts(PortaSampleData.persistedDemoFacts(nowEpochMillis()))
            .onSuccess {
                configureJudgeDemo()
                complete(OnboardingDestination.SAMPLE_MAP)
            }
            .onFailure { showDemoProfileError() }
    }

    private suspend fun configureJudgeDemo() {
        runCatching {
            userPreferences.putInt(
                UserPreferences.KEY_FRONTIER_HORIZON_DAYS,
                PortaSampleData.JUDGE_DEMO_HORIZON_DAYS,
            )
            userPreferences.putBoolean(UserPreferences.KEY_FRONTIER_PRIORITIZE_SAVED, true)
            PortaSampleData.judgeDemoSavedOpportunityIds.forEach { opportunityId ->
                savedOpportunityRepository?.setSaved(opportunityId, isSaved = true)?.getOrThrow()
            }
        }.onFailure { throwable ->
            runCatching { AppLogger.e("Could not apply optional judge-demo planning preferences", throwable) }
        }
    }

    private fun showDemoProfileError() {
        _uiState.update { state ->
            state.copy(
                isInstallingDemoProfile = false,
                demoProfileError = "Porta could not create the demo profile. Your local data was not replaced.",
            )
        }
    }

    private fun restoreOnboardingState() = viewModelScope.launch {
        try {
            val selectedGoal = OnboardingGoal.fromStorageValue(
                userPreferences.getString(UserPreferences.KEY_OPPORTUNITY_GOAL),
            )?.takeIf(OnboardingGoal.selectableEntries::contains)
            if (userPreferences.getBoolean(UserPreferences.KEY_IS_ONBOARD_SHOWN)) {
                _uiState.update {
                    it.copy(
                        selectedGoal = selectedGoal,
                        demoDoors = buildOnboardingDemoDoors(
                            eligibilityEvaluator = eligibilityEvaluator,
                            sampleIelts = it.sampleIelts,
                            selectedGoal = selectedGoal,
                        ),
                        completion = OnboardingCompletion(
                            destination = OnboardingDestination.SAMPLE_MAP,
                            isNewUser = false,
                        ),
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        selectedGoal = selectedGoal,
                        demoDoors = buildOnboardingDemoDoors(
                            eligibilityEvaluator = eligibilityEvaluator,
                            sampleIelts = it.sampleIelts,
                            selectedGoal = selectedGoal,
                        ),
                        isLoading = false,
                    )
                }
            }
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            // Logging must not become a second startup failure (for example, in an isolated test
            // where the global Koin-backed logger has not been initialized yet).
            runCatching { AppLogger.e("Could not restore onboarding state", throwable) }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private companion object {
        const val LAST_STEP = 3
    }
}

internal const val SAMPLE_IELTS_BASELINE = 6.5
internal const val SAMPLE_IELTS_TARGET = 7.0

internal fun buildOnboardingDemoDoors(
    eligibilityEvaluator: EligibilityEvaluator,
    sampleIelts: Double,
    selectedGoal: OnboardingGoal?,
): List<OnboardingDemoDoorUiState> {
    val baselineProfile = PortaSampleData.profile.withOverrides(
        mapOf(FactKey.IELTS_SCORE to FactValue.NumberValue(SAMPLE_IELTS_BASELINE, "band")),
    )
    val targetProfile = baselineProfile.withOverrides(
        mapOf(FactKey.IELTS_SCORE to FactValue.NumberValue(SAMPLE_IELTS_TARGET, "band")),
    )
    val currentProfile = baselineProfile.withOverrides(
        mapOf(FactKey.IELTS_SCORE to FactValue.NumberValue(sampleIelts, "band")),
    )

    return PortaSampleData.opportunities.mapNotNull { opportunity ->
        val baselineRule = opportunity.ieltsRuleResult(eligibilityEvaluator, baselineProfile)
        val targetRule = opportunity.ieltsRuleResult(eligibilityEvaluator, targetProfile)
        if (baselineRule?.result == RuleResult.PASS || targetRule?.result != RuleResult.PASS) {
            return@mapNotNull null
        }
        val currentRule = opportunity.ieltsRuleResult(eligibilityEvaluator, currentProfile)
        val requiredIelts = (targetRule.expected as? FactValue.NumberValue)?.amount
            ?: return@mapNotNull null
        OnboardingDemoDoorUiState(
            opportunityId = opportunity.id,
            title = opportunity.title,
            category = opportunity.category,
            requiredIelts = requiredIelts,
            isRequirementMet = currentRule?.result == RuleResult.PASS,
        )
    }.sortedByDescending { door -> door.category == selectedGoal?.category }
}

private fun Opportunity.ieltsRuleResult(
    eligibilityEvaluator: EligibilityEvaluator,
    profile: io.github.vietdsk6.porta.domain.model.ProfileSnapshot,
) = eligibilityEvaluator.evaluate(
    profile = profile,
    opportunity = this,
    today = PortaSampleData.referenceDate,
).rules.firstOrNull { it.factKey == FactKey.IELTS_SCORE }
