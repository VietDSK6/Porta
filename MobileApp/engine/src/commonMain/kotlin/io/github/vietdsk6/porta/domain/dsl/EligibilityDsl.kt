package io.github.vietdsk6.porta.domain.dsl

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.RuleNode

@PortaDsl
class EligibilityBuilder internal constructor(path: String) {
    private val ruleIds = mutableSetOf<String>()
    private val scope = RuleScope(path, ruleIds)
    internal val softCriteria = mutableListOf<String>()

    fun all(id: String, block: RuleScope.() -> Unit) = scope.all(id, block)

    fun any(id: String, block: RuleScope.() -> Unit) = scope.any(id, block)

    fun atomic(
        id: String,
        predicate: FactPredicate,
        explanation: String,
        sourceNote: String? = null,
        actionability: Actionability = Actionability.UNKNOWN,
        actionTemplateId: String? = null,
    ) = scope.atomic(id, predicate, explanation, sourceNote, actionability, actionTemplateId)

    fun atomic(
        id: String,
        factKey: FactKey,
        operator: Operator,
        expected: FactValue?,
        explanation: String,
        sourceNote: String? = null,
        actionability: Actionability = Actionability.UNKNOWN,
        actionTemplateId: String? = null,
    ) = scope.atomic(id, FactPredicate(factKey, operator, expected), explanation, sourceNote, actionability, actionTemplateId)

    fun manual(id: String, explanation: String, sourceNote: String? = null) = scope.manual(id, explanation, sourceNote)

    /** Replays an existing domain tree through the same duplicate and empty-group checks. */
    fun rule(node: RuleNode) = scope.rule(node)

    fun soft(text: String) {
        requireDsl(text.isNotBlank(), "${scope.path}.soft", "Soft criterion cannot be blank.")
        softCriteria += text
    }

    internal fun root(): RuleNode {
        requireDsl(scope.nodes.size == 1, scope.path, "Eligibility needs exactly one root rule.")
        return scope.nodes.single()
    }
}

@PortaDsl
class RuleScope internal constructor(
    internal val path: String,
    private val ruleIds: MutableSet<String>,
) {
    internal val nodes = mutableListOf<RuleNode>()

    fun all(id: String, block: RuleScope.() -> Unit) {
        val childPath = claim(id, "all")
        val child = RuleScope(childPath, ruleIds).apply(block)
        requireDsl(child.nodes.isNotEmpty(), childPath, "ALL cannot be empty.")
        nodes += RuleNode.All(id, child.nodes.toList())
    }

    fun any(id: String, block: RuleScope.() -> Unit) {
        val childPath = claim(id, "any")
        val child = RuleScope(childPath, ruleIds).apply(block)
        requireDsl(child.nodes.isNotEmpty(), childPath, "ANY cannot be empty.")
        nodes += RuleNode.Any(id, child.nodes.toList())
    }

    fun atomic(
        id: String,
        predicate: FactPredicate,
        explanation: String,
        sourceNote: String? = null,
        actionability: Actionability = Actionability.UNKNOWN,
        actionTemplateId: String? = null,
    ) {
        val nodePath = claim(id, "atomic")
        requireDsl(explanation.isNotBlank(), "$nodePath.explanation", "Explanation is required.")
        nodes += RuleNode.Atomic(
            id = id,
            factKey = predicate.key,
            operator = predicate.operator,
            expected = predicate.expected,
            explanation = explanation,
            sourceNote = sourceNote,
            actionability = actionability,
            actionTemplateId = actionTemplateId,
        )
    }

    fun atomic(
        id: String,
        factKey: FactKey,
        operator: Operator,
        expected: FactValue?,
        explanation: String,
        sourceNote: String? = null,
        actionability: Actionability = Actionability.UNKNOWN,
        actionTemplateId: String? = null,
    ) = atomic(id, FactPredicate(factKey, operator, expected), explanation, sourceNote, actionability, actionTemplateId)

    fun manual(id: String, explanation: String, sourceNote: String? = null) {
        val nodePath = claim(id, "manual")
        requireDsl(explanation.isNotBlank(), "$nodePath.explanation", "Explanation is required.")
        nodes += RuleNode.ManualCheck(id, explanation, sourceNote)
    }

    fun rule(node: RuleNode) {
        when (node) {
            is RuleNode.All -> all(node.id) { node.children.forEach(::rule) }

            is RuleNode.Any -> any(node.id) { node.children.forEach(::rule) }

            is RuleNode.Atomic -> atomic(
                id = node.id,
                factKey = node.factKey,
                operator = node.operator,
                expected = node.expected,
                explanation = node.explanation,
                sourceNote = node.sourceNote,
                actionability = node.actionability,
                actionTemplateId = node.actionTemplateId,
            )

            is RuleNode.ManualCheck -> manual(node.id, node.explanation, node.sourceNote)
        }
    }

    private fun claim(id: String, kind: String): String {
        val nodePath = "$path.$kind[$id]"
        requireDsl(id.isNotBlank(), nodePath, "Rule ID is required.")
        requireDsl(ruleIds.add(id), nodePath, "Duplicate rule ID '$id'.")
        return nodePath
    }
}
