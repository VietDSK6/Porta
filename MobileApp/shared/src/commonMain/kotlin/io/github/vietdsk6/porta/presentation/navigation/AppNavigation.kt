package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavItem
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavigationBar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_gallery
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_home
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_profile
import io.github.vietdsk6.porta.generated.resources.ic_doors
import io.github.vietdsk6.porta.generated.resources.ic_doors_selected
import io.github.vietdsk6.porta.generated.resources.ic_frontier
import io.github.vietdsk6.porta.generated.resources.ic_frontier_selected
import io.github.vietdsk6.porta.generated.resources.ic_student_profile
import io.github.vietdsk6.porta.generated.resources.ic_student_profile_selected
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreen
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreen
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenStyle
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingDestination
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorScreen
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.util.extensions.isKeyboardOpen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Access the [Navigator] from any screen without threading it through every composable. */
val LocalNavigator = compositionLocalOf<Navigator> { error("No Navigator provided") }

// Tab switches should be instant — no enter/exit animation (unlike pushes, which animate).
private val noAnimationTransition = EnterTransition.None togetherWith ExitTransition.None
private val noAnimationMetadata =
    NavDisplay.transitionSpec { noAnimationTransition } +
        NavDisplay.popTransitionSpec { noAnimationTransition } +
        NavDisplay.predictivePopTransitionSpec { noAnimationTransition }

// Top-level destinations in display order. Values render either the compact bar or wide rail.
private val TOP_LEVEL_ROUTES: Map<TopLevelScreenRoute, BottomNavItem> = linkedMapOf(
    DoorsScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_home,
        icon = Res.drawable.ic_doors,
        selectedIcon = Res.drawable.ic_doors_selected,
    ),
    FrontierScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_gallery,
        icon = Res.drawable.ic_frontier,
        selectedIcon = Res.drawable.ic_frontier_selected,
    ),
    StudentProfileScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_profile,
        icon = Res.drawable.ic_student_profile,
        selectedIcon = Res.drawable.ic_student_profile_selected,
    ),
)

@Preview(name = "Doors selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationDoorsPreview() {
    PortaBottomNavigationPreview(selectedIndex = 0)
}

@Preview(name = "Next moves selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationFrontierPreview() {
    PortaBottomNavigationPreview(selectedIndex = 1)
}

@Preview(name = "Profile selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationProfilePreview() {
    PortaBottomNavigationPreview(selectedIndex = 2)
}

@Composable
private fun PortaBottomNavigationPreview(selectedIndex: Int) {
    AppTheme {
        BottomNavigationBar(
            items = TOP_LEVEL_ROUTES.values.toList(),
            selectedIndex = selectedIndex,
            onClickItem = {},
        )
    }
}

/**
 * The single nav host: owns the [Navigator], renders the current back stack via [NavDisplay],
 * and shows adaptive top-level navigation only at a tab root (hidden on pushed screens and when
 * the keyboard is open). Compact windows use a bottom bar; wide windows use a rail. Starts on
 * onboarding.
 */
@Composable
fun AppNavigation() {
    val navigationState = rememberNavigationState(
        startRoute = OnBoardingScreenRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys,
        primaryTopLevelRoute = DoorsScreenRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider = remember(navigator) { entryProvider { screens(navigator) } }

    val isAtRoot = navigationState.currentBackstack.size <= 1
    val activeTopLevel = navigationState.topLevelRoute
    val selectedTopLevelIndex = TOP_LEVEL_ROUTES.keys.indexOf(activeTopLevel).coerceAtLeast(0)
    val bottomNavItems = remember { TOP_LEVEL_ROUTES.values.toList() }
    val isTopLevelNavigationVisible = isAtRoot && activeTopLevel != null && !isKeyboardOpen()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        AdaptiveAppScaffold(
            items = bottomNavItems,
            selectedIndex = selectedTopLevelIndex,
            navigationVisible = isTopLevelNavigationVisible,
            onClickItem = { clickedItem ->
                val targetRoute = TOP_LEVEL_ROUTES.entries
                    .firstOrNull { it.value === clickedItem }?.key ?: return@AdaptiveAppScaffold
                navigator.navigate(targetRoute)
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavDisplay(
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )
            }
        }
    }
}

// Maps each route to its screen. Every `entry<X>` wires the screen's navigation callbacks to the
// Navigator; ViewModels are obtained per-entry via koinViewModel<…>().
private fun EntryProviderScope<ScreenRoute>.screens(navigator: Navigator) {
    // ── Top-level (no animation when switching tabs) ────────────────────────────

    entry<DoorsScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<DoorsViewModel>()
        DoorsScreen(
            viewModel = viewModel,
            onOpenOpportunity = { id ->
                navigator.navigate(OpportunityDetailScreenRoute(opportunityId = id))
            },
        )
    }

    entry<FrontierScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<FrontierViewModel>()
        FrontierScreen(
            viewModel = viewModel,
            onPreviewScenario = { actionId ->
                navigator.navigate(ScenarioPreviewScreenRoute(actionId = actionId))
            },
            onOpenActivePlan = { navigator.navigate(ActivePlanScreenRoute) },
        )
    }

    entry<StudentProfileScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<StudentProfileViewModel>()
        StudentProfileScreen(
            viewModel = viewModel,
            onEditProfile = { navigator.navigate(ProfileSetupScreenRoute(returnToProfile = true)) },
            onOpenPackInspector = { navigator.navigate(PackInspectorScreenRoute) },
        )
    }

    // ── Pushed destinations ─────────────────────────────────────────────────────

    entry<OnBoardingScreenRoute> {
        val viewModel = koinViewModel<OnBoardingViewModel>()
        OnBoardingScreen(
            style = OnBoardingScreenStyle.STYLE1,
            viewModel = viewModel,
            onOnBoardingFinished = { completion ->
                navigator.set(
                    when (completion.destination) {
                        OnboardingDestination.SAMPLE_MAP -> DoorsScreenRoute
                        OnboardingDestination.PROFILE_SETUP -> ProfileSetupScreenRoute()
                    },
                )
            },
        )
    }

    entry<ProfileSetupScreenRoute> { key ->
        val viewModel = koinViewModel<ProfileSetupViewModel>()
        ProfileSetupScreen(
            viewModel = viewModel,
            requestedFactKeyName = key.requestedFactKeyName,
            requestContext = key.requestContext,
            onCompleted = {
                if (key.returnToProfile) navigator.goBack() else navigator.set(DoorsScreenRoute)
            },
            onNavigateBack = {
                if (key.returnToProfile) navigator.goBack() else navigator.set(DoorsScreenRoute)
            },
        )
    }

    entry<OpportunityDetailScreenRoute> { key ->
        val viewModel = koinViewModel<OpportunityDetailViewModel>(
            parameters = { parametersOf(key.opportunityId) },
        )
        OpportunityDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onPreviewAction = { actionId ->
                navigator.navigate(ScenarioPreviewScreenRoute(actionId = actionId))
            },
            onEditProfile = { factKey, context ->
                navigator.navigate(
                    ProfileSetupScreenRoute(
                        returnToProfile = true,
                        requestedFactKeyName = factKey.name,
                        requestContext = context,
                    ),
                )
            },
        )
    }

    entry<ScenarioPreviewScreenRoute> { key ->
        val viewModel = koinViewModel<ScenarioPreviewViewModel>(
            parameters = { parametersOf(key.actionId) },
        )
        ScenarioPreviewScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onPlanSaved = { navigator.replace(ActivePlanScreenRoute) },
            onBuildProfile = { navigator.replace(ProfileSetupScreenRoute()) },
        )
    }

    entry<ActivePlanScreenRoute> {
        val viewModel = koinViewModel<ActivePlanViewModel>()
        ActivePlanScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onBrowseActions = { navigator.navigate(FrontierScreenRoute) },
            onPreviewImpact = { actionId ->
                navigator.replace(ScenarioPreviewScreenRoute(actionId = actionId))
            },
            onUpdateProfile = { navigator.navigate(ActualProfileUpdateScreenRoute) },
        )
    }

    entry<PackInspectorScreenRoute> {
        val viewModel = koinViewModel<PackInspectorViewModel>()
        PackInspectorScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<ActualProfileUpdateScreenRoute> {
        val viewModel = koinViewModel<ActualProfileUpdateViewModel>()
        ActualProfileUpdateScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onProfileUpdated = { navigator.set(DoorsScreenRoute) },
        )
    }

    // Add new screen entries below — generate_screen.sh inserts here.
}
