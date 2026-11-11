package io.github.vietdsk6.porta.domain.model

data class OpportunityPackImportResult(
    val status: OpportunityPackImportStatus,
    val document: OpportunityPackDocument,
)

enum class OpportunityPackImportStatus {
    IMPORTED,
    NO_CHANGE,
}

class InvalidOpportunityPackException(
    val diagnostics: List<PackDiagnostic>,
) : IllegalArgumentException(
    diagnostics.joinToString(separator = "\n") { diagnostic ->
        "${diagnostic.path}: ${diagnostic.message}"
    },
)

class OpportunityPackVersionConflictException(
    packId: String,
    version: Int,
) : IllegalStateException(
    "Pack '$packId' version $version already exists with different content. Publish a new version.",
)
