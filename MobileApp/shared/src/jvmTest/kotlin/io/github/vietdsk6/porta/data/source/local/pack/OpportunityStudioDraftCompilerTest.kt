package io.github.vietdsk6.porta.data.source.local.pack

import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.OpportunityPackDocument
import io.github.vietdsk6.porta.domain.model.OpportunityStudioDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioExpectedValueDraft
import io.github.vietdsk6.porta.domain.model.OpportunityStudioRuleDraft
import io.github.vietdsk6.porta.domain.model.RuleNode
import io.github.vietdsk6.porta.domain.model.StudioDeadlineKind
import io.github.vietdsk6.porta.domain.model.StudioFactValueKind
import io.github.vietdsk6.porta.domain.model.StudioRootRuleKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpportunityStudioDraftCompilerTest {
    private val codec = OpportunityPackCodec()
    private val parser = OpportunityPackParser(codec, OpportunityPackValidator())
    private val compiler = OpportunityStudioDraftCompiler(codec, parser)

    @Test
    fun `valid draft appends a record and deterministically bumps the cloned pack version`() {
        val base = baseDocument()
        val originalJson = base.metadata.rawJson
        val draft = validDraft()

        val first = compiler.compile(base, draft)
        val second = compiler.compile(base, draft)

        assertTrue(first.isValid, first.diagnostics.joinToString())
        assertEquals(first.rawJson, second.rawJson)
        assertEquals(2, first.nextPackVersion)
        assertEquals(originalJson, base.metadata.rawJson)

        val candidateDto = codec.decode(assertNotNull(first.rawJson)).getOrThrow()
        val baseDto = codec.decode(originalJson).getOrThrow()
        assertEquals(2, candidateDto.version)
        assertEquals("2026-11-04", candidateDto.lastCheckedOn)
        assertEquals(2, candidateDto.opportunities.size)
        assertEquals(baseDto.opportunities.single(), candidateDto.opportunities.first())
        assertEquals("studio-door", candidateDto.opportunities.last().id)
    }

    @Test
    fun `all seven fact value kinds compile through the shared pack parser`() {
        val rules = listOf(
            atomicRule(
                id = "boolean-rule",
                factKey = FactKey.ENROLLED,
                expected = value(StudioFactValueKind.BOOLEAN, "true"),
            ),
            atomicRule(
                id = "number-rule",
                factKey = FactKey.IELTS_SCORE,
                expected = value(StudioFactValueKind.NUMBER, "7.0", "band"),
            ),
            atomicRule(
                id = "enum-rule",
                factKey = FactKey.STUDY_LEVEL,
                expected = value(StudioFactValueKind.ENUM, "UNDERGRADUATE"),
            ),
            atomicRule(
                id = "enum-list-rule",
                factKey = FactKey.FIELD_OF_STUDY,
                expected = value(StudioFactValueKind.ENUM_LIST, "computing, engineering"),
                operator = Operator.CONTAINS_ANY,
            ),
            atomicRule(
                id = "text-rule",
                factKey = FactKey.NATIONALITY,
                expected = value(StudioFactValueKind.TEXT, "Vietnam"),
            ),
            atomicRule(
                id = "date-rule",
                factKey = FactKey.DATE_OF_BIRTH,
                expected = value(StudioFactValueKind.DATE, "2010-08-02"),
                operator = Operator.DATE_BEFORE,
            ),
            atomicRule(
                id = "duration-rule",
                factKey = FactKey.EXPERIENCE_MONTHS,
                expected = value(StudioFactValueKind.DURATION, "24", "months"),
                operator = Operator.GREATER_THAN_OR_EQUAL,
            ),
        )

        val result = compiler.compile(baseDocument(), validDraft().copy(rules = rules))

        assertTrue(result.isValid, result.diagnostics.joinToString())
        val document = assertNotNull(parser.parse(assertNotNull(result.rawJson)).document)
        val root = assertIs<RuleNode.All>(document.opportunities.last().eligibilityRule)
        val values = root.children.map { assertIs<RuleNode.Atomic>(it).expected }
        assertIs<FactValue.BooleanValue>(values[0])
        assertIs<FactValue.NumberValue>(values[1])
        assertIs<FactValue.EnumValue>(values[2])
        assertIs<FactValue.EnumListValue>(values[3])
        assertIs<FactValue.TextValue>(values[4])
        assertIs<FactValue.DateValue>(values[5])
        assertIs<FactValue.DurationValue>(values[6])
    }

    @Test
    fun `malformed typed values return field diagnostics and no exportable json`() {
        val rules = listOf(
            atomicRule(
                id = "number-rule",
                factKey = FactKey.GPA,
                expected = value(StudioFactValueKind.NUMBER, "NaN", "4.0 scale"),
            ),
            atomicRule(
                id = "date-rule",
                factKey = FactKey.DATE_OF_BIRTH,
                expected = value(StudioFactValueKind.DATE, "04/11/2026"),
            ),
            atomicRule(
                id = "duration-rule",
                factKey = FactKey.EXPERIENCE_MONTHS,
                expected = value(StudioFactValueKind.DURATION, "-1", "months"),
            ),
        )

        val result = compiler.compile(baseDocument(), validDraft().copy(rules = rules))

        assertFalse(result.isValid)
        assertNull(result.rawJson)
        assertEquals(
            setOf("invalid_number_value", "invalid_date_value", "invalid_duration_value"),
            result.diagnostics.map { it.code }.toSet(),
        )
        assertTrue(result.diagnostics.all { it.path.startsWith("rules[") })
    }

    @Test
    fun `duplicate opportunity id is rejected without changing the base document`() {
        val base = baseDocument()
        val originalJson = base.metadata.rawJson

        val result = compiler.compile(
            base = base,
            draft = validDraft().copy(opportunityId = "base-door"),
        )

        assertFalse(result.isValid)
        assertNull(result.rawJson)
        assertTrue(result.diagnostics.any { it.code == "duplicate_opportunity_id" })
        assertEquals(originalJson, base.metadata.rawJson)
    }

    private fun validDraft(): OpportunityStudioDraft = OpportunityStudioDraft(
        opportunityId = "studio-door",
        title = "Studio-authored scholarship",
        provider = "Synthetic provider",
        category = OpportunityCategory.SCHOLARSHIP,
        cycle = "2027",
        cycleStartOn = "2026-11-01",
        sourceUrl = "https://example.invalid/studio-door",
        sourceSection = "Eligibility",
        lastCheckedOn = "2026-11-04",
        deadlineKind = StudioDeadlineKind.KNOWN,
        deadlineDate = "2027-05-01",
        rootRuleKind = StudioRootRuleKind.ALL,
        rules = listOf(
            atomicRule(
                id = "enrolled-rule",
                factKey = FactKey.ENROLLED,
                expected = value(StudioFactValueKind.BOOLEAN, "true"),
            ),
            OpportunityStudioRuleDraft.ManualCheck(
                id = "manual-fit",
                explanation = "Confirm the published programme-specific fit criteria.",
                sourceNote = "Synthetic source note.",
            ),
        ),
    )

    private fun atomicRule(
        id: String,
        factKey: FactKey,
        expected: OpportunityStudioExpectedValueDraft,
        operator: Operator = Operator.EQUALS,
    ): OpportunityStudioRuleDraft.Atomic = OpportunityStudioRuleDraft.Atomic(
        id = id,
        factKey = factKey,
        operator = operator,
        expected = expected,
        explanation = "Synthetic rule $id.",
        sourceNote = "Synthetic source note.",
        actionability = Actionability.CONTEXT_ONLY,
    )

    private fun value(
        kind: StudioFactValueKind,
        value: String,
        unit: String = "",
    ): OpportunityStudioExpectedValueDraft = OpportunityStudioExpectedValueDraft(
        kind = kind,
        value = value,
        unit = unit,
    )

    private fun baseDocument(): OpportunityPackDocument {
        val rawJson = codec.encode(
            OpportunityPackDto(
                schemaVersion = OpportunityPackCodec.CURRENT_SCHEMA_VERSION,
                id = "studio-test-pack",
                name = "Studio test pack",
                publisher = "Porta tests",
                version = 1,
                sourceUrl = "https://example.invalid/pack",
                lastCheckedOn = "2026-10-20",
                opportunities = listOf(
                    OpportunityDto(
                        id = "base-door",
                        title = "Base opportunity",
                        provider = "Base provider",
                        category = OpportunityCategory.FELLOWSHIP.name,
                        cycle = "2026",
                        sourceUrl = "https://example.invalid/base-door",
                        lastCheckedOn = "2026-10-20",
                        deadline = DeadlineDto.Tba,
                        eligibilityRule = RuleNodeDto.ManualCheck(
                            id = "base-manual-rule",
                            explanation = "Confirm the base opportunity manually.",
                            sourceNote = "Synthetic fixture.",
                        ),
                    ),
                ),
            ),
        )
        return assertNotNull(parser.parse(rawJson).document)
    }
}
