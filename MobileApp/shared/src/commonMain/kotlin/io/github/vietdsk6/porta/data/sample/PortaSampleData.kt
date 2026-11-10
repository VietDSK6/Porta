package io.github.vietdsk6.porta.data.sample

import io.github.vietdsk6.porta.domain.model.ActionTemplate
import io.github.vietdsk6.porta.domain.model.Actionability
import io.github.vietdsk6.porta.domain.model.CostBand
import io.github.vietdsk6.porta.domain.model.Deadline
import io.github.vietdsk6.porta.domain.model.EffortLevel
import io.github.vietdsk6.porta.domain.model.FactKey
import io.github.vietdsk6.porta.domain.model.FactValue
import io.github.vietdsk6.porta.domain.model.Operator
import io.github.vietdsk6.porta.domain.model.Opportunity
import io.github.vietdsk6.porta.domain.model.OpportunityCategory
import io.github.vietdsk6.porta.domain.model.ProfileFact
import io.github.vietdsk6.porta.domain.model.ProfileSnapshot
import io.github.vietdsk6.porta.domain.model.RuleNode
import kotlinx.datetime.LocalDate

/**
 * Clearly labelled fictional profiles plus a small synthetic opportunity set for onboarding.
 * The judge profile is evaluated against the separate, source-linked bundled catalog; none of the
 * fictional data must be presented as a verified real student's profile.
 */
object PortaSampleData {
    private const val PERSISTED_DEMO_FACT_ID_PREFIX = "porta-demo:"
    const val JUDGE_DEMO_HORIZON_DAYS = 180

    val referenceDate = LocalDate(2026, 10, 20)

    val profile = ProfileSnapshot(
        id = "porta-sample-profile",
        facts = listOf(
            fact(FactKey.STUDY_LEVEL, FactValue.EnumValue("undergraduate"), Actionability.TIME_DEPENDENT),
            fact(FactKey.FIELD_OF_STUDY, FactValue.TextValue("Computer Science"), Actionability.CONTEXT_ONLY),
            fact(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band"), Actionability.CONTROLLABLE),
            fact(FactKey.GPA, FactValue.NumberValue(3.6, "4.0 scale"), Actionability.CONTROLLABLE),
            fact(FactKey.ENROLLED, FactValue.BooleanValue(true), Actionability.TIME_DEPENDENT),
            fact(FactKey.NATIONALITY, FactValue.EnumValue("VN"), Actionability.CONTEXT_ONLY),
            fact(FactKey.AGE, FactValue.NumberValue(20.0, "years"), Actionability.IMMUTABLE),
            fact(FactKey.DEGREE_COMPLETED, FactValue.BooleanValue(false), Actionability.TIME_DEPENDENT),
            fact(FactKey.EXPERIENCE_MONTHS, FactValue.NumberValue(6.0, "months"), Actionability.TIME_DEPENDENT),
            fact(FactKey.AVAILABLE_FULL_PERIOD, FactValue.BooleanValue(false), Actionability.CONTROLLABLE),
        ).associateBy(ProfileFact::key),
    )

    /**
     * A richer fictional persona for the persisted judge flow. Unlike [profile], this is not tied
     * to the four-door IELTS onboarding lesson. Its deliberate strengths and gaps produce distinct
     * degree, English and programme-verification choices in the sourced catalog.
     */
    val judgeDemoProfile = ProfileSnapshot(
        id = "porta-judge-demo-profile",
        facts = listOf(
            fact(FactKey.STUDY_LEVEL, FactValue.EnumValue("undergraduate"), Actionability.TIME_DEPENDENT),
            fact(FactKey.FIELD_OF_STUDY, FactValue.TextValue("Computer Science"), Actionability.CONTEXT_ONLY),
            fact(FactKey.GPA, FactValue.NumberValue(3.45, "4.0 scale"), Actionability.CONTROLLABLE),
            fact(FactKey.IELTS_SCORE, FactValue.NumberValue(6.5, "band"), Actionability.CONTROLLABLE),
            fact(FactKey.ENROLLED, FactValue.BooleanValue(true), Actionability.TIME_DEPENDENT),
            fact(FactKey.NATIONALITY, FactValue.EnumValue("VN"), Actionability.CONTEXT_ONLY),
            fact(FactKey.RESIDENCY, FactValue.EnumValue("VN"), Actionability.CONTEXT_ONLY),
            fact(FactKey.AGE, FactValue.NumberValue(21.0, "years"), Actionability.IMMUTABLE),
            fact(FactKey.DATE_OF_BIRTH, FactValue.DateValue(LocalDate(2005, 3, 18)), Actionability.IMMUTABLE),
            fact(FactKey.DEGREE_COMPLETED, FactValue.BooleanValue(false), Actionability.TIME_DEPENDENT),
            fact(FactKey.EXPERIENCE_MONTHS, FactValue.NumberValue(10.0, "months"), Actionability.TIME_DEPENDENT),
            fact(
                FactKey.COMMUNITY_PROJECT,
                FactValue.TextValue("Kotlin mentoring for local high-school students"),
                Actionability.UNKNOWN,
            ),
        ).associateBy(ProfileFact::key),
    )

    val judgeDemoSavedOpportunityIds: Set<String> = setOf(
        "erasmus-mundus-joint-masters",
        "schwarzman-scholars-2028-2029",
        "kotlin-student-coding-competition-2026",
    )

    val opportunities: List<Opportunity> = listOf(
        sampleDoor("aurora", "Aurora Scholars", "Northstar Foundation", OpportunityCategory.SCHOLARSHIP, 7.0),
        sampleDoor("forge", "Forge Student Lab", "Open Learning Lab", OpportunityCategory.INTERNSHIP, 7.0),
        sampleDoor("horizon", "Horizon Exchange", "Atlas University", OpportunityCategory.ACADEMIC_PROGRAM, 7.0),
        sampleDoor("spark", "Spark Fellowship", "Civic Futures", OpportunityCategory.FELLOWSHIP, 7.0),
        sampleDoor("local", "Local Makers Challenge", "Makers Guild", OpportunityCategory.STUDENT_CONTEST, 6.0),
    )

    val savedOpportunityIds: Set<String> = setOf(
        "aurora",
        "horizon",
        "kotlin-student-coding-competition-2026",
        "oecd-internship-rolling",
        "cern-openlab-summer-student-2027",
    )

    /**
     * Re-bases the synthetic sample onto the active local profile so every normal profile-backed
     * workflow can be exercised without presenting the facts as a real student's data.
     */
    fun persistedDemoFacts(updatedAtEpochMillis: Long): List<ProfileFact> = judgeDemoProfile.facts.values
        .sortedBy { fact -> fact.key.name }
        .map { fact ->
            fact.copy(
                id = "$PERSISTED_DEMO_FACT_ID_PREFIX${fact.key.name.lowercase()}",
                profileId = ProfileFact.DEFAULT_PROFILE_ID,
                updatedAtEpochMillis = updatedAtEpochMillis,
            )
        }

    fun isPersistedDemoProfile(profile: ProfileSnapshot): Boolean = profile.facts.values
        .any { fact -> fact.id.startsWith(PERSISTED_DEMO_FACT_ID_PREFIX) }

    private fun sampleDoor(
        id: String,
        title: String,
        provider: String,
        category: OpportunityCategory,
        requiredIelts: Double,
    ): Opportunity {
        val action = ActionTemplate(
            id = "raise-ielts-$id",
            title = "Raise IELTS from 6.5 to 7.0",
            targetFactKey = FactKey.IELTS_SCORE,
            targetValue = FactValue.NumberValue(7.0, "band"),
            targetActionability = Actionability.CONTROLLABLE,
            minimumDurationDays = 45,
            effort = EffortLevel.MEDIUM,
            costBand = CostBand.LOW,
            completionEvidenceTypes = setOf("official score report"),
            sourceNote = "Synthetic sample action for demonstrating Porta; not personal advice.",
        )
        return Opportunity(
            id = id,
            title = title,
            provider = provider,
            category = category,
            cycle = "Synthetic sample · 2027",
            cycleStartOn = LocalDate(2026, 10, 1),
            sourceUrl = "https://example.invalid/porta-sample/$id",
            sourceSection = "Synthetic eligibility example",
            lastCheckedOn = referenceDate,
            deadline = Deadline.Known(LocalDate(2027, 6, 1)),
            eligibilityRule = RuleNode.Atomic(
                id = "$id-ielts",
                factKey = FactKey.IELTS_SCORE,
                operator = Operator.GREATER_THAN_OR_EQUAL,
                expected = FactValue.NumberValue(requiredIelts, "band"),
                explanation = "The synthetic sample requires IELTS $requiredIelts or above.",
                sourceNote = "Synthetic sample rule; it is not a real provider requirement.",
                actionability = Actionability.CONTROLLABLE,
                actionTemplateId = action.id,
            ),
            readinessItems = listOf("Confirm current cycle on the official source before applying."),
            actionTemplates = listOf(action),
            disclaimer = "Sample data only. This is not a real opportunity or eligibility decision.",
        )
    }

    private fun fact(
        key: FactKey,
        value: FactValue,
        actionability: Actionability,
    ): ProfileFact = ProfileFact(
        id = "porta-sample-${key.name.lowercase()}",
        profileId = "porta-sample-profile",
        key = key,
        value = value,
        actionability = actionability,
    )
}
