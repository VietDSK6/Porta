package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.data.sample.PortaSampleData
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.RuleNode

/**
 * Produces the synthetic pack that ships with Porta. It is intentionally labelled as demo data and
 * exists so imports can always be rolled back to a known-good, offline pack.
 */
class BundledOpportunityPackProvider(
    private val codec: OpportunityPackCodec,
) {
    fun rawJson(): String = codec.encode(
        OpportunityPackDto(
            schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
            id = PACK_ID,
            name = "Porta synthetic demo pack",
            publisher = "Porta",
            version = PACK_VERSION,
            sourceUrl = "https://example.invalid/porta-sample",
            lastCheckedOn = PortaSampleData.referenceDate.toString(),
            opportunities = PortaSampleData.opportunities.map(Opportunity::toDto),
        ),
    )

    companion object {
        const val PACK_ID = "porta-synthetic-demo"
        const val PACK_VERSION = 1
    }
}

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
    Deadline.Missing -> DeadlineDto.Missing
    Deadline.Tba -> DeadlineDto.Tba
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

    is RuleNode.ManualCheck -> RuleNodeDto.ManualCheck(
        id = id,
        explanation = explanation,
        sourceNote = sourceNote,
    )
}

private fun ActionTemplate.toDto(): ActionTemplateDto = ActionTemplateDto(
    id = id,
    title = title,
    targetFactKey = targetFactKey.name,
    targetValue = targetValue.toDto(),
    targetActionability = targetActionability.name,
    minimumDurationDays = minimumDurationDays,
    effort = effort.name,
    costBand = costBand.name,
    completionEvidenceTypes = completionEvidenceTypes,
    sourceNote = sourceNote,
)

private fun FactValue.toDto(): FactValueDto = when (this) {
    is FactValue.BooleanValue -> FactValueDto.BooleanValue(value)
    is FactValue.DateValue -> FactValueDto.DateValue(value.toString())
    is FactValue.DurationValue -> FactValueDto.DurationValue(amount, unit.name)
    is FactValue.EnumListValue -> FactValueDto.EnumListValue(values)
    is FactValue.EnumValue -> FactValueDto.EnumValue(value)
    is FactValue.NumberValue -> FactValueDto.NumberValue(amount, unit)
    is FactValue.TextValue -> FactValueDto.TextValue(value)
}
