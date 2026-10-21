package io.github.vietdsk6.porta.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.github.vietdsk6.porta.data.source.featureflag.FeatureFlagManager
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavItem
import io.github.vietdsk6.porta.designsystem.components.bottomnav.BottomNavigationBar
import io.github.vietdsk6.porta.designsystem.generated.resources.UiRes
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_gallery
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_home
import io.github.vietdsk6.porta.designsystem.generated.resources.ic_profile
import io.github.vietdsk6.porta.generated.resources.Res
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_gallery
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_home
import io.github.vietdsk6.porta.generated.resources.bottom_nav_label_profile
import io.github.vietdsk6.porta.presentation.screens.account.AccountScreen
import io.github.vietdsk6.porta.presentation.screens.account.AccountViewModel
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanScreen
import io.github.vietdsk6.porta.presentation.screens.activeplan.ActivePlanViewModel
import io.github.vietdsk6.porta.presentation.screens.creditbalance.CreditBalanceScreen
import io.github.vietdsk6.porta.presentation.screens.creditbalance.CreditBalanceViewModel
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsScreen
import io.github.vietdsk6.porta.presentation.screens.doors.DoorsViewModel
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierScreen
import io.github.vietdsk6.porta.presentation.screens.frontier.FrontierViewModel
import io.github.vietdsk6.porta.presentation.screens.gallery.GalleryScreen
import io.github.vietdsk6.porta.presentation.screens.gallery.GalleryViewModel
import io.github.vietdsk6.porta.presentation.screens.generationresult.GenerationResultScreen
import io.github.vietdsk6.porta.presentation.screens.generationresult.GenerationResultViewModel
import io.github.vietdsk6.porta.presentation.screens.helpandsupport.HelpAndSupportScreen
import io.github.vietdsk6.porta.presentation.screens.home.HomeScreen
import io.github.vietdsk6.porta.presentation.screens.home.HomeViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreen
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingScreenStyle
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnBoardingViewModel
import io.github.vietdsk6.porta.presentation.screens.onboarding.OnboardingDestination
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailScreen
import io.github.vietdsk6.porta.presentation.screens.opportunitydetail.OpportunityDetailViewModel
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorScreen
import io.github.vietdsk6.porta.presentation.screens.packinspector.PackInspectorViewModel
import io.github.vietdsk6.porta.presentation.screens.paywall.PaywallScreen
import io.github.vietdsk6.porta.presentation.screens.paywall.PaywallViewModel
import io.github.vietdsk6.porta.presentation.screens.paywall.remotepaywall.RemotePaywallScreen
import io.github.vietdsk6.porta.presentation.screens.profile.ProfileScreen
import io.github.vietdsk6.porta.presentation.screens.profile.ProfileViewModel
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupScreen
import io.github.vietdsk6.porta.presentation.screens.profilesetup.ProfileSetupViewModel
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewScreen
import io.github.vietdsk6.porta.presentation.screens.scenariopreview.ScenarioPreviewViewModel
import io.github.vietdsk6.porta.presentation.screens.signin.SignInScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileScreen
import io.github.vietdsk6.porta.presentation.screens.studentprofile.StudentProfileViewModel
import io.github.vietdsk6.porta.presentation.screens.subscriptions.SubscriptionsScreen
import io.github.vietdsk6.porta.presentation.screens.subscriptions.SubscriptionsViewModel
import io.github.vietdsk6.porta.root.AppConfiguration
import io.github.vietdsk6.porta.util.Constants
import io.github.vietdsk6.porta.util.extensions.isKeyboardOpen
import org.koin.compose.koinInject
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

// Bottom-nav tabs, in display order. The keys are the routes; the values render the nav bar.
private val TOP_LEVEL_ROUTES: Map<TopLevelScreenRoute, BottomNavItem> = linkedMapOf(
    DoorsScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_home,
        icon = UiRes.drawable.ic_home,
    ),
    FrontierScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_gallery,
        icon = UiRes.drawable.ic_gallery,
    ),
    StudentProfileScreenRoute to BottomNavItem(
        label = Res.string.bottom_nav_label_profile,
        icon = UiRes.drawable.ic_profile,
    ),
)

/**
 * The single nav host: owns the [Navigator], renders the current back stack via [NavDisplay],
 * and shows the bottom nav bar only at a tab root (hidden on pushed screens and when the keyboard
 * is open). Starts on onboarding.
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
    val isBottomNavVisible = isAtRoot && activeTopLevel != null && !isKeyboardOpen()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                NavDisplay(
                    entries = navigationState.toDecoratedEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )
            }
            AnimatedVisibility(isBottomNavVisible, enter = fadeIn(snap()), exit = fadeOut(snap())) {
                BottomNavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    items = bottomNavItems,
                    selectedIndex = selectedTopLevelIndex,
                ) { clickedItem ->
                    val targetRoute = TOP_LEVEL_ROUTES.entries
                        .firstOrNull { it.value === clickedItem }?.key ?: return@BottomNavigationBar
                    navigator.navigate(targetRoute)
                }
            }
        }
    }
}

// Maps each route to its screen. Every `entry<X>` wires the screen's navigation callbacks to the
// Navigator; ViewModels are obtained per-entry via koinViewModel<…>().
private fun EntryProviderScope<ScreenRoute>.screens(navigator: Navigator) {
    // ── Top-level (no animation when switching tabs) ────────────────────────────

    entry<HomeScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<HomeViewModel>()
        HomeScreen(
            viewModel = viewModel,
            onPremiumRequired = { navigator.navigate(PaywallScreenRoute()) },
            onMoreCreditsNeeded = { navigator.navigate(CreditBalanceScreenRoute) },
            onAuthRequired = { navigator.navigate(SignInScreenRoute()) },
            onGenerationResult = { generationOutput ->
                navigator.navigate(GenerationResultScreenRoute(id = generationOutput.id))
            },
        )
    }

    entry<GalleryScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<GalleryViewModel>()
        GalleryScreen(
            viewModel = viewModel,
            onNavigateToResult = { id -> navigator.navigate(GenerationResultScreenRoute(id = id)) },
            onNavigateToHome = { navigator.navigate(HomeScreenRoute) },
        )
    }

    entry<AccountScreenRoute>(metadata = noAnimationMetadata) {
        val viewModel = koinViewModel<AccountViewModel>()
        AccountScreen(
            viewModel = viewModel,
            onNavigateHelpAndSupport = { navigator.navigate(HelpAndSupportScreenRoute) },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
            onNavigateSignIn = { navigator.navigate(SignInScreenRoute()) },
            onNavigateProfile = { navigator.navigate(ProfileScreenRoute) },
            onNavigateSubscriptions = { navigator.navigate(SubscriptionsScreenRoute) },
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
                if (completion.isNewUser && AppConfiguration.PREMIUM_FEATURES_ENABLED) {
                    navigator.navigate(
                        PaywallScreenRoute(placementId = Constants.PAYWALL_PLACEMENT_ONBOARDING),
                    )
                }
            },
        )
    }

    entry<ProfileScreenRoute> {
        val viewModel = koinViewModel<ProfileViewModel>()
        ProfileScreen(
            viewModel = viewModel,
            onSignInRequired = { navigator.replace(SignInScreenRoute()) },
            onNavigateToBack = { navigator.goBack() },
        )
    }

    entry<SignInScreenRoute> { key ->
        SignInScreen(
            isSignIn = key.isSignIn,
            onSuccessfulSignIn = { navigator.goBack() },
            onNavigateBack = { navigator.goBack() },
        )
    }

    entry<SubscriptionsScreenRoute> {
        val viewModel = koinViewModel<SubscriptionsViewModel>()
        SubscriptionsScreen(
            viewModel = viewModel,
            onClickBack = { navigator.goBack() },
            onNavigatePaywall = { navigator.navigate(PaywallScreenRoute()) },
        )
    }

    entry<HelpAndSupportScreenRoute> {
        HelpAndSupportScreen(onNavigateBack = { navigator.goBack() })
    }

    entry<CreditBalanceScreenRoute> {
        val viewModel = koinViewModel<CreditBalanceViewModel>()
        CreditBalanceScreen(
            viewModel = viewModel,
            onPurchaseRequired = { placementId ->
                navigator.navigate(PaywallScreenRoute(placementId = placementId))
            },
            onClickBack = { navigator.goBack() },
        )
    }

    entry<GenerationResultScreenRoute> { key ->
        val viewModel =
            koinViewModel<GenerationResultViewModel>(parameters = { parametersOf(key.id) })
        GenerationResultScreen(
            viewModel = viewModel,
            onNavigateToBack = { navigator.goBack() },
        )
    }

    // SHOW_REMOTE_PAYWALL flag picks the provider's hosted paywall UI vs. the app's custom one.
    entry<PaywallScreenRoute> { key ->
        val featureFlagManager = koinInject<FeatureFlagManager>()
        val viewModel = koinViewModel<PaywallViewModel>(
            parameters = { parametersOf(key.placementId) },
        )
        if (featureFlagManager.getBoolean(FeatureFlagManager.Keys.SHOW_REMOTE_PAYWALL)) {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                RemotePaywallScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        viewModel.onPaywallDismissActionHandled()
                        navigator.goBack()
                    },
                    onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
                )
            }
        } else {
            PaywallScreen(
                viewModel = viewModel,
                onDismiss = {
                    viewModel.onPaywallDismissActionHandled()
                    navigator.goBack()
                },
                onSignInRequired = { navigator.navigate(SignInScreenRoute()) },
            )
        }
    }

    entry<ProfileSetupScreenRoute> { key ->
        val viewModel = koinViewModel<ProfileSetupViewModel>()
        ProfileSetupScreen(
            viewModel = viewModel,
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
            onUpdateProfile = { navigator.navigate(ProfileSetupScreenRoute()) },
        )
    }

    entry<PackInspectorScreenRoute> {
        val viewModel = koinViewModel<PackInspectorViewModel>()
        PackInspectorScreen(
            viewModel = viewModel,
            onNavigateBack = { navigator.goBack() },
        )
    }

    // Add new screen entries below — generate_screen.sh inserts here.
}
