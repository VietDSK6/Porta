package io.github.vietdsk6.porta.domain.model

data class OpportunityPackDocument(
    val metadata: OpportunityPack,
    val opportunities: List<Opportunity>,
)

data class PackValidationResult(
    val document: OpportunityPackDocument? = null,
    val diagnostics: List<PackDiagnostic> = emptyList(),
) {
    val isValid: Boolean
        get() = document != null && diagnostics.none { it.severity == PackDiagnosticSeverity.ERROR }
}

data class PackDiagnostic(
    val code: String,
    val message: String,
    val path: String,
    val severity: PackDiagnosticSeverity = PackDiagnosticSeverity.ERROR,
)

enum class PackDiagnosticSeverity {
    ERROR,
    WARNING,
}
