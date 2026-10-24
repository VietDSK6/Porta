package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.PackDiagnostic

internal data class OpportunityPackNormalizationResult(
    val pack: OpportunityPackDto? = null,
    val diagnostics: List<PackDiagnostic> = emptyList(),
)

/** Upgrades older, supported pack documents before v2 validation and domain mapping. */
internal object OpportunityPackNormalizer {
    fun normalize(pack: OpportunityPackDto): OpportunityPackNormalizationResult = when (pack.schemaVersion) {
        OpportunityPackCodec.CURRENT_SCHEMA_VERSION -> OpportunityPackNormalizationResult(pack = pack)

        1 -> upgradeV1(pack)

        else -> OpportunityPackNormalizationResult(
            diagnostics = listOf(
                PackDiagnostic(
                    code = "unsupported_schema",
                    message = "Schema ${pack.schemaVersion} cannot be upgraded.",
                    path = "schemaVersion",
                ),
            ),
        )
    }

    private fun upgradeV1(pack: OpportunityPackDto): OpportunityPackNormalizationResult {
        val diagnostics = mutableListOf<PackDiagnostic>()
        val opportunities = pack.opportunities.mapIndexed { opportunityIndex, opportunity ->
            val atomicRules = opportunity.eligibilityRule.flattenAtomics()
            val actions = opportunity.actionTemplates.mapIndexed { actionIndex, action ->
                if (action.targetFactKey != FactKey.AVAILABLE_FULL_PERIOD.name) {
                    return@mapIndexed action.copy(
                        targetScope = ActionTargetScope.PROFILE.name,
                        targetRuleId = null,
                    )
                }

                val references = atomicRules.filter { it.actionTemplateId == action.id }
                if (references.size != 1) {
                    diagnostics += PackDiagnostic(
                        code = "ambiguous_v1_availability_target",
                        message = "The v1 availability action '${action.id}' must be referenced by exactly one " +
                            "atomic rule in '${opportunity.id}' before it can be safely upgraded; found " +
                            "${references.size} references.",
                        path = "opportunities[$opportunityIndex].actionTemplates[$actionIndex]",
                    )
                    action
                } else {
                    action.copy(
                        targetScope = ActionTargetScope.RULE.name,
                        targetRuleId = references.single().id,
                    )
                }
            }
            opportunity.copy(
                schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
                actionTemplates = actions,
            )
        }

        return if (diagnostics.isEmpty()) {
            OpportunityPackNormalizationResult(
                pack = pack.copy(
                    schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
                    opportunities = opportunities,
                ),
            )
        } else {
            OpportunityPackNormalizationResult(diagnostics = diagnostics)
        }
    }
}

private fun RuleNodeDto.flattenAtomics(): List<RuleNodeDto.Atomic> = when (this) {
    is RuleNodeDto.All -> children.flatMap(RuleNodeDto::flattenAtomics)
    is RuleNodeDto.Any -> children.flatMap(RuleNodeDto::flattenAtomics)
    is RuleNodeDto.Atomic -> listOf(this)
    is RuleNodeDto.ManualCheck -> emptyList()
}
