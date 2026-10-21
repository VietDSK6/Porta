package io.github.vietdsk6.porta.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every navigation destination in the app.
 *
 * `@SerialName` values are the wire format on disk — keep them stable when
 * renaming routes.
 */
@Serializable
sealed interface ScreenRoute : NavKey

/** Marker for routes that root a top-level back stack (a tab in the bottom nav). */
@Serializable
sealed interface TopLevelScreenRoute : ScreenRoute

@Serializable
@SerialName("Home")
data object HomeScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Gallery")
data object GalleryScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("Account")
data object AccountScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("OnBoarding")
data object OnBoardingScreenRoute : ScreenRoute

@Serializable
@SerialName("Profile")
data object ProfileScreenRoute : ScreenRoute

@Serializable
@SerialName("SignIn")
data class SignInScreenRoute(val isSignIn: Boolean = false) : ScreenRoute

@Serializable
@SerialName("Subscriptions")
data object SubscriptionsScreenRoute : ScreenRoute

@Serializable
@SerialName("HelpAndSupport")
data object HelpAndSupportScreenRoute : ScreenRoute

@Serializable
@SerialName("CreditBalance")
data object CreditBalanceScreenRoute : ScreenRoute

@Serializable
@SerialName("GenerationResult")
data class GenerationResultScreenRoute(val id: String) : ScreenRoute

@Serializable
@SerialName("Paywall")
data class PaywallScreenRoute(val placementId: String? = null) : ScreenRoute

@Serializable
@SerialName("ProfileSetup")
data class ProfileSetupScreenRoute(val returnToProfile: Boolean = false) : ScreenRoute

@Serializable
@SerialName("Doors")
data object DoorsScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("OpportunityDetail")
data class OpportunityDetailScreenRoute(val opportunityId: String) : ScreenRoute

@Serializable
@SerialName("Frontier")
data object FrontierScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("ScenarioPreview")
data class ScenarioPreviewScreenRoute(val actionId: String) : ScreenRoute

@Serializable
@SerialName("ActivePlan")
data object ActivePlanScreenRoute : ScreenRoute

@Serializable
@SerialName("StudentProfile")
data object StudentProfileScreenRoute : TopLevelScreenRoute

@Serializable
@SerialName("PackInspector")
data object PackInspectorScreenRoute : ScreenRoute

// Add new routes below — generate_screen.sh inserts here.
