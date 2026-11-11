package io.github.vietdsk6.porta.data.source.remote.draft

import io.github.vietdsk6.porta.domain.model.DraftExpectedValue
import io.github.vietdsk6.porta.domain.model.RawRuleDraftSuggestion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Bundled excerpts are short, independently checked against their linked official pages. */
data class DraftDemoFixture(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val sourceText: String,
    val suggestions: List<RawRuleDraftSuggestion>,
)

object DraftDemoFixtures {
    val all: List<DraftDemoFixture> = Json.decodeFromString<List<FixtureDto>>(FIXTURES_JSON).map { fixture ->
        DraftDemoFixture(
            id = fixture.id,
            title = fixture.title,
            sourceUrl = fixture.sourceUrl,
            sourceText = fixture.sourceText,
            suggestions = fixture.suggestions.map { suggestion ->
                RawRuleDraftSuggestion(
                    id = suggestion.id,
                    kind = suggestion.kind,
                    factKey = suggestion.factKey,
                    operator = suggestion.operator,
                    expected = suggestion.expectedKind?.let { kind ->
                        DraftExpectedValue(kind, suggestion.expectedValue.orEmpty(), suggestion.expectedUnit)
                    },
                    actionability = suggestion.actionability,
                    quote = fixture.sourceText,
                    rationale = suggestion.rationale,
                )
            },
        )
    }

    // One short excerpt per official source. Quotes are the exact sourceText field, so no copied text is duplicated.
    // https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters
    // https://summerofcode.withgoogle.com/get-started
    // https://careers.cern/jobs/cern-openlab-summer-student-programme-2026/
    private const val FIXTURES_JSON = """
        [
          {
            "id": "erasmus-mundus",
            "title": "Erasmus Mundus Joint Masters",
            "sourceUrl": "https://erasmus-plus.ec.europa.eu/opportunities/individuals/students/erasmus-mundus-joint-masters",
            "sourceText": "have a bachelor’s degree (first degree), or be in your last year of bachelor studies",
            "suggestions": [
              {
                "id": "erasmus-degree-branch",
                "kind": "MANUAL_CHECK",
                "rationale": "This is an alternative branch. A single degree-completed fact would overstate the requirement."
              }
            ]
          },
          {
            "id": "gsoc",
            "title": "Google Summer of Code",
            "sourceUrl": "https://summerofcode.withgoogle.com/get-started",
            "sourceText": "Must be at least 18 years old at time of registration.",
            "suggestions": [
              {
                "id": "gsoc-minimum-age",
                "kind": "ATOMIC",
                "factKey": "AGE",
                "operator": "GREATER_THAN_OR_EQUAL",
                "expectedKind": "NUMBER",
                "expectedValue": "18",
                "expectedUnit": "years",
                "actionability": "IMMUTABLE",
                "rationale": "The official text names an age threshold; registration timing still needs human review."
              }
            ]
          },
          {
            "id": "cern-openlab",
            "title": "CERN openlab summer student programme",
            "sourceUrl": "https://careers.cern/jobs/cern-openlab-summer-student-programme-2026/",
            "sourceText": "You remain enrolled as a full-time student for the full duration of your summer at CERN.",
            "suggestions": [
              {
                "id": "cern-full-duration-enrollment",
                "kind": "MANUAL_CHECK",
                "rationale": "A current enrolled flag cannot prove enrollment throughout the full programme."
              }
            ]
          }
        ]
    """
}

/** The offline path is intentionally limited to known excerpts, never a general source interpreter. */
class DemoDraftService : DraftService {
    override suspend fun draft(sourceText: String): List<RawRuleDraftSuggestion> = DraftDemoFixtures.all.firstOrNull { sourceText.contains(it.sourceText) }?.suggestions.orEmpty()
}

@Serializable
private data class FixtureDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("sourceUrl") val sourceUrl: String,
    @SerialName("sourceText") val sourceText: String,
    @SerialName("suggestions") val suggestions: List<FixtureSuggestionDto>,
)

@Serializable
private data class FixtureSuggestionDto(
    @SerialName("id") val id: String,
    @SerialName("kind") val kind: String,
    @SerialName("factKey") val factKey: String? = null,
    @SerialName("operator") val operator: String? = null,
    @SerialName("expectedKind") val expectedKind: String? = null,
    @SerialName("expectedValue") val expectedValue: String? = null,
    @SerialName("expectedUnit") val expectedUnit: String? = null,
    @SerialName("actionability") val actionability: String? = null,
    @SerialName("rationale") val rationale: String,
)
