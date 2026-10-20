package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpportunityPackDto(
    val schemaVersion: Int,
    val id: String,
    val name: String,
    val publisher: String,
    val version: Int,
    val sourceUrl: String? = null,
    val lastCheckedOn: String,
    val opportunities: List<OpportunityDto>,
)

@Serializable
data class OpportunityDto(
    val id: String,
    val title: String,
    val provider: String,
    val category: String,
    val cycle: String,
    val cycleStartOn: String? = null,
    val sourceUrl: String,
    val sourceSection: String? = null,
    val lastCheckedOn: String,
    val deadline: DeadlineDto,
    val eligibilityRule: RuleNodeDto,
    val softCriteria: List<String> = emptyList(),
    val readinessItems: List<String> = emptyList(),
    val actionTemplates: List<ActionTemplateDto> = emptyList(),
    val schemaVersion: Int = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
    val disclaimer: String = Opportunity.DEFAULT_DISCLAIMER,
    val archived: Boolean = false,
)

@Serializable
sealed interface DeadlineDto {
    @Serializable
    @SerialName("known")
    data class Known(val date: String) : DeadlineDto

    @Serializable
    @SerialName("tba")
    data object Tba : DeadlineDto

    @Serializable
    @SerialName("missing")
    data object Missing : DeadlineDto
}

@Serializable
sealed interface RuleNodeDto {
    val id: String

    @Serializable
    @SerialName("all")
    data class All(
        override val id: String,
        val children: List<RuleNodeDto>,
    ) : RuleNodeDto

    @Serializable
    @SerialName("any")
    data class Any(
        override val id: String,
        val children: List<RuleNodeDto>,
    ) : RuleNodeDto

    @Serializable
    @SerialName("atomic")
    data class Atomic(
        override val id: String,
        val factKey: String,
        val operator: String,
        val expected: FactValueDto? = null,
        val explanation: String,
        val sourceNote: String? = null,
        val actionability: String,
        val actionTemplateId: String? = null,
    ) : RuleNodeDto

    @Serializable
    @SerialName("manual_check")
    data class ManualCheck(
        override val id: String,
        val explanation: String,
        val sourceNote: String? = null,
    ) : RuleNodeDto
}

@Serializable
data class ActionTemplateDto(
    val id: String,
    val title: String,
    val targetFactKey: String,
    val targetValue: FactValueDto,
    val targetActionability: String,
    val minimumDurationDays: Int,
    val effort: String,
    val costBand: String,
    val completionEvidenceTypes: Set<String> = emptySet(),
    val sourceNote: String? = null,
)

@Serializable
sealed interface FactValueDto {
    @Serializable
    @SerialName("number")
    data class NumberValue(val amount: Double, val unit: String) : FactValueDto

    @Serializable
    @SerialName("boolean")
    data class BooleanValue(val value: Boolean) : FactValueDto

    @Serializable
    @SerialName("enum")
    data class EnumValue(val value: String) : FactValueDto

    @Serializable
    @SerialName("enum_list")
    data class EnumListValue(val values: Set<String>) : FactValueDto

    @Serializable
    @SerialName("date")
    data class DateValue(val value: String) : FactValueDto

    @Serializable
    @SerialName("duration")
    data class DurationValue(val amount: Int, val unit: String) : FactValueDto

    @Serializable
    @SerialName("text")
    data class TextValue(val value: String) : FactValueDto
}

internal fun OpportunityDto.toDomain(): Opportunity = Opportunity(
    id = id,
    title = title,
    provider = provider,
    category = OpportunityCategory.valueOf(category),
    cycle = cycle,
    cycleStartOn = cycleStartOn?.let(LocalDate::parse),
    sourceUrl = sourceUrl,
    sourceSection = sourceSection,
    lastCheckedOn = LocalDate.parse(lastCheckedOn),
    deadline = deadline.toDomain(),
    eligibilityRule = eligibilityRule.toDomain(),
    softCriteria = softCriteria,
    readinessItems = readinessItems,
    actionTemplates = actionTemplates.map(ActionTemplateDto::toDomain),
    schemaVersion = schemaVersion,
    disclaimer = disclaimer,
    archived = archived,
)

private fun DeadlineDto.toDomain(): Deadline = when (this) {
    is DeadlineDto.Known -> Deadline.Known(LocalDate.parse(date))
    DeadlineDto.Missing -> Deadline.Missing
    DeadlineDto.Tba -> Deadline.Tba
}

private fun RuleNodeDto.toDomain(): RuleNode = when (this) {
    is RuleNodeDto.All -> RuleNode.All(id, children.map(RuleNodeDto::toDomain))

    is RuleNodeDto.Any -> RuleNode.Any(id, children.map(RuleNodeDto::toDomain))

    is RuleNodeDto.Atomic -> RuleNode.Atomic(
        id = id,
        factKey = FactKey.valueOf(factKey),
        operator = Operator.valueOf(operator),
        expected = expected?.toDomain(),
        explanation = explanation,
        sourceNote = sourceNote,
        actionability = Actionability.valueOf(actionability),
        actionTemplateId = actionTemplateId,
    )

    is RuleNodeDto.ManualCheck -> RuleNode.ManualCheck(id, explanation, sourceNote)
}

private fun ActionTemplateDto.toDomain(): ActionTemplate = ActionTemplate(
    id = id,
    title = title,
    targetFactKey = FactKey.valueOf(targetFactKey),
    targetValue = targetValue.toDomain(),
    targetActionability = Actionability.valueOf(targetActionability),
    minimumDurationDays = minimumDurationDays,
    effort = EffortLevel.valueOf(effort),
    costBand = CostBand.valueOf(costBand),
    completionEvidenceTypes = completionEvidenceTypes,
    sourceNote = sourceNote,
)

private fun FactValueDto.toDomain(): FactValue = when (this) {
    is FactValueDto.BooleanValue -> FactValue.BooleanValue(value)
    is FactValueDto.DateValue -> FactValue.DateValue(LocalDate.parse(value))
    is FactValueDto.DurationValue -> FactValue.DurationValue(amount, DurationUnit.valueOf(unit))
    is FactValueDto.EnumListValue -> FactValue.EnumListValue(values)
    is FactValueDto.EnumValue -> FactValue.EnumValue(value)
    is FactValueDto.NumberValue -> FactValue.NumberValue(amount, unit)
    is FactValueDto.TextValue -> FactValue.TextValue(value)
}
