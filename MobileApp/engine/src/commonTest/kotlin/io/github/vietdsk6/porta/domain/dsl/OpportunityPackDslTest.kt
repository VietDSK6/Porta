package io.github.vietdsk6.porta.domain.dsl

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OpportunityPackDslTest {
    private val reviewedOn = LocalDate.parse("2026-11-02")

    @Test
    fun buildsTypedOpportunityWithEveryNestedPart() {
        val document = opportunityPack(
            id = "porta-curated",
            name = "Porta curated pack",
            publisher = "Porta",
            lastCheckedOn = reviewedOn,
            version = 3,
            sourceUrl = "https://example.org/pack",
        ) {
            opportunity(
                id = "example",
                title = "Example scholarship",
                provider = "University",
                category = OpportunityCategory.SCHOLARSHIP,
                cycle = "2027 intake",
            ) {
                cycleStartOn = LocalDate.parse("2027-09-01")
                source("https://example.org/rules", "Eligibility", reviewedOn)
                deadline { known("2027-01-15") }
                eligibility {
                    all("root") {
                        atomic(
                            "language",
                            fact(IeltsScore) atLeast 7.0.band,
                            "Minimum IELTS band",
                            actionability = Actionability.CONTROLLABLE,
                            actionTemplateId = "improve-ielts",
                        )
                        any("degree-options") {
                            atomic("degree", fact(FirstDegree).isTrue(), "First degree")
                            manual("equivalent", "Provider must confirm equivalence")
                        }
                    }
                    soft("Research experience is preferred")
                }
                readiness("Prepare transcript")
                action("improve-ielts", "Improve IELTS") {
                    target(fact(IeltsScore) atLeast 7.0.band)
                    targetActionability = Actionability.CONTROLLABLE
                    minimumDurationDays = 60
                    effort = EffortLevel.HIGH
                    costBand = CostBand.MEDIUM
                    completionEvidenceTypes = setOf("test-score")
                }
            }
        }

        assertEquals(3, document.metadata.version)
        assertEquals("https://example.org/pack", document.metadata.sourceUrl)
        val opportunity = document.opportunities.single()
        assertEquals(Deadline.Known(LocalDate.parse("2027-01-15")), opportunity.deadline)
        assertEquals(listOf("Research experience is preferred"), opportunity.softCriteria)
        assertEquals(listOf("Prepare transcript"), opportunity.readinessItems)
        val root = assertIs<RuleNode.All>(opportunity.eligibilityRule)
        val language = assertIs<RuleNode.Atomic>(root.children.first())
        assertEquals(FactKey.IELTS_SCORE, language.factKey)
        assertEquals(Operator.GREATER_THAN_OR_EQUAL, language.operator)
        assertEquals(FactValue.NumberValue(7.0, "band"), language.expected)
        val action = opportunity.actionTemplates.single()
        assertEquals("improve-ielts", action.id)
        assertEquals(
            ActionTarget.ProfileFactTarget(FactKey.IELTS_SCORE, FactValue.NumberValue(7.0, "band")),
            action.target,
        )
    }

    @Test
    fun examplesHaveCorrectTypedDomainValues() {
        assertEquals(
            FactPredicate(FactKey.NATIONALITY, Operator.IN_SET, FactValue.EnumListValue(setOf("VN", "SG"))),
            fact(Nationality) oneOf listOf("VN", "SG"),
        )
        assertEquals(
            FactPredicate(FactKey.EXPERIENCE_MONTHS, Operator.GREATER_THAN_OR_EQUAL, FactValue.NumberValue(6.0, "months")),
            fact(Experience) atLeast 6.months,
        )
        assertEquals(
            FactPredicate(FactKey.DEGREE_COMPLETED, Operator.EQUALS, FactValue.BooleanValue(true)),
            fact(FirstDegree).isTrue(),
        )
    }

    @Test
    fun duplicateOpportunityIdFailsAtSecondDeclarationWithPath() {
        val failure = assertFailsWith<PackDslException> {
            opportunityPack("pack", "Pack", "Porta", reviewedOn) {
                opportunity("same", "One", "Provider", OpportunityCategory.FELLOWSHIP, "2027") {
                    source("https://example.org")
                    deadline { tba() }
                    eligibility { manual("rule", "Check source") }
                }
                opportunity("same", "Two", "Provider", OpportunityCategory.FELLOWSHIP, "2027") {
                    error("Second opportunity body must not run")
                }
            }
        }
        assertEquals("pack[pack].opportunities[same]", failure.path)
        assertTrue(failure.message.orEmpty().contains("Duplicate opportunity ID"))
    }

    @Test
    fun duplicateRuleIdAcrossBranchesHasPrecisePath() {
        val failure = assertFailsWith<PackDslException> {
            sampleDocument {
                eligibility {
                    all("root") {
                        atomic("same", fact(FirstDegree).isTrue(), "A degree")
                        any("alternatives") {
                            manual("same", "An alternative")
                        }
                    }
                }
            }
        }
        assertEquals(
            "pack[pack].opportunities[door].eligibility.all[root].any[alternatives].manual[same]",
            failure.path,
        )
    }

    @Test
    fun emptyAllAndAnyFailImmediately() {
        for (kind in listOf("all", "any")) {
            val failure = assertFailsWith<PackDslException> {
                sampleDocument {
                    eligibility {
                        if (kind == "all") all("empty") {} else any("empty") {}
                        error("Empty group should fail before the next statement")
                    }
                }
            }
            assertTrue(failure.path.endsWith(".$kind[empty]"))
            assertTrue(failure.message.orEmpty().contains("cannot be empty"))
        }
    }

    @Test
    fun duplicateActionIdAndMissingRequiredPartHavePaths() {
        val duplicate = assertFailsWith<PackDslException> {
            sampleDocument {
                action("same", "First") { target(fact(FirstDegree).isTrue()) }
                action("same", "Second") { error("Duplicate action body must not run") }
            }
        }
        assertEquals("pack[pack].opportunities[door].actions[same]", duplicate.path)

        val missing = assertFailsWith<PackDslException> {
            opportunityPack("pack", "Pack", "Porta", reviewedOn) {
                opportunity("door", "Door", "Provider", OpportunityCategory.SCHOLARSHIP, "2027") {
                    source("https://example.org")
                    eligibility { manual("check", "Check the rules") }
                }
            }
        }
        assertEquals("pack[pack].opportunities[door].deadline", missing.path)
    }

    @Test
    fun importedRuleTreeIsReplayedAndValidated() {
        val original = RuleNode.All(
            "root",
            listOf(
                RuleNode.Atomic(
                    id = "degree",
                    factKey = FactKey.DEGREE_COMPLETED,
                    operator = Operator.EQUALS,
                    expected = FactValue.BooleanValue(true),
                    explanation = "A degree is required",
                    actionability = Actionability.TIME_DEPENDENT,
                ),
                RuleNode.ManualCheck("other", "Confirm with provider"),
            ),
        )
        val document = opportunityPack("pack", "Pack", "Porta", reviewedOn) {
            opportunity("door", "Door", "Provider", OpportunityCategory.SCHOLARSHIP, "2027") {
                source {
                    url = "https://example.org"
                    section = "Eligibility"
                }
                deadline { tba() }
                eligibility { rule(original) }
            }
        }
        assertEquals(original, document.opportunities.single().eligibilityRule)

        val invalid = assertFailsWith<PackDslException> {
            sampleDocument { eligibility { rule(RuleNode.Any("empty", emptyList())) } }
        }
        assertEquals("pack[pack].opportunities[door].eligibility.any[empty]", invalid.path)
    }

    private fun sampleDocument(block: OpportunityBuilder.() -> Unit) {
        opportunityPack("pack", "Pack", "Porta", reviewedOn) {
            opportunity("door", "Door", "Provider", OpportunityCategory.SCHOLARSHIP, "2027") {
                source("https://example.org")
                deadline { tba() }
                eligibility { manual("default", "Check source") }
                block()
            }
        }
    }
}
