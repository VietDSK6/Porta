package io.github.vietdsk6.porta.presentation.screens.opportunitydetail

import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleReceipt
import io.github.vietdsk6.porta.domain.model.RuleResult

/** A single applicable path through ALL/ANY rules, for a concise receipt. Alternatives stay inspectable. */
data class PublishedCheckPath(
    val ruleIds: Set<String> = emptySet(),
    val passed: Int = 0,
    val failed: Int = 0,
    val unknown: Int = 0,
    val review: Int = 0,
) {
    val hasOnlyManualRemaining: Boolean get() = passed > 0 && failed == 0 && unknown == 0 && review > 0

    operator fun plus(other: PublishedCheckPath): PublishedCheckPath = PublishedCheckPath(
        ruleIds = ruleIds + other.ruleIds,
        passed = passed + other.passed,
        failed = failed + other.failed,
        unknown = unknown + other.unknown,
        review = review + other.review,
    )
}

internal fun publishedCheckPath(rule: RuleNode, receipts: List<RuleReceipt>): PublishedCheckPath {
    val byId = receipts.associateBy(RuleReceipt::ruleId)
    fun visit(node: RuleNode): PublishedCheckPath = when (node) {
        is RuleNode.All -> node.children.fold(PublishedCheckPath()) { path, child -> path + visit(child) }

        is RuleNode.Any -> node.children.map(::visit).minWithOrNull(
            compareBy<PublishedCheckPath> { it.failed + it.unknown + it.review * 2 }
                .thenBy { it.failed }
                .thenByDescending { it.passed }
                .thenBy { it.ruleIds.sorted().joinToString() },
        ) ?: PublishedCheckPath()

        is RuleNode.Atomic, is RuleNode.ManualCheck -> {
            val result = byId[node.id]?.result ?: RuleResult.UNKNOWN
            PublishedCheckPath(
                ruleIds = setOf(node.id),
                passed = if (result == RuleResult.PASS) 1 else 0,
                failed = if (result == RuleResult.FAIL) 1 else 0,
                unknown = if (result == RuleResult.UNKNOWN) 1 else 0,
                review = if (result == RuleResult.REVIEW) 1 else 0,
            )
        }
    }
    return visit(rule)
}
