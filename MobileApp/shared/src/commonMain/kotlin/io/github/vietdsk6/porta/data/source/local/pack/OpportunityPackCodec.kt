package io.github.vietdsk6.porta.data.source.local.pack

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class OpportunityPackCodec(
    private val json: Json = DEFAULT_JSON,
) {
    fun decode(rawJson: String): Result<OpportunityPackDto> = runCatching {
        val root = json.parseToJsonElement(rawJson).jsonObject
        val schemaVersion = root["schemaVersion"]?.jsonPrimitive?.intOrNull
            ?: throw IllegalArgumentException("Pack must declare an integer schemaVersion.")
        require(schemaVersion <= CURRENT_SCHEMA_VERSION) {
            "Pack schema $schemaVersion is newer than supported schema $CURRENT_SCHEMA_VERSION."
        }
        json.decodeFromString<OpportunityPackDto>(rawJson)
    }

    fun encode(pack: OpportunityPackDto): String = json.encodeToString(pack)

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1

        private val DEFAULT_JSON = Json {
            classDiscriminator = "kind"
            encodeDefaults = true
            explicitNulls = false
            ignoreUnknownKeys = false
            prettyPrint = true
        }
    }
}
