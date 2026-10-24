package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPack
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.PackDiagnostic
import io.github.vietdsk6.porta.domain.model.PackValidationResult
import kotlinx.datetime.LocalDate

class OpportunityPackValidator {
    fun validate(
        pack: OpportunityPackDto,
        rawJson: String,
        importedAtEpochMillis: Long = 0L,
    ): PackValidationResult {
        val diagnostics = mutableListOf<PackDiagnostic>()
        fun error(code: String, message: String, path: String) {
            diagnostics += PackDiagnostic(code = code, message = message, path = path)
        }

        if (pack.schemaVersion != OpportunityPackCodec.CURRENT_SCHEMA_VERSION) {
            error(
                code = "unsupported_schema",
                message = "Schema ${pack.schemaVersion} is not supported.",
                path = "schemaVersion",
            )
        }
        requireText(pack.id, "missing_pack_id", "Pack ID is required.", "id", ::error)
        requireText(pack.name, "missing_pack_name", "Pack name is required.", "name", ::error)
        requireText(pack.publisher, "missing_publisher", "Publisher is required.", "publisher", ::error)
        if (pack.version < 1) {
            error("invalid_pack_version", "Pack version must be at least 1.", "version")
        }
        val packCheckedDate = parseDate(pack.lastCheckedOn)
        if (packCheckedDate == null) {
            error("invalid_checked_date", "Pack lastCheckedOn must be an ISO date.", "lastCheckedOn")
        }

        pack.opportunities
            .groupBy(OpportunityDto::id)
            .filterValues { it.size > 1 }
            .keys
            .forEach { duplicateId ->
                error(
                    "duplicate_opportunity_id",
                    "Opportunity ID '$duplicateId' appears more than once.",
                    "opportunities",
                )
            }

        pack.opportunities
            .flatMap { opportunity ->
                opportunity.actionTemplates.map { action ->
                    action.id to opportunity.id
                }
            }
            .groupBy { (actionId) -> actionId }
            .filterValues { occurrences -> occurrences.map { (_, opportunityId) -> opportunityId }.distinct().size > 1 }
            .forEach { (actionId, occurrences) ->
                val opportunityIds = occurrences
                    .map { (_, opportunityId) -> opportunityId }
                    .distinct()
                    .joinToString()
                error(
                    code = "duplicate_action_id_across_pack",
                    message = "Action ID '$actionId' is reused across opportunities: $opportunityIds.",
                    path = "opportunities",
                )
            }

        pack.opportunities.forEachIndexed { index, opportunity ->
            validateOpportunity(opportunity, index, ::error)
        }

        if (diagnostics.isNotEmpty() || packCheckedDate == null) {
            return PackValidationResult(diagnostics = diagnostics)
        }

        return PackValidationResult(
            document = OpportunityPackDocument(
                metadata = OpportunityPack(
                    id = pack.id,
                    name = pack.name,
                    publisher = pack.publisher,
                    schemaVersion = pack.schemaVersion,
                    version = pack.version,
                    rawJson = rawJson,
                    sourceUrl = pack.sourceUrl,
                    lastCheckedOn = packCheckedDate,
                    importedAtEpochMillis = importedAtEpochMillis,
                    isActive = true,
                ),
                opportunities = pack.opportunities.map(OpportunityDto::toDomain),
            ),
        )
    }

    private fun validateOpportunity(
        opportunity: OpportunityDto,
        index: Int,
        error: ErrorSink,
    ) {
        val path = "opportunities[$index]"
        requireText(opportunity.id, "missing_opportunity_id", "Opportunity ID is required.", "$path.id", error)
        requireText(opportunity.title, "missing_title", "Title is required.", "$path.title", error)
        requireText(opportunity.provider, "missing_provider", "Provider is required.", "$path.provider", error)
        requireText(opportunity.cycle, "missing_cycle", "Cycle is required.", "$path.cycle", error)
        requireText(opportunity.sourceUrl, "missing_source", "Official source URL is required.", "$path.sourceUrl", error)
        if (!opportunity.sourceUrl.startsWith("https://")) {
            error("invalid_source", "Official source URL must use HTTPS.", "$path.sourceUrl")
        }
        if (!isEnumValue<OpportunityCategory>(opportunity.category)) {
            error("unknown_category", "Unknown opportunity category '${opportunity.category}'.", "$path.category")
        }
        if (opportunity.schemaVersion != OpportunityPackCodec.CURRENT_SCHEMA_VERSION) {
            error(
                "unsupported_opportunity_schema",
                "Opportunity schema ${opportunity.schemaVersion} is not supported.",
                "$path.schemaVersion",
            )
        }
        if (parseDate(opportunity.lastCheckedOn) == null) {
            error("invalid_checked_date", "lastCheckedOn must be an ISO date.", "$path.lastCheckedOn")
        }
        val cycleStart = opportunity.cycleStartOn?.let(::parseDate)
        if (opportunity.cycleStartOn != null && cycleStart == null) {
            error("invalid_cycle_start", "cycleStartOn must be an ISO date.", "$path.cycleStartOn")
        }
        val deadline = when (val value = opportunity.deadline) {
            is DeadlineDto.Known -> parseDate(value.date).also {
                if (it == null) {
                    error("invalid_deadline", "Deadline must be an ISO date.", "$path.deadline.date")
                }
            }

            DeadlineDto.Missing -> null

            DeadlineDto.Tba -> null
        }
        if (deadline != null && cycleStart != null && deadline < cycleStart && !opportunity.archived) {
            error(
                "deadline_before_cycle",
                "Deadline precedes the cycle start; mark the record archived or correct the dates.",
                "$path.deadline",
            )
        }

        val actionsById = opportunity.actionTemplates.groupBy(ActionTemplateDto::id)
        actionsById.filterValues { it.size > 1 }.keys.forEach { duplicateId ->
            error(
                "duplicate_action_id",
                "Action ID '$duplicateId' appears more than once in this opportunity.",
                "$path.actionTemplates",
            )
        }
        opportunity.actionTemplates.forEachIndexed { actionIndex, action ->
            validateAction(action, "$path.actionTemplates[$actionIndex]", error)
        }

        val ruleIds = mutableSetOf<String>()
        validateRule(
            rule = opportunity.eligibilityRule,
            path = "$path.eligibilityRule",
            actionIds = actionsById.keys,
            seenRuleIds = ruleIds,
            error = error,
        )
        val atomicRules = opportunity.eligibilityRule.flattenAtomics()
        opportunity.actionTemplates.forEachIndexed { actionIndex, action ->
            validateActionTarget(
                action = action,
                atomicRules = atomicRules,
                path = "$path.actionTemplates[$actionIndex]",
                error = error,
            )
        }
    }

    private fun validateAction(action: ActionTemplateDto, path: String, error: ErrorSink) {
        requireText(action.id, "missing_action_id", "Action ID is required.", "$path.id", error)
        requireText(action.title, "missing_action_title", "Action title is required.", "$path.title", error)
        if (!isEnumValue<FactKey>(action.targetFactKey)) {
            error("unknown_fact_key", "Unknown target fact '${action.targetFactKey}'.", "$path.targetFactKey")
        }
        val actionability = enumValueOrNull<Actionability>(action.targetActionability)
        if (actionability == null) {
            error(
                "unknown_actionability",
                "Unknown actionability '${action.targetActionability}'.",
                "$path.targetActionability",
            )
        } else if (actionability !in ALLOWED_ACTION_TARGETS) {
            error(
                "unsafe_action_target",
                "Actions cannot target ${actionability.name.lowercase()} facts.",
                "$path.targetActionability",
            )
        }
        val targetScope = enumValueOrNull<ActionTargetScope>(action.targetScope)
        if (targetScope == null) {
            error("unknown_action_scope", "Unknown action target scope '${action.targetScope}'.", "$path.targetScope")
        } else {
            when (targetScope) {
                ActionTargetScope.PROFILE -> if (action.targetRuleId != null) {
                    error(
                        "unexpected_target_rule",
                        "Profile actions cannot declare targetRuleId.",
                        "$path.targetRuleId",
                    )
                }

                ActionTargetScope.RULE -> if (action.targetRuleId.isNullOrBlank()) {
                    error(
                        "missing_target_rule",
                        "Rule-scoped actions must declare targetRuleId.",
                        "$path.targetRuleId",
                    )
                }
            }
        }
        if (action.minimumDurationDays < 0) {
            error("invalid_duration", "Minimum duration cannot be negative.", "$path.minimumDurationDays")
        }
        if (!isEnumValue<EffortLevel>(action.effort)) {
            error("unknown_effort", "Unknown effort '${action.effort}'.", "$path.effort")
        }
        if (!isEnumValue<CostBand>(action.costBand)) {
            error("unknown_cost", "Unknown cost band '${action.costBand}'.", "$path.costBand")
        }
        validateFactValue(action.targetValue, "$path.targetValue", error)
    }

    private fun validateActionTarget(
        action: ActionTemplateDto,
        atomicRules: List<RuleNodeDto.Atomic>,
        path: String,
        error: ErrorSink,
    ) {
        val scope = enumValueOrNull<ActionTargetScope>(action.targetScope) ?: return
        val references = atomicRules.filter { it.actionTemplateId == action.id }
        if (references.isEmpty()) {
            error(
                "unreferenced_action",
                "Action '${action.id}' is not referenced by an atomic rule.",
                "$path.id",
            )
            return
        }

        val targetRules = when (scope) {
            ActionTargetScope.PROFILE -> references

            ActionTargetScope.RULE -> {
                val targetRuleId = action.targetRuleId ?: return
                val targetRule = atomicRules.firstOrNull { it.id == targetRuleId }
                if (targetRule == null) {
                    error(
                        "unknown_target_rule",
                        "Rule-scoped action '${action.id}' targets missing atomic rule '$targetRuleId'.",
                        "$path.targetRuleId",
                    )
                    return
                }
                if (targetRule.actionTemplateId != action.id) {
                    error(
                        "target_rule_action_mismatch",
                        "Target rule '$targetRuleId' does not reference action '${action.id}'.",
                        "$path.targetRuleId",
                    )
                }
                if (references.any { it.id != targetRuleId }) {
                    error(
                        "ambiguous_rule_action_reference",
                        "Rule-scoped action '${action.id}' may only be referenced by its target rule '$targetRuleId'.",
                        "$path.targetRuleId",
                    )
                }
                listOf(targetRule)
            }
        }

        targetRules.forEach { rule ->
            if (rule.factKey != action.targetFactKey) {
                error(
                    "target_fact_mismatch",
                    "Action '${action.id}' targets ${action.targetFactKey}, but rule '${rule.id}' uses ${rule.factKey}.",
                    "$path.targetFactKey",
                )
            }
            if (rule.actionability != action.targetActionability) {
                error(
                    "target_actionability_mismatch",
                    "Action '${action.id}' and rule '${rule.id}' must use the same actionability.",
                    "$path.targetActionability",
                )
            }
            val operator = enumValueOrNull<Operator>(rule.operator)
            if (operator != null && !targetSatisfiesRule(action.targetValue, operator, rule.expected)) {
                error(
                    "incompatible_target_value",
                    "Action '${action.id}' does not provide a value that satisfies rule '${rule.id}'.",
                    "$path.targetValue",
                )
            }
        }
    }

    private fun validateRule(
        rule: RuleNodeDto,
        path: String,
        actionIds: Set<String>,
        seenRuleIds: MutableSet<String>,
        error: ErrorSink,
    ) {
        if (rule.id.isBlank()) {
            error("missing_rule_id", "Rule ID is required.", "$path.id")
        } else if (!seenRuleIds.add(rule.id)) {
            error("duplicate_rule_id", "Rule ID '${rule.id}' is duplicated.", "$path.id")
        }
        when (rule) {
            is RuleNodeDto.All -> validateChildren(rule.children, path, actionIds, seenRuleIds, error)

            is RuleNodeDto.Any -> validateChildren(rule.children, path, actionIds, seenRuleIds, error)

            is RuleNodeDto.Atomic -> {
                if (!isEnumValue<FactKey>(rule.factKey)) {
                    error("unknown_fact_key", "Unknown fact '${rule.factKey}'.", "$path.factKey")
                }
                val operator = enumValueOrNull<Operator>(rule.operator)
                if (operator == null) {
                    error("unknown_operator", "Unknown operator '${rule.operator}'.", "$path.operator")
                }
                if (!isEnumValue<Actionability>(rule.actionability)) {
                    error(
                        "unknown_actionability",
                        "Unknown actionability '${rule.actionability}'.",
                        "$path.actionability",
                    )
                }
                if (operator != Operator.EXISTS && rule.expected == null) {
                    error("missing_expected", "This atomic rule requires an expected value.", "$path.expected")
                }
                rule.expected?.let { validateFactValue(it, "$path.expected", error) }
                if (rule.explanation.isBlank()) {
                    error("missing_explanation", "Rule explanation is required.", "$path.explanation")
                }
                if (rule.sourceNote.isNullOrBlank()) {
                    error("missing_source_note", "Hard rules require a source note.", "$path.sourceNote")
                }
                rule.actionTemplateId?.let { actionId ->
                    if (actionId !in actionIds) {
                        error(
                            "unknown_action_reference",
                            "Rule references missing action '$actionId'.",
                            "$path.actionTemplateId",
                        )
                    }
                }
            }

            is RuleNodeDto.ManualCheck -> {
                if (rule.explanation.isBlank()) {
                    error("missing_explanation", "Manual-check explanation is required.", "$path.explanation")
                }
            }
        }
    }

    private fun validateChildren(
        children: List<RuleNodeDto>,
        path: String,
        actionIds: Set<String>,
        seenRuleIds: MutableSet<String>,
        error: ErrorSink,
    ) {
        if (children.isEmpty()) {
            error("empty_rule_group", "ALL/ANY rule groups cannot be empty.", "$path.children")
        }
        children.forEachIndexed { index, child ->
            validateRule(child, "$path.children[$index]", actionIds, seenRuleIds, error)
        }
    }

    private fun validateFactValue(value: FactValueDto, path: String, error: ErrorSink) {
        when (value) {
            is FactValueDto.DateValue -> if (parseDate(value.value) == null) {
                error("invalid_date_value", "Fact date must be an ISO date.", "$path.value")
            }

            is FactValueDto.DurationValue -> {
                if (value.amount < 0) {
                    error("invalid_duration_value", "Duration cannot be negative.", "$path.amount")
                }
                if (!isEnumValue<DurationUnit>(value.unit)) {
                    error("unknown_duration_unit", "Unknown duration unit '${value.unit}'.", "$path.unit")
                }
            }

            is FactValueDto.EnumListValue -> if (value.values.isEmpty()) {
                error("empty_enum_list", "Enum-list values cannot be empty.", "$path.values")
            }

            is FactValueDto.EnumValue -> if (value.value.isBlank()) {
                error("empty_enum_value", "Enum value cannot be blank.", "$path.value")
            }

            is FactValueDto.NumberValue -> if (value.unit.isBlank()) {
                error("missing_numeric_unit", "Numeric facts require a unit or scale.", "$path.unit")
            }

            is FactValueDto.TextValue -> if (value.value.isBlank()) {
                error("empty_text_value", "Text value cannot be blank.", "$path.value")
            }

            is FactValueDto.BooleanValue -> Unit
        }
    }

    private fun targetSatisfiesRule(
        actual: FactValueDto,
        operator: Operator,
        expected: FactValueDto?,
    ): Boolean = when (operator) {
        Operator.EXISTS -> true

        Operator.EQUALS -> actual == expected

        Operator.NOT_EQUALS -> expected != null && actual::class == expected::class && actual != expected

        Operator.GREATER_THAN_OR_EQUAL -> when {
            actual is FactValueDto.NumberValue && expected is FactValueDto.NumberValue ->
                actual.unit == expected.unit && actual.amount >= expected.amount

            actual is FactValueDto.DurationValue && expected is FactValueDto.DurationValue ->
                actual.unit == expected.unit && actual.amount >= expected.amount

            else -> false
        }

        Operator.LESS_THAN_OR_EQUAL -> when {
            actual is FactValueDto.NumberValue && expected is FactValueDto.NumberValue ->
                actual.unit == expected.unit && actual.amount <= expected.amount

            actual is FactValueDto.DurationValue && expected is FactValueDto.DurationValue ->
                actual.unit == expected.unit && actual.amount <= expected.amount

            else -> false
        }

        Operator.IN_SET -> expected is FactValueDto.EnumListValue && when (actual) {
            is FactValueDto.EnumValue -> actual.value in expected.values
            is FactValueDto.TextValue -> actual.value in expected.values
            else -> false
        }

        Operator.CONTAINS_ANY ->
            actual is FactValueDto.EnumListValue &&
                expected is FactValueDto.EnumListValue &&
                actual.values.any { value -> value in expected.values }

        Operator.CONTAINS_ALL ->
            actual is FactValueDto.EnumListValue &&
                expected is FactValueDto.EnumListValue &&
                actual.values.containsAll(expected.values)

        Operator.DATE_BEFORE ->
            actual is FactValueDto.DateValue &&
                expected is FactValueDto.DateValue &&
                parseDate(actual.value)?.let { actualDate ->
                    parseDate(expected.value)?.let { expectedDate -> actualDate < expectedDate }
                } == true

        Operator.DATE_AFTER ->
            actual is FactValueDto.DateValue &&
                expected is FactValueDto.DateValue &&
                parseDate(actual.value)?.let { actualDate ->
                    parseDate(expected.value)?.let { expectedDate -> actualDate > expectedDate }
                } == true
    }

    private fun requireText(
        value: String,
        code: String,
        message: String,
        path: String,
        error: ErrorSink,
    ) {
        if (value.isBlank()) error(code, message, path)
    }

    private fun parseDate(value: String): LocalDate? = try {
        LocalDate.parse(value)
    } catch (_: IllegalArgumentException) {
        null
    }

    private inline fun <reified T : Enum<T>> isEnumValue(value: String): Boolean = enumValueOrNull<T>(value) != null

    private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? = enumValues<T>().firstOrNull { it.name == value }

    private companion object {
        val ALLOWED_ACTION_TARGETS = setOf(Actionability.CONTROLLABLE, Actionability.TIME_DEPENDENT)
    }
}

private typealias ErrorSink = (code: String, message: String, path: String) -> Unit

private fun RuleNodeDto.flattenAtomics(): List<RuleNodeDto.Atomic> = when (this) {
    is RuleNodeDto.All -> children.flatMap(RuleNodeDto::flattenAtomics)
    is RuleNodeDto.Any -> children.flatMap(RuleNodeDto::flattenAtomics)
    is RuleNodeDto.Atomic -> listOf(this)
    is RuleNodeDto.ManualCheck -> emptyList()
}
