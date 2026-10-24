package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.PackDiagnostic
import io.github.vietdsk6.porta.domain.model.PackValidationResult

class OpportunityPackParser(
    private val codec: OpportunityPackCodec,
    private val validator: OpportunityPackValidator,
) {
    fun parse(
        rawJson: String,
        importedAtEpochMillis: Long = 0L,
    ): PackValidationResult = codec.decode(rawJson).fold(
        onSuccess = { pack ->
            val normalization = OpportunityPackNormalizer.normalize(pack)
            normalization.pack?.let { normalizedPack ->
                validator.validate(normalizedPack, rawJson, importedAtEpochMillis)
            } ?: PackValidationResult(diagnostics = normalization.diagnostics)
        },
        onFailure = { error ->
            PackValidationResult(
                diagnostics = listOf(
                    PackDiagnostic(
                        code = "invalid_json_or_schema",
                        message = error.message ?: "The pack could not be decoded.",
                        path = "$",
                    ),
                ),
            )
        },
    )
}
