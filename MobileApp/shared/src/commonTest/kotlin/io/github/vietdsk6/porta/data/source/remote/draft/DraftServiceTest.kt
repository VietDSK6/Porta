package io.github.vietdsk6.porta.data.source.remote.draft

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DraftServiceTest {
    @Test
    fun offlineFixturesOfferOnlyQuotesPresentInTheirOfficialExcerpt() = runTest {
        val service = DemoDraftService()
        DraftDemoFixtures.all.forEach { fixture ->
            assertTrue(fixture.sourceUrl.startsWith("https://"))
            val drafts = service.draft(fixture.sourceText)
            assertTrue(drafts.isNotEmpty(), fixture.id)
            assertTrue(drafts.all { it.quote in fixture.sourceText }, fixture.id)
        }
        assertTrue(service.draft("Unrelated pasted text").isEmpty())
    }

    @Test
    fun geminiSendsSchemaAndKeyAndParsesCandidateWithoutLoggingResponse() = runTest {
        val candidateJson = buildJsonArray {
            add(
                buildJsonObject {
                    put("id", "age-18")
                    put("kind", "ATOMIC")
                    put("factKey", "AGE")
                    put("operator", "GREATER_THAN_OR_EQUAL")
                    putJsonObject("expected") {
                        put("kind", "NUMBER")
                        put("value", "18")
                        put("unit", "years")
                    }
                    put("actionability", "IMMUTABLE")
                    put("quote", "at least 18 years old")
                    put("rationale", "Published age threshold")
                },
            )
        }.toString()
        val envelope = buildJsonObject {
            putJsonArray("candidates") {
                add(
                    buildJsonObject {
                        putJsonObject("content") {
                            putJsonArray("parts") {
                                add(buildJsonObject { put("text", candidateJson) })
                            }
                        }
                    },
                )
            }
        }.toString()
        var requests = 0
        val client = HttpClient(
            MockEngine { request ->
                requests++
                assertEquals("test-key", request.headers["x-goog-api-key"])
                assertTrue(request.url.toString().endsWith("/gemini-3.5-flash-lite:generateContent"))
                val body = Json.parseToJsonElement((request.body as TextContent).text).jsonObject
                val config = body.getValue("generationConfig").jsonObject
                assertEquals("application/json", config.getValue("responseMimeType").jsonPrimitive.content)
                assertEquals("ARRAY", config.getValue("responseSchema").jsonObject.getValue("type").jsonPrimitive.content)
                assertTrue(config.getValue("temperature").jsonPrimitive.content.toDouble() <= 0.2)
                assertEquals(
                    "Official text: at least 18 years old",
                    body.getValue("contents").jsonArray.first().jsonObject
                        .getValue("parts").jsonArray.first().jsonObject.getValue("text").jsonPrimitive.content,
                )
                respond(envelope, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            },
        )
        val drafts = GeminiDraftService(client, "test-key").draft("Official text: at least 18 years old")
        assertEquals(1, requests)
        assertEquals(1, drafts.size)
        assertEquals("draft-1", drafts.single().id)
        assertEquals("18", drafts.single().expected?.value)
        client.close()
    }

    @Test
    fun malformedStructuredResponseIsRejected() = runTest {
        val envelope = buildJsonObject {
            putJsonArray("candidates") {
                add(buildJsonObject { putJsonObject("content") { putJsonArray("parts") { add(buildJsonObject { put("text", "not json") }) } } })
            }
        }.toString()
        val client = HttpClient(MockEngine { respond(envelope, HttpStatusCode.OK) })
        assertFailsWith<DraftServiceException> {
            GeminiDraftService(client, "test-key").draft("Some official source text")
        }
        client.close()
    }
}
