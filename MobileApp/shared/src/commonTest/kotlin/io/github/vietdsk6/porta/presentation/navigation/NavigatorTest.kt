package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NavigatorTest {

    private fun newState(
        start: ScreenRoute = DoorsScreenRoute,
        topLevel: Set<TopLevelScreenRoute> = setOf(
            DoorsScreenRoute,
            FrontierScreenRoute,
            StudentProfileScreenRoute,
        ),
        primary: TopLevelScreenRoute = DoorsScreenRoute,
    ): NavigationState {
        val topLevelBackStacks: Map<TopLevelScreenRoute, SnapshotStateList<ScreenRoute>> =
            topLevel.associateWith { route -> mutableStateListOf<ScreenRoute>(route) }
        val defaultBackstack: SnapshotStateList<ScreenRoute> =
            if (start is TopLevelScreenRoute) mutableStateListOf() else mutableStateListOf(start)
        val currentBackstack: SnapshotStateList<ScreenRoute> = mutableStateListOf<ScreenRoute>().apply {
            if (start is TopLevelScreenRoute) addAll(topLevelBackStacks.getValue(start)) else addAll(defaultBackstack)
        }
        return NavigationState(
            topLevelBackStacks = topLevelBackStacks,
            defaultBackstack = defaultBackstack,
            primaryTopLevelRoute = primary,
            currentBackstack = currentBackstack,
        )
    }

    @Test
    fun `add pushes onto the current back stack`() {
        val nav = Navigator(newState())
        val detail = OpportunityDetailScreenRoute("aurora")
        nav.add(detail)
        assertEquals(listOf(DoorsScreenRoute, detail), nav.state.currentBackstack.toList())
    }

    @Test
    fun `navigate pushes a non-top-level route`() {
        val nav = Navigator(newState())
        val detail = OpportunityDetailScreenRoute("aurora")
        nav.navigate(detail)
        assertEquals(detail, nav.state.currentBackstack.last())
    }

    @Test
    fun `navigate to a top-level route switches tab`() {
        val nav = Navigator(newState())
        nav.navigate(FrontierScreenRoute)
        assertEquals(FrontierScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `each tab keeps its own back stack`() {
        val nav = Navigator(newState())
        val detail = OpportunityDetailScreenRoute("aurora")
        nav.add(detail) // Doors tab: [Doors, OpportunityDetail]
        nav.switchToTab(FrontierScreenRoute) // Frontier tab: [Frontier]
        assertEquals(listOf(FrontierScreenRoute), nav.state.currentBackstack.toList())

        nav.switchToTab(DoorsScreenRoute) // Doors restored with its pushed entry
        assertEquals(listOf(DoorsScreenRoute, detail), nav.state.currentBackstack.toList())
    }

    @Test
    fun `switching to the active tab is a no-op`() {
        val nav = Navigator(newState())
        val detail = OpportunityDetailScreenRoute("aurora")
        nav.add(detail)
        nav.switchToTab(DoorsScreenRoute)
        assertEquals(listOf(DoorsScreenRoute, detail), nav.state.currentBackstack.toList())
    }

    @Test
    fun `goBack pops when more than one entry`() {
        val nav = Navigator(newState())
        nav.add(OpportunityDetailScreenRoute("aurora"))
        nav.goBack()
        assertEquals(listOf(DoorsScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `goBack from a non-primary tab returns to the primary tab`() {
        val nav = Navigator(newState())
        nav.switchToTab(FrontierScreenRoute)
        nav.goBack()
        assertEquals(DoorsScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `goBack on the primary tab root is a no-op`() {
        val nav = Navigator(newState())
        nav.goBack()
        assertEquals(listOf(DoorsScreenRoute), nav.state.currentBackstack.toList())
        assertEquals(DoorsScreenRoute, nav.state.topLevelRoute)
    }

    @Test
    fun `set clears the back stack and places the route on top`() {
        val nav = Navigator(newState())
        nav.add(OpportunityDetailScreenRoute("aurora"))
        nav.set(ActivePlanScreenRoute)
        assertEquals(listOf(ActivePlanScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `replace swaps the top entry`() {
        val nav = Navigator(newState())
        nav.add(OpportunityDetailScreenRoute("aurora"))
        nav.replace(PackInspectorScreenRoute)
        assertEquals(listOf(DoorsScreenRoute, PackInspectorScreenRoute), nav.state.currentBackstack.toList())
    }

    @Test
    fun `switching to an unregistered top-level route fails fast`() {
        // StudentProfileScreenRoute is deliberately left out of the registered tab set.
        val nav = Navigator(newState(topLevel = setOf(DoorsScreenRoute, FrontierScreenRoute)))
        assertFailsWith<IllegalArgumentException> {
            nav.switchToTab(StudentProfileScreenRoute)
        }
    }
}
