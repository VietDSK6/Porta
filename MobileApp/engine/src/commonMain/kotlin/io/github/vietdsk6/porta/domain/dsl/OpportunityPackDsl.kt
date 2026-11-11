package io.github.vietdsk6.porta.domain.dsl

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPack
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate

@DslMarker
annotation class PortaDsl

class PackDslException(
    val path: String,
    detail: String,
) : IllegalArgumentException("$path: $detail")

/** Builds the immutable domain document. Serialization and import timestamps belong to the data layer. */
fun opportunityPack(
    id: String,
    name: String,
    publisher: String,
    lastCheckedOn: LocalDate,
    version: Int = 1,
    schemaVersion: Int = 1,
    sourceUrl: String? = null,
    block: PackBuilder.() -> Unit,
): OpportunityPackDocument = PackBuilder(
    id = id,
    name = name,
    publisher = publisher,
    lastCheckedOn = lastCheckedOn,
    version = version,
    schemaVersion = schemaVersion,
    sourceUrl = sourceUrl,
).apply(block).build()

@PortaDsl
class PackBuilder internal constructor(
    private val id: String,
    private val name: String,
    private val publisher: String,
    private val lastCheckedOn: LocalDate,
    private val version: Int,
    private val schemaVersion: Int,
    private val sourceUrl: String?,
) {
    private val opportunities = mutableListOf<Opportunity>()
    private val opportunityIds = mutableSetOf<String>()

    fun opportunity(
        id: String,
        title: String,
        provider: String,
        category: OpportunityCategory,
        cycle: String,
        block: OpportunityBuilder.() -> Unit,
    ) {
        val path = "pack[${this.id}].opportunities[$id]"
        requireDsl(id.isNotBlank(), path, "Opportunity ID is required.")
        requireDsl(opportunityIds.add(id), path, "Duplicate opportunity ID '$id'.")
        opportunities += OpportunityBuilder(
            id = id,
            title = title,
            provider = provider,
            category = category,
            cycle = cycle,
            defaultCheckedOn = lastCheckedOn,
            defaultSchemaVersion = schemaVersion,
            path = path,
        ).apply(block).build()
    }

    internal fun build(): OpportunityPackDocument {
        val path = "pack[$id]"
        requireDsl(id.isNotBlank(), "$path.id", "Pack ID is required.")
        requireDsl(name.isNotBlank(), "$path.name", "Pack name is required.")
        requireDsl(publisher.isNotBlank(), "$path.publisher", "Publisher is required.")
        requireDsl(version > 0, "$path.version", "Version must be positive.")
        requireDsl(schemaVersion > 0, "$path.schemaVersion", "Schema version must be positive.")
        return OpportunityPackDocument(
            metadata = OpportunityPack(
                id = id,
                name = name,
                publisher = publisher,
                schemaVersion = schemaVersion,
                version = version,
                rawJson = "",
                sourceUrl = sourceUrl,
                lastCheckedOn = lastCheckedOn,
                importedAtEpochMillis = 0L,
                isActive = true,
            ),
            opportunities = opportunities.toList(),
        )
    }
}

@PortaDsl
class OpportunityBuilder internal constructor(
    private val id: String,
    private val title: String,
    private val provider: String,
    private val category: OpportunityCategory,
    private val cycle: String,
    private val defaultCheckedOn: LocalDate,
    private val defaultSchemaVersion: Int,
    private val path: String,
) {
    var cycleStartOn: LocalDate? = null
    var schemaVersion: Int = defaultSchemaVersion
    var disclaimer: String = Opportunity.DEFAULT_DISCLAIMER
    var archived: Boolean = false
    var sourceUrl: String? = null
    var sourceSection: String? = null
    var lastCheckedOn: LocalDate = defaultCheckedOn
    var deadline: Deadline? = null
    var eligibilityRule: RuleNode? = null
    val softCriteria: MutableList<String> = mutableListOf()
    val readinessItems: MutableList<String> = mutableListOf()
    val actionTemplates: MutableList<ActionTemplate> = mutableListOf()
    private val actionIds = mutableSetOf<String>()

    fun source(
        url: String,
        section: String? = null,
        checkedOn: LocalDate = lastCheckedOn,
    ) {
        requireDsl(url.isNotBlank(), "$path.source.url", "Official source URL is required.")
        sourceUrl = url
        sourceSection = section
        lastCheckedOn = checkedOn
    }

    fun source(block: SourceBuilder.() -> Unit) {
        val built = SourceBuilder(lastCheckedOn, "$path.source").apply(block)
        built.validate()
        source(built.url, built.section, built.checkedOn)
    }

    fun deadline(block: DeadlineBuilder.() -> Deadline) {
        deadline = DeadlineBuilder("$path.deadline").block()
    }

    fun eligibility(block: EligibilityBuilder.() -> Unit) {
        val built = EligibilityBuilder("$path.eligibility").apply(block)
        eligibilityRule = built.root()
        softCriteria += built.softCriteria
    }

    fun soft(text: String) {
        requireDsl(text.isNotBlank(), "$path.softCriteria", "Soft criterion cannot be blank.")
        softCriteria += text
    }

    fun readiness(text: String) {
        requireDsl(text.isNotBlank(), "$path.readinessItems", "Readiness item cannot be blank.")
        readinessItems += text
    }

    fun action(id: String, title: String, block: ActionBuilder.() -> Unit) {
        val actionPath = "$path.actions[$id]"
        requireDsl(id.isNotBlank(), actionPath, "Action ID is required.")
        requireDsl(actionIds.add(id), actionPath, "Duplicate action ID '$id'.")
        actionTemplates += ActionBuilder(id, title, actionPath).apply(block).build()
    }

    /** Allows a preexisting domain action to be included without changing its fields. */
    fun action(template: ActionTemplate) {
        val actionPath = "$path.actions[${template.id}]"
        requireDsl(template.id.isNotBlank(), actionPath, "Action ID is required.")
        requireDsl(actionIds.add(template.id), actionPath, "Duplicate action ID '${template.id}'.")
        actionTemplates += template
    }

    internal fun build(): Opportunity {
        requireDsl(title.isNotBlank(), "$path.title", "Title is required.")
        requireDsl(provider.isNotBlank(), "$path.provider", "Provider is required.")
        requireDsl(cycle.isNotBlank(), "$path.cycle", "Cycle is required.")
        val resolvedSourceUrl = sourceUrl ?: throw PackDslException("$path.source", "Source is required.")
        val resolvedDeadline = deadline ?: throw PackDslException("$path.deadline", "Deadline is required.")
        val resolvedRule = eligibilityRule ?: throw PackDslException("$path.eligibility", "Eligibility rule is required.")
        requireDsl(schemaVersion > 0, "$path.schemaVersion", "Schema version must be positive.")
        return Opportunity(
            id = id,
            title = title,
            provider = provider,
            category = category,
            cycle = cycle,
            cycleStartOn = cycleStartOn,
            sourceUrl = resolvedSourceUrl,
            sourceSection = sourceSection,
            lastCheckedOn = lastCheckedOn,
            deadline = resolvedDeadline,
            eligibilityRule = resolvedRule,
            softCriteria = softCriteria.toList(),
            readinessItems = readinessItems.toList(),
            actionTemplates = actionTemplates.toList(),
            schemaVersion = schemaVersion,
            disclaimer = disclaimer,
            archived = archived,
        )
    }
}

@PortaDsl
class SourceBuilder internal constructor(
    defaultCheckedOn: LocalDate,
    private val path: String,
) {
    var url: String = ""
    var section: String? = null
    var checkedOn: LocalDate = defaultCheckedOn

    internal fun validate() {
        requireDsl(url.isNotBlank(), "$path.url", "Official source URL is required.")
    }
}

@PortaDsl
class DeadlineBuilder internal constructor(private val path: String) {
    fun known(date: LocalDate): Deadline = Deadline.Known(date)

    fun known(isoDate: String): Deadline = try {
        Deadline.Known(LocalDate.parse(isoDate))
    } catch (exception: IllegalArgumentException) {
        throw PackDslException(path, "Invalid ISO date '$isoDate'.")
    }

    fun tba(): Deadline = Deadline.Tba

    fun missing(): Deadline = Deadline.Missing
}

@PortaDsl
class ActionBuilder internal constructor(
    private val id: String,
    private val title: String,
    private val path: String,
) {
    var target: ActionTarget? = null
    var targetActionability: Actionability = Actionability.UNKNOWN
    var minimumDurationDays: Int = 0
    var effort: EffortLevel = EffortLevel.MEDIUM
    var costBand: CostBand = CostBand.UNKNOWN
    var completionEvidenceTypes: Set<String> = emptySet()
    var sourceNote: String? = null

    fun target(predicate: FactPredicate) {
        val expected = predicate.expected
            ?: throw PackDslException("$path.target", "Action target needs an expected value.")
        target = ActionTarget.ProfileFactTarget(predicate.key, expected)
    }

    fun target(target: ActionTarget) {
        this.target = target
    }

    internal fun build(): ActionTemplate {
        requireDsl(title.isNotBlank(), "$path.title", "Action title is required.")
        requireDsl(minimumDurationDays >= 0, "$path.minimumDurationDays", "Duration cannot be negative.")
        val resolvedTarget = target ?: throw PackDslException("$path.target", "Action target is required.")
        return ActionTemplate(
            id = id,
            title = title,
            target = resolvedTarget,
            targetActionability = targetActionability,
            minimumDurationDays = minimumDurationDays,
            effort = effort,
            costBand = costBand,
            completionEvidenceTypes = completionEvidenceTypes,
            sourceNote = sourceNote,
        )
    }
}

internal fun requireDsl(condition: Boolean, path: String, detail: String) {
    if (!condition) throw PackDslException(path, detail)
}
