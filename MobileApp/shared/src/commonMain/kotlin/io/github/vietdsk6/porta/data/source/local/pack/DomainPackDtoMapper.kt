package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.RuleNode

/** Maps a typed DSL document to the stable import format used by local storage and exports. */
internal fun OpportunityPackDocument.toDto(): OpportunityPackDto = OpportunityPackDto(
    schemaVersion = metadata.schemaVersion,
    id = metadata.id,
    name = metadata.name,
    publisher = metadata.publisher,
    version = metadata.version,
    sourceUrl = metadata.sourceUrl,
    lastCheckedOn = metadata.lastCheckedOn.toString(),
    opportunities = opportunities.map(Opportunity::toDto),
)

private fun Opportunity.toDto(): OpportunityDto = OpportunityDto(
    id = id,
    title = title,
    provider = provider,
    category = category.name,
    cycle = cycle,
    cycleStartOn = cycleStartOn?.toString(),
    sourceUrl = sourceUrl,
    sourceSection = sourceSection,
    lastCheckedOn = lastCheckedOn.toString(),
    deadline = deadline.toDto(),
    eligibilityRule = eligibilityRule.toDto(),
    softCriteria = softCriteria,
    readinessItems = readinessItems,
    actionTemplates = actionTemplates.map(ActionTemplate::toDto),
    schemaVersion = schemaVersion,
    disclaimer = disclaimer,
    archived = archived,
)

private fun Deadline.toDto(): DeadlineDto = when (this) {
    is Deadline.Known -> DeadlineDto.Known(date.toString())
    Deadline.Tba -> DeadlineDto.Tba
    Deadline.Missing -> DeadlineDto.Missing
}

private fun RuleNode.toDto(): RuleNodeDto = when (this) {
    is RuleNode.All -> RuleNodeDto.All(id, children.map(RuleNode::toDto))

    is RuleNode.Any -> RuleNodeDto.Any(id, children.map(RuleNode::toDto))

    is RuleNode.Atomic -> RuleNodeDto.Atomic(
        id = id,
        factKey = factKey.name,
        operator = operator.name,
        expected = expected?.toDto(),
        explanation = explanation,
        sourceNote = sourceNote,
        actionability = actionability.name,
        actionTemplateId = actionTemplateId,
    )

    is RuleNode.ManualCheck -> RuleNodeDto.ManualCheck(id, explanation, sourceNote)
}

private fun ActionTemplate.toDto(): ActionTemplateDto = ActionTemplateDto(
    id = id,
    title = title,
    targetFactKey = target.factKey.name,
    targetValue = target.value.toDto(),
    targetScope = when (target) {
        is ActionTarget.ProfileFactTarget -> ActionTargetScope.PROFILE.name
        is ActionTarget.OpportunityRuleTarget -> ActionTargetScope.RULE.name
    },
    targetRuleId = (target as? ActionTarget.OpportunityRuleTarget)?.ruleId,
    targetActionability = targetActionability.name,
    minimumDurationDays = minimumDurationDays,
    effort = effort.name,
    costBand = costBand.name,
    completionEvidenceTypes = completionEvidenceTypes,
    sourceNote = sourceNote,
)

private fun FactValue.toDto(): FactValueDto = when (this) {
    is FactValue.NumberValue -> FactValueDto.NumberValue(amount, unit)
    is FactValue.BooleanValue -> FactValueDto.BooleanValue(value)
    is FactValue.EnumValue -> FactValueDto.EnumValue(value)
    is FactValue.EnumListValue -> FactValueDto.EnumListValue(values)
    is FactValue.DateValue -> FactValueDto.DateValue(value.toString())
    is FactValue.DurationValue -> FactValueDto.DurationValue(amount, unit.name)
    is FactValue.TextValue -> FactValueDto.TextValue(value)
}
