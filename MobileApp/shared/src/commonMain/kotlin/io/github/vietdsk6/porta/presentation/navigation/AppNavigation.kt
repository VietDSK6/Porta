package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavItem
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavigationBar
import io.github.vietdsk6.porta.designsystem.theme.AppTheme
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_doors
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_frontier
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_profile
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_route
import io.github.vietdsk6.porta.generated.resources.ic_doors
import io.github.vietdsk6.porta.generated.resources.ic_doors_selected
import io.github.vietdsk6.porta.generated.resources.ic_frontier
import io.github.vietdsk6.porta.generated.resources.ic_frontier_selected
import io.github.vietdsk6.porta.generated.resources.ic_route
import io.github.vietdsk6.porta.generated.resources.ic_route_selected
import io.github.vietdsk6.porta.generated.resources.ic_student_profile
import io.github.vietdsk6.porta.generated.resources.ic_student_profile_selected
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreen
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateScreen
import io.github.vietdsk6.porta.presentation.screens.actualprofileupdate.ActualProfileUpdateViewModel
import io.github.vietdsk6.porta.presentation.screens.dataprivacy.DataPrivacyScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingDestination
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitystudio.OpportunityStudioViewModel
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorScreen
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.route.RouteScreen
import io.github.vietdsk6.porta.presentation.screens.route.RouteViewModel
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.util.extensions.isKeyboardOpen
import kotlinx.coroutines.flow.Flow
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
    RouteScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_route,
        icon = Res.drawable.ic_route,
        selectedIcon = Res.drawable.ic_route_selected,
    ),
    DoorsScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_doors,
        icon = Res.drawable.ic_doors,
        selectedIcon = Res.drawable.ic_doors_selected,
    ),
    FrontierScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_frontier,
        icon = Res.drawable.ic_frontier,
        selectedIcon = Res.drawable.ic_frontier_selected,
    ),
    StudentProfileScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_profile,
        icon = Res.drawable.ic_student_profile,
        selectedIcon = Res.drawable.ic_student_profile_selected,
    ),
)

@Preview(name = "Route selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationRoutePreview() {
    PortaBottomNavigationPreview(selectedIndex = 0)
}

@Preview(name = "Doors selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationDoorsPreview() {
    PortaBottomNavigationPreview(selectedIndex = 1)
}

@Preview(name = "Next moves selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationFrontierPreview() {
    PortaBottomNavigationPreview(selectedIndex = 2)
}

@Preview(name = "Profile selected", widthDp = 390)
@Composable
private fun PortaBottomNavigationProfilePreview() {
    PortaBottomNavigationPreview(selectedIndex = 3)
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
fun AppNavigation(desktopCommands: Flow<DesktopCommand>? = null) {
    val navigationState = rememberNavigationState(
        startRoute = OnBoardingScreenRoute,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys,
        primaryTopLevelRoute = RouteScreenRoute,
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val profileSaveNotice = rememberSaveable { mutableStateOf(false) }
    val pendingDesktopAction = remember { mutableStateOf<DesktopCommand?>(null) }
    val showAbout = remember { mutableStateOf(false) }

    LaunchedEffect(desktopCommands, navigator) {
        desktopCommands?.collect { command ->
            when (command) {
                DesktopCommand.NavigateRoute -> navigator.navigate(RouteScreenRoute)

                DesktopCommand.NavigateDoors -> navigator.navigate(DoorsScreenRoute)

                DesktopCommand.NavigateNextMoves -> navigator.navigate(FrontierScreenRoute)

                DesktopCommand.NavigateProfile -> navigator.navigate(StudentProfileScreenRoute)

                DesktopCommand.NavigatePacks -> navigator.navigate(PackInspectorScreenRoute)

                DesktopCommand.ShowSources -> navigator.navigate(PackInspectorScreenRoute)

                DesktopCommand.ShowAbout -> showAbout.value = true

                DesktopCommand.FocusSearch -> {
                    pendingDesktopAction.value = command
                    navigator.navigate(DoorsScreenRoute)
                }

                DesktopCommand.ImportPack,
                is DesktopCommand.ImportPackJson,
                DesktopCommand.ExportPack,
                -> {
                    pendingDesktopAction.value = command
                    navigator.navigate(PackInspectorScreenRoute)
                }

                DesktopCommand.ImportProfile -> {
                    pendingDesktopAction.value = command
                    navigator.navigate(DataPrivacyScreenRoute)
                }

                DesktopCommand.ExportProfile -> {
                    pendingDesktopAction.value = command
                    navigator.navigate(StudentProfileScreenRoute)
                }
            }
        }
    }

    val entryProvider = remember(navigator) { entryProvider { screens(navigator, profileSaveNotice) } }

    val isAtRoot = navigationState.currentBackstack.size <= 1
    val activeTopLevel = navigationState.topLevelRoute
    val selectedTopLevelIndex = TOP_LEVEL_ROUTES.keys.indexOf(activeTopLevel).coerceAtLeast(0)
    val bottomNavItems = remember { TOP_LEVEL_ROUTES.values.toList() }
    val isTopLevelNavigationVisible = isAtRoot && activeTopLevel != null && !isKeyboardOpen()

    val desktopRequest = pendingDesktopAction.value?.let { command ->
        DesktopCommandRequest(command) {
            if (pendingDesktopAction.value == command) pendingDesktopAction.value = null
        }
    }
    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalDesktopCommand provides desktopRequest,
    ) {
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
        if (showAbout.value) {
            AlertDialog(
                onDismissRequest = { showAbout.value = false },
                title = { Text("About Porta") },
                text = { Text("Porta plans honest routes through published student opportunities. Check live sources before acting.") },
                confirmButton = { TextButton(onClick = { showAbout.value = false }) { Text("Close") } },
            )
        }
    }
}

// Maps each route to its screen. Every `entry<X>` wires the screen's navigation callbacks to the
// Navigator; ViewModels are obtained per-entry via koinViewModel<…>().
private fun EntryProviderScope<ScreenRoute>.screens(
    navigator: Navigator,
    profileSaveNotice: MutableState<Boolean>,
) {
    // ── Top-level (no animation when switching tabs) ────────────────────────────

    entry<RouteScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<RouteViewModel>()
        RouteScreen(
            viewModel = viewModel,
            onOpenNextMoves = { navigator.navigate(FrontierScreenRoute) },
            onOpenActivePlan = { navigator.navigate(ActivePlanScreenRoute) },
        )
    }

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
            saveNotice = profileSaveNotice.value,
            onSaveNoticeDismissed = { profileSaveNotice.value = false },
            onEditProfile = { navigator.navigate(ProfileSetupScreenRoute(returnToProfile = true)) },
            onOpenPackInspector = { navigator.navigate(PackInspectorScreenRoute) },
            onOpenDataPrivacy = { navigator.navigate(DataPrivacyScreenRoute) },
        )
    }

    // ── Pushed destinations ─────────────────────────────────────────────────────

    entry<OnBoardingScreenRoute> {
        val viewModel = koinViewModel<OnBoardingViewModel>()
        OnBoardingScreen(
            viewModel = viewModel,
            onOnBoardingFinished = { completion ->
                navigator.set(
                    when (completion.destination) {
                        OnboardingDestination.SAMPLE_MAP -> RouteScreenRoute
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
            onOpenDataPrivacy = { navigator.navigate(DataPrivacyScreenRoute) },
            onCompleted = {
                if (key.returnToProfile) {
                    if (navigator.state.currentBackstack.dropLast(1).lastOrNull() == StudentProfileScreenRoute) {
                        profileSaveNotice.value = true
                    }
                    navigator.goBack()
                } else {
                    navigator.set(RouteScreenRoute)
                }
            },
            onNavigateBack = {
                if (key.returnToProfile) navigator.goBack() else navigator.set(RouteScreenRoute)
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
            onOpenOpportunityStudio = { navigator.navigate(OpportunityStudioScreenRoute) },
        )
    }

    entry<OpportunityStudioScreenRoute> {
        val viewModel = koinViewModel<OpportunityStudioViewModel>()
        OpportunityStudioScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<DataPrivacyScreenRoute> {
        val viewModel = koinViewModel<StudentProfileViewModel>()
        DataPrivacyScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onOpenPackInspector = { navigator.navigate(PackInspectorScreenRoute) },
        )
    }

    entry<ActualProfileUpdateScreenRoute> {
        val viewModel = koinViewModel<ActualProfileUpdateViewModel>()
        ActualProfileUpdateScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
            onProfileUpdated = { navigator.set(RouteScreenRoute) },
        )
    }

    // Add new screen entries below — generate_screen.sh inserts here.
}
