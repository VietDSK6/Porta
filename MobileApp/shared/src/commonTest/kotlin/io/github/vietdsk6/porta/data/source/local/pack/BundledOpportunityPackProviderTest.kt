package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.ActionTarget
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.EligibilityState
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.PlanningConstraints
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.RuleResult
import io.github.vietdsk6.porta.domain.model.asScenarioOverrides
import io.github.vietdsk6.porta.domain.model.resolvedHardGapOpportunityIds
import io.github.vietdsk6.porta.domain.usecase.ActionFrontierEngine
import io.github.vietdsk6.porta.domain.usecase.EligibilityEvaluator
import io.github.vietdsk6.porta.domain.usecase.ScenarioEvaluator
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BundledOpportunityPackProviderTest {
    @Test
    fun `bundled catalog is a valid sourced fifteen-record pack`() {
        val codec = OpportunityPackCodec()
        val result = OpportunityPackParser(codec, OpportunityPackValidator()).parse(
            rawJson = BundledOpportunityPackProvider(codec).rawJson(),
            importedAtEpochMillis = 42L,
        )

        assertTrue(result.isValid, result.diagnostics.joinToString())
        val document = assertNotNull(result.document)
        val opportunities = document.opportunities
        assertEquals(BundledOpportunityPackProvider.PACK_ID, document.metadata.id)
        assertEquals(3, document.metadata.version)
        assertEquals(PortaContestPackData.REVIEWED_ON, document.metadata.lastCheckedOn.toString())
        assertEquals(15, opportunities.size)
        assertTrue(opportunities.all { it.eligibilityRule.id.isNotBlank() })
        assertTrue(opportunities.all { it.sourceUrl.startsWith("https://") })
        assertTrue(
            opportunities.all {
                it.lastCheckedOn.toString() in setOf(
                    PortaContestPackData.REVIEWED_ON,
                    PortaContestPackData.SOURCE_REFRESH_REVIEWED_ON,
                )
            },
        )
        assertEquals(
            setOf(
                "erasmus-mundus-joint-masters",
                "chevening-scholarship-2027",
                "schwarzman-scholars-2028-2029",
                "daad-epos-2027-2028",
                "jj-wbgsp-2027-window-1",
                "oecd-internship-rolling",
                "unicef-internship-rolling",
            ),
            opportunities
                .filter { it.lastCheckedOn.toString() == PortaContestPackData.SOURCE_REFRESH_REVIEWED_ON }
                .mapTo(mutableSetOf()) { it.id },
        )
        assertFalse(opportunities.any { "example.invalid" in it.sourceUrl })
        assertTrue(opportunities.count { it.softCriteria.isNotEmpty() } >= 3)
        assertTrue(opportunities.sumOf { it.actionTemplates.size } >= 5)
        assertTrue(opportunities.flatMap { it.eligibilityRule.flatten() }.count { it is RuleNode.ManualCheck } >= 3)
        assertTrue(
            opportunities
                .flatMap { it.eligibilityRule.flatten() }
                .filterIsInstance<RuleNode.Atomic>()
                .count { it.actionability == Actionability.IMMUTABLE } >= 3,
        )

        val sharedProfileActionSignatures = opportunities
            .flatMap { it.actionTemplates }
            .filter { it.target is ActionTarget.ProfileFactTarget }
            .groupBy { "${it.targetFactKey}:${it.targetValue}" }
            .count { (_, actions) -> actions.size > 1 }
        assertTrue(sharedProfileActionSignatures >= 2)

        val availabilityTargets = opportunities
            .flatMap { it.actionTemplates }
            .mapNotNull { it.target as? ActionTarget.OpportunityRuleTarget }
            .filter { it.factKey == FactKey.AVAILABLE_FULL_PERIOD }
        val availabilityActions = opportunities
            .flatMap { it.actionTemplates }
            .filter { it.targetFactKey == FactKey.AVAILABLE_FULL_PERIOD }
        assertEquals(4, availabilityTargets.size)
        assertEquals(4, availabilityTargets.map { it.opportunityId to it.ruleId }.distinct().size)
        assertTrue(availabilityTargets.all { it.ruleFingerprint.isNotBlank() })
        assertEquals(4, availabilityActions.map { it.title }.distinct().size)
        assertTrue(availabilityActions.all { it.title.startsWith("Confirm full-period availability for ") })
        assertTrue(availabilityActions.all { "14-day confirmation lead time is a Porta assumption" in it.sourceNote.orEmpty() })
    }

    @Test
    fun `new bundled version refreshes only older bundled catalogs`() {
        val provider = BundledOpportunityPackProvider(OpportunityPackCodec())

        assertTrue(provider.shouldInstallOver(activePackId = null, activeVersion = null))
        assertTrue(provider.shouldInstallOver(BundledOpportunityPackProvider.LEGACY_SYNTHETIC_PACK_ID, 1))
        assertTrue(provider.shouldInstallOver(BundledOpportunityPackProvider.PACK_ID, 2))
        assertFalse(provider.shouldInstallOver(BundledOpportunityPackProvider.PACK_ID, 3))
        assertFalse(provider.shouldInstallOver("student-imported-catalog", 1))
    }

    @Test
    fun `schwarzman age checks use date of birth at the published reference date`() {
        val opportunity = bundledOpportunities().first { it.id == "schwarzman-scholars-2028-2029" }
        val evaluator = EligibilityEvaluator()

        fun result(dateOfBirth: LocalDate, ruleId: String): RuleResult = evaluator
            .evaluate(
                profile = profile(FactKey.DATE_OF_BIRTH to FactValue.DateValue(dateOfBirth)),
                opportunity = opportunity,
                today = LocalDate(2026, 10, 26),
            ).rules.single { it.ruleId == ruleId }.result

        assertEquals(
            RuleResult.PASS,
            result(LocalDate(2010, 8, 1), "schwarzman-age-min"),
        )
        assertEquals(
            RuleResult.FAIL,
            result(LocalDate(2010, 8, 2), "schwarzman-age-min"),
        )
        assertEquals(
            RuleResult.FAIL,
            result(LocalDate(1999, 8, 1), "schwarzman-age-max"),
        )
        assertEquals(
            RuleResult.PASS,
            result(LocalDate(1999, 8, 2), "schwarzman-age-max"),
        )
    }

    @Test
    fun `schwarzman IELTS is one accepted route and cannot make the whole programme open`() {
        val opportunity = bundledOpportunities().first { it.id == "schwarzman-scholars-2028-2029" }
        val evaluator = EligibilityEvaluator()
        val commonFacts = arrayOf(
            FactKey.DATE_OF_BIRTH to FactValue.DateValue(LocalDate(2006, 4, 12)),
            FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(true),
        )
        val below = evaluator.evaluate(
            profile = profile(*commonFacts, FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band")),
            opportunity = opportunity,
            today = LocalDate(2026, 11, 2),
        )
        val atThreshold = evaluator.evaluate(
            profile = profile(*commonFacts, FactKey.IELTS_SCORE to FactValue.NumberValue(7.0, "band")),
            opportunity = opportunity,
            today = LocalDate(2026, 11, 2),
        )

        assertEquals(RuleResult.FAIL, below.rules.single { it.ruleId == "schwarzman-ielts-seven" }.result)
        assertTrue(below.rules.any { it.ruleId == "schwarzman-other-english-route" && it.result == RuleResult.REVIEW })
        assertEquals(RuleResult.PASS, atThreshold.rules.single { it.ruleId == "schwarzman-ielts-seven" }.result)
        assertEquals(RuleResult.REVIEW, atThreshold.rootResult)
        assertEquals(EligibilityState.NEEDS_INFO, below.state)
        assertEquals(EligibilityState.NEEDS_INFO, atThreshold.state)
        assertTrue(atThreshold.rules.any { it.ruleId == "schwarzman-english-evidence" && it.result == RuleResult.REVIEW })
        assertTrue(atThreshold.rules.any { it.ruleId == "schwarzman-passport-route" && it.result == RuleResult.REVIEW })

        val futureGraduate = evaluator.evaluate(
            profile = profile(*commonFacts, FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false)),
            opportunity = opportunity,
            today = LocalDate(2026, 11, 2),
        )
        assertEquals(RuleResult.FAIL, futureGraduate.rules.single { it.ruleId == "schwarzman-degree" }.result)
        assertTrue(futureGraduate.rules.any { it.ruleId == "schwarzman-degree-before-start" && it.result == RuleResult.REVIEW })
        assertEquals(EligibilityState.NEEDS_INFO, futureGraduate.state)
    }

    @Test
    fun `closed Chevening cycle and 2027 World Bank country caution remain visible`() {
        val opportunities = bundledOpportunities()
        val evaluator = EligibilityEvaluator()
        val chevening = opportunities.first { it.id == "chevening-scholarship-2027" }
        assertTrue(chevening.archived)
        assertEquals(
            EligibilityState.CLOSED,
            evaluator.evaluate(profile(), chevening, LocalDate(2026, 11, 2)).state,
        )

        val worldBank = opportunities.first { it.id == "jj-wbgsp-2027-window-1" }
        val countryCheck = worldBank.eligibilityRule.flatten()
            .filterIsInstance<RuleNode.ManualCheck>()
            .single { it.id == "jj-country-admission" }
        assertTrue("Viet Nam is absent" in countryCheck.explanation)
        assertTrue("countries-eligible-for-jjwbgsp-scholarship" in countryCheck.sourceNote.orEmpty())
        val receipt = evaluator.evaluate(
            profile = profile(
                FactKey.NATIONALITY to FactValue.EnumValue("VN"),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(true),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(36.0, "months"),
            ),
            opportunity = worldBank,
            today = LocalDate(2026, 11, 2),
        )
        assertEquals(EligibilityState.NEEDS_INFO, receipt.state)
        assertTrue(receipt.rules.any { it.ruleId == "jj-country-admission" && it.result == RuleResult.REVIEW })
    }

    @Test
    fun `every bundled record yields sourced receipts for contrasting profiles`() {
        val codec = OpportunityPackCodec()
        val document = assertNotNull(
            OpportunityPackParser(codec, OpportunityPackValidator())
                .parse(BundledOpportunityPackProvider(codec).rawJson())
                .document,
        )
        val profiles = listOf(
            profile(
                FactKey.AGE to FactValue.NumberValue(20.0, "years"),
                FactKey.DATE_OF_BIRTH to FactValue.DateValue(LocalDate(2006, 4, 12)),
                FactKey.ENROLLED to FactValue.BooleanValue(true),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            ),
            profile(
                FactKey.AGE to FactValue.NumberValue(30.0, "years"),
                FactKey.DATE_OF_BIRTH to FactValue.DateValue(LocalDate(1990, 4, 12)),
                FactKey.ENROLLED to FactValue.BooleanValue(false),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(true),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(true),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(36.0, "months"),
            ),
        )
        val states = profiles.flatMap { profile ->
            document.opportunities.map { opportunity ->
                EligibilityEvaluator()
                    .evaluate(profile, opportunity, LocalDate(2026, 10, 21))
                    .also { receipt ->
                        assertTrue(receipt.rules.isNotEmpty(), opportunity.id)
                        assertTrue(receipt.rules.all { !it.sourceNote.isNullOrBlank() }, opportunity.id)
                    }
                    .state
            }
        }.toSet()

        assertTrue(EligibilityState.NEEDS_INFO in states)
        assertTrue(EligibilityState.HAS_GAPS in states)
        assertTrue(EligibilityState.NOT_ACTIONABLE in states)
        assertTrue(EligibilityState.CLOSED in states)
    }

    @Test
    fun `bundled catalog produces a multi-door frontier from non-language facts`() {
        val codec = OpportunityPackCodec()
        val opportunities = assertNotNull(
            OpportunityPackParser(codec, OpportunityPackValidator())
                .parse(BundledOpportunityPackProvider(codec).rawJson())
                .document,
        ).opportunities
        val eligibilityEvaluator = EligibilityEvaluator()
        val frontier = ActionFrontierEngine(
            eligibilityEvaluator = eligibilityEvaluator,
            scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator),
        ).build(
            profile = profile(
                FactKey.AGE to FactValue.NumberValue(20.0, "years"),
                FactKey.ENROLLED to FactValue.BooleanValue(true),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
                FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            ),
            opportunities = opportunities,
            savedOpportunityIds = setOf("oecd-internship-rolling", "cern-openlab-summer-student-2027"),
            constraints = PlanningConstraints(
                horizonDays = 90,
                maxEffort = EffortLevel.VERY_HIGH,
                maxCost = CostBand.UNKNOWN,
                prioritizeSaved = false,
            ),
            today = LocalDate(2026, 10, 21),
        )

        val availability = frontier.filter { action ->
            action.template.target is ActionTarget.OpportunityRuleTarget &&
                action.template.targetFactKey == FactKey.AVAILABLE_FULL_PERIOD
        }
        assertEquals(4, availability.size)
        availability.forEach { action ->
            val target = assertNotNull(action.template.target as? ActionTarget.OpportunityRuleTarget)
            assertEquals(listOf(target.opportunityId), action.improvedOpportunityIds)
            assertEquals(listOf(target.opportunityId), action.timingUnverifiedOpportunityIds)
            assertTrue(action.newlyOpenOpportunityIds.isEmpty())
        }
        assertEquals(2, availability.sumOf { it.improvedSavedOpportunityIds.size })
    }

    @Test
    fun `first degree is a sourced three-door current-cycle action without a false open claim`() {
        val eligibilityEvaluator = EligibilityEvaluator()
        val frontier = ActionFrontierEngine(
            eligibilityEvaluator = eligibilityEvaluator,
            scenarioEvaluator = ScenarioEvaluator(eligibilityEvaluator),
        ).build(
            profile = profile(
                FactKey.AGE to FactValue.NumberValue(23.0, "years"),
                FactKey.DATE_OF_BIRTH to FactValue.DateValue(LocalDate(2003, 5, 12)),
                FactKey.ENROLLED to FactValue.BooleanValue(true),
                FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
                FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
                FactKey.IELTS_SCORE to FactValue.NumberValue(6.5, "band"),
                FactKey.NATIONALITY to FactValue.EnumValue("VN"),
            ),
            opportunities = bundledOpportunities(),
            savedOpportunityIds = emptySet(),
            constraints = PlanningConstraints(
                horizonDays = 365,
                maxEffort = EffortLevel.VERY_HIGH,
                maxCost = CostBand.UNKNOWN,
                prioritizeSaved = false,
            ),
            today = LocalDate(2026, 11, 2),
        )

        val degreeMove = assertNotNull(frontier.singleOrNull { it.template.targetFactKey == FactKey.DEGREE_COMPLETED })
        assertEquals(
            listOf(
                "daad-epos-2027-2028",
                "erasmus-mundus-joint-masters",
                "schwarzman-scholars-2028-2029",
            ),
            degreeMove.improvedOpportunityIds,
        )
        assertTrue(degreeMove.newlyOpenOpportunityIds.isEmpty())
        assertFalse("chevening-scholarship-2027" in degreeMove.improvedOpportunityIds)
        assertFalse("jj-wbgsp-2027-window-1" in degreeMove.improvedOpportunityIds)
    }

    @Test
    fun `real vertical slice exposes exact sourced progress and remaining blockers`() {
        val opportunities = bundledOpportunities()
        val evaluator = ScenarioEvaluator(EligibilityEvaluator())
        val baseline = profile(
            FactKey.AGE to FactValue.NumberValue(24.0, "years"),
            FactKey.ENROLLED to FactValue.BooleanValue(true),
            FactKey.DEGREE_COMPLETED to FactValue.BooleanValue(false),
            FactKey.EXPERIENCE_MONTHS to FactValue.NumberValue(6.0, "months"),
            FactKey.AVAILABLE_FULL_PERIOD to FactValue.BooleanValue(false),
        )

        val degreeAction = opportunities
            .first { it.id == "erasmus-mundus-joint-masters" }
            .actionTemplates
            .single()
        val degreeScenario = evaluator.evaluate(
            profile = baseline,
            overrides = degreeAction.target.asScenarioOverrides(),
            opportunities = opportunities,
            today = LocalDate(2026, 10, 25),
        )
        val degreeImpacts = degreeScenario.impacts.associateBy { it.opportunityId }
        listOf(
            "erasmus-mundus-joint-masters" to "erasmus-degree-complete",
            "daad-epos-2027-2028" to "daad-degree",
            "jj-wbgsp-2027-window-1" to "jj-degree",
            "schwarzman-scholars-2028-2029" to "schwarzman-degree",
        ).forEach { (opportunityId, ruleId) ->
            val impact = assertNotNull(degreeImpacts[opportunityId])
            val delta = assertNotNull(impact.changedRules.singleOrNull { it.ruleId == ruleId })
            assertEquals(RuleResult.FAIL, delta.beforeResult)
            assertEquals(RuleResult.PASS, delta.afterResult)
            assertTrue(delta.sourceNote?.contains(PortaContestPackData.SOURCE_REFRESH_REVIEWED_ON) == true)
            assertTrue(impact.afterState != EligibilityState.OPEN)
        }
        assertTrue(
            degreeImpacts
                .getValue("erasmus-mundus-joint-masters")
                .remainingRules
                .isEmpty(),
            "The final-year alternative must not remain a blocker once the completed-degree branch passes.",
        )
        listOf("daad-epos-2027-2028", "jj-wbgsp-2027-window-1").forEach { opportunityId ->
            assertTrue(degreeImpacts.getValue(opportunityId).remainingRules.isNotEmpty())
        }

        val experienceAction = opportunities
            .first { it.id == "jj-wbgsp-2027-window-1" }
            .actionTemplates
            .first { it.targetFactKey == FactKey.EXPERIENCE_MONTHS }
        val experienceScenario = evaluator.evaluate(
            profile = baseline,
            overrides = experienceAction.target.asScenarioOverrides(),
            opportunities = opportunities,
            today = LocalDate(2026, 10, 25),
        )
        assertEquals(
            setOf("daad-epos-2027-2028", "jj-wbgsp-2027-window-1"),
            experienceScenario
                .resolvedHardGapOpportunityIds(experienceAction.target)
                .toSet(),
        )

        val oecdAction = opportunities
            .first { it.id == "oecd-internship-rolling" }
            .actionTemplates
            .first { it.targetFactKey == FactKey.AVAILABLE_FULL_PERIOD }
        val oecdScenario = evaluator.evaluate(
            profile = baseline,
            overrides = oecdAction.target.asScenarioOverrides(),
            opportunities = opportunities,
            today = LocalDate(2026, 10, 25),
        )
        assertEquals(
            listOf("oecd-internship-rolling"),
            oecdScenario.impacts.map { it.opportunityId },
        )
        assertTrue(oecdScenario.impacts.single().remainingRules.any { it.result == RuleResult.REVIEW })
    }

    private fun bundledOpportunities() = assertNotNull(
        OpportunityPackParser(OpportunityPackCodec(), OpportunityPackValidator())
            .parse(BundledOpportunityPackProvider(OpportunityPackCodec()).rawJson())
            .document,
    ).opportunities
}

private fun profile(vararg values: Pair<FactKey, FactValue>): ProfileSnapshot = ProfileSnapshot(
    facts = values.associate { (key, value) ->
        key to ProfileFact(
            id = key.name.lowercase(),
            key = key,
            value = value,
            actionability = when (key) {
                FactKey.AGE -> Actionability.IMMUTABLE

                FactKey.ENROLLED,
                FactKey.DEGREE_COMPLETED,
                FactKey.EXPERIENCE_MONTHS,
                -> Actionability.TIME_DEPENDENT

                FactKey.AVAILABLE_FULL_PERIOD -> Actionability.CONTROLLABLE

                else -> Actionability.UNKNOWN
            },
        )
    },
)

private fun RuleNode.flatten(): List<RuleNode> = when (this) {
    is RuleNode.All -> listOf(this) + children.flatMap(RuleNode::flatten)

    is RuleNode.Any -> listOf(this) + children.flatMap(RuleNode::flatten)

    is RuleNode.Atomic,
    is RuleNode.ManualCheck,
    -> listOf(this)
}
