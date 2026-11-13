package io.github.vietdsk6.porta.data.scenario

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleScenarioProviderTest {
    @Test
    fun `fictional IELTS lesson shows four newly passing sample checks`() {
        val lesson = SampleScenarioProvider().thresholdLesson

        assertEquals(6.5, lesson.beforeScore)
        assertEquals(7.0, lesson.afterScore)
        assertEquals(
            setOf("Aurora Scholars", "Forge Student Lab", "Horizon Exchange", "Spark Fellowship"),
            lesson.changedOpportunityTitles.toSet(),
        )
        assertEquals(listOf("Local Makers Challenge"), lesson.unchangedOpportunityTitles)
    }
}
