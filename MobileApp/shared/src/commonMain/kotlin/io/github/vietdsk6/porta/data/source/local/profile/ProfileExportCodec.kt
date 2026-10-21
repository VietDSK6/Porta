package io.github.vietdsk6.porta.data.source.local.profile

import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.displayValue
import kotlinx.serialization.Serializable
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
            profileId = profile.id,
            exportedAtEpochMillis = exportedAtEpochMillis,
            facts = profile.facts.values
                .sortedBy { it.key.name }
                .map { fact ->
                    ProfileFactExport(
                        key = fact.key.name,
                        value = fact.value.displayValue(),
                        valueType = fact.value.exportType(),
                        provenance = fact.provenance.name,
                        actionability = fact.actionability.name,
                        evidenceLabel = fact.evidence?.label,
                        evidenceReference = fact.evidence?.reference,
                        evidenceCheckedAtEpochMillis = fact.evidence?.checkedAtEpochMillis,
                        updatedAtEpochMillis = fact.updatedAtEpochMillis,
                    )
                },
        ),
    )

    companion object {
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

@Serializable
private data class ProfileMetadataExport(
    val schemaVersion: Int = 1,
    val warning: String = "Private profile metadata. Evidence binary files are not included.",
    val profileId: String,
    val exportedAtEpochMillis: Long,
    val facts: List<ProfileFactExport>,
)

@Serializable
private data class ProfileFactExport(
    val key: String,
    val value: String,
    val valueType: String,
    val provenance: String,
    val actionability: String,
    val evidenceLabel: String? = null,
    val evidenceReference: String? = null,
    val evidenceCheckedAtEpochMillis: Long? = null,
    val updatedAtEpochMillis: Long,
)
