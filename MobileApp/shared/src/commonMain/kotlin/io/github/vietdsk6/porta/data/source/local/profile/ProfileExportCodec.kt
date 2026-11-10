package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.DurationUnit
import io.github.vietdsk6.porta.domain.model.EvidenceAttachmentMetadata
import io.github.vietdsk6.porta.domain.model.EvidenceMetadata
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactProvenance
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleAssertion
import io.github.vietdsk6.porta.domain.model.RuleRef
import io.github.vietdsk6.porta.domain.model.displayValue
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProfileExportCodec(
    private val json: Json = DEFAULT_JSON,
) {
    fun encode(
        profile: ProfileSnapshot,
        exportedAtEpochMillis: Long,
    ): String = json.encodeToString(
        ProfileMetadataExport(
            schemaVersion = PROFILE_EXPORT_SCHEMA_VERSION,
            profileId = profile.id,
            exportedAtEpochMillis = exportedAtEpochMillis,
            facts = profile.facts.values
                .sortedBy { it.key.name }
                .map { fact ->
                    ProfileFactExport(
                        key = fact.key.name,
                        value = fact.value.displayValue(),
                        valueType = fact.value.exportType(),
                        structuredValue = fact.value.toExportValue(),
                        provenance = fact.provenance.name,
                        actionability = fact.actionability.name,
                        evidenceLabel = fact.evidence?.label,
                        evidenceReference = fact.evidence?.reference,
                        evidenceCheckedAtEpochMillis = fact.evidence?.checkedAtEpochMillis,
                        evidenceAttachmentName = fact.evidence?.attachment?.displayName,
                        evidenceAttachmentMimeType = fact.evidence?.attachment?.mimeType,
                        evidenceAttachmentSize = fact.evidence?.attachment?.byteSize,
                        updatedAtEpochMillis = fact.updatedAtEpochMillis,
                    )
                },
            ruleAssertions = profile.ruleAssertions.values
                .sortedWith(
                    compareBy(
                        { it.ruleRef.opportunityId },
                        { it.ruleRef.ruleId },
                    ),
                ).map { assertion ->
                    RuleAssertionExport(
                        opportunityId = assertion.ruleRef.opportunityId,
                        ruleId = assertion.ruleRef.ruleId,
                        ruleFingerprint = assertion.ruleFingerprint,
                        factKey = assertion.factKey.name,
                        value = assertion.value.displayValue(),
                        valueType = assertion.value.exportType(),
                        structuredValue = assertion.value.toExportValue(),
                        evidenceLabel = assertion.evidence?.label,
                        evidenceReference = assertion.evidence?.reference,
                        evidenceCheckedAtEpochMillis = assertion.evidence?.checkedAtEpochMillis,
                        evidenceAttachmentName = assertion.evidence?.attachment?.displayName,
                        evidenceAttachmentMimeType = assertion.evidence?.attachment?.mimeType,
                        evidenceAttachmentSize = assertion.evidence?.attachment?.byteSize,
                        updatedAtEpochMillis = assertion.updatedAtEpochMillis,
                    )
                },
        ),
    )

    fun decode(encoded: String): ProfileSnapshot {
        val export = json.decodeFromString<ProfileMetadataExport>(encoded)
        require(export.schemaVersion == PROFILE_EXPORT_SCHEMA_VERSION) {
            "Unsupported profile export schema ${export.schemaVersion}."
        }
        require(export.profileId.isNotBlank()) { "Exported profile ID must not be blank." }
        require(export.facts.size <= FactKey.entries.size) { "Profile export contains too many facts." }
        require(export.ruleAssertions.size <= MAX_RULE_ASSERTIONS) {
            "Profile export contains too many programme assertions."
        }
        val factKeys = export.facts.map { fact -> FactKey.valueOf(fact.key) }
        require(factKeys.distinct().size == factKeys.size) { "Profile export contains duplicate fact keys." }
        val ruleRefs = export.ruleAssertions.map { assertion ->
            RuleRef(assertion.opportunityId, assertion.ruleId)
        }
        require(ruleRefs.distinct().size == ruleRefs.size) {
            "Profile export contains duplicate programme rule references."
        }
        val facts = export.facts.map { fact ->
            val key = FactKey.valueOf(fact.key)
            ProfileFact(
                id = "${export.profileId}:${key.name.lowercase()}",
                profileId = export.profileId,
                key = key,
                value = fact.structuredValue.toDomainValue(fact.valueType),
                provenance = FactProvenance.valueOf(fact.provenance),
                actionability = Actionability.valueOf(fact.actionability),
                evidence = fact.evidence(),
                updatedAtEpochMillis = fact.updatedAtEpochMillis,
            )
        }
        val assertions = export.ruleAssertions.map { assertion ->
            val ruleRef = RuleRef(assertion.opportunityId, assertion.ruleId)
            RuleAssertion(
                profileId = export.profileId,
                ruleRef = ruleRef,
                ruleFingerprint = assertion.ruleFingerprint,
                factKey = FactKey.valueOf(assertion.factKey),
                value = assertion.structuredValue.toDomainValue(assertion.valueType),
                evidence = assertion.evidence(),
                updatedAtEpochMillis = assertion.updatedAtEpochMillis,
            )
        }
        return ProfileSnapshot(
            id = export.profileId,
            facts = facts.associateBy(ProfileFact::key),
            ruleAssertions = assertions.associateBy(RuleAssertion::ruleRef),
        )
    }

    companion object {
        private const val PROFILE_EXPORT_SCHEMA_VERSION = 2
        private const val MAX_RULE_ASSERTIONS = 1_000

        private val DEFAULT_JSON = Json {
            encodeDefaults = true
            explicitNulls = false
            prettyPrint = true
        }
    }
}

private fun FactValue.exportType(): String = when (this) {
    is FactValue.BooleanValue -> "boolean"
    is FactValue.DateValue -> "date"
    is FactValue.DurationValue -> "duration"
    is FactValue.EnumListValue -> "enum_list"
    is FactValue.EnumValue -> "enum"
    is FactValue.NumberValue -> "number"
    is FactValue.TextValue -> "text"
}

private fun FactValue.toExportValue(): FactValueExport = when (this) {
    is FactValue.BooleanValue -> FactValueExport(booleanValue = value)

    is FactValue.DateValue -> FactValueExport(textValue = value.toString())

    is FactValue.DurationValue -> FactValueExport(
        durationAmount = amount,
        durationUnit = unit.name,
    )

    is FactValue.EnumListValue -> FactValueExport(listValue = values.sorted())

    is FactValue.EnumValue -> FactValueExport(textValue = value)

    is FactValue.NumberValue -> FactValueExport(numberValue = amount, unit = unit)

    is FactValue.TextValue -> FactValueExport(textValue = value)
}

private fun FactValueExport.toDomainValue(valueType: String): FactValue = when (valueType) {
    "boolean" -> FactValue.BooleanValue(checkNotNull(booleanValue))

    "date" -> FactValue.DateValue(LocalDate.parse(checkNotNull(textValue)))

    "duration" -> FactValue.DurationValue(
        amount = checkNotNull(durationAmount).also { require(it >= 0) { "Duration must not be negative." } },
        unit = DurationUnit.valueOf(checkNotNull(durationUnit)),
    )

    "enum_list" -> FactValue.EnumListValue(listValue.toSet())

    "enum" -> FactValue.EnumValue(checkNotNull(textValue))

    "number" -> FactValue.NumberValue(
        checkNotNull(numberValue).also { require(it.isFinite()) { "Number must be finite." } },
        unit.orEmpty(),
    )

    "text" -> FactValue.TextValue(textValue.orEmpty())

    else -> error("Unsupported exported fact value type '$valueType'.")
}

private fun ProfileFactExport.evidence(): EvidenceMetadata? = evidenceLabel?.let { label ->
    EvidenceMetadata(
        label = label,
        reference = evidenceReference,
        checkedAtEpochMillis = evidenceCheckedAtEpochMillis,
        attachment = attachmentDescriptor(
            displayName = evidenceAttachmentName,
            mimeType = evidenceAttachmentMimeType,
            byteSize = evidenceAttachmentSize,
        ),
    )
}

private fun RuleAssertionExport.evidence(): EvidenceMetadata? = evidenceLabel?.let { label ->
    EvidenceMetadata(
        label = label,
        reference = evidenceReference,
        checkedAtEpochMillis = evidenceCheckedAtEpochMillis,
        attachment = attachmentDescriptor(
            displayName = evidenceAttachmentName,
            mimeType = evidenceAttachmentMimeType,
            byteSize = evidenceAttachmentSize,
        ),
    )
}

private fun attachmentDescriptor(
    displayName: String?,
    mimeType: String?,
    byteSize: Long?,
): EvidenceAttachmentMetadata? = displayName?.let { name ->
    require(name.isNotBlank()) { "Evidence attachment name must not be blank." }
    val size = checkNotNull(byteSize) { "Evidence attachment size is required when a name is present." }
    require(size >= 0L) { "Evidence attachment size must not be negative." }
    EvidenceAttachmentMetadata(
        displayName = name,
        mimeType = mimeType,
        byteSize = size,
        storageKey = null,
    )
}

@Serializable
private data class ProfileMetadataExport(
    val schemaVersion: Int,
    val warning: String = "Private profile metadata. Evidence binary files are not included. Rule assertions only apply to the exact opportunity rule and fingerprint shown.",
    val profileId: String,
    val exportedAtEpochMillis: Long,
    val facts: List<ProfileFactExport>,
    val ruleAssertions: List<RuleAssertionExport>,
)

@Serializable
private data class ProfileFactExport(
    val key: String,
    val value: String,
    val valueType: String,
    val structuredValue: FactValueExport,
    val provenance: String,
    val actionability: String,
    val evidenceLabel: String? = null,
    val evidenceReference: String? = null,
    val evidenceCheckedAtEpochMillis: Long? = null,
    val evidenceAttachmentName: String? = null,
    val evidenceAttachmentMimeType: String? = null,
    val evidenceAttachmentSize: Long? = null,
    val updatedAtEpochMillis: Long,
)

@Serializable
private data class RuleAssertionExport(
    val opportunityId: String,
    val ruleId: String,
    val ruleFingerprint: String,
    val factKey: String,
    val value: String,
    val valueType: String,
    val structuredValue: FactValueExport,
    val evidenceLabel: String? = null,
    val evidenceReference: String? = null,
    val evidenceCheckedAtEpochMillis: Long? = null,
    val evidenceAttachmentName: String? = null,
    val evidenceAttachmentMimeType: String? = null,
    val evidenceAttachmentSize: Long? = null,
    val updatedAtEpochMillis: Long,
)

@Serializable
private data class FactValueExport(
    val numberValue: Double? = null,
    val textValue: String? = null,
    val booleanValue: Boolean? = null,
    val listValue: List<String> = emptyList(),
    val unit: String? = null,
    val durationAmount: Int? = null,
    val durationUnit: String? = null,
)
