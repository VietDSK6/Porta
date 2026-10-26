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
@SerialName("OnBoarding")
data object OnBoardingScreenRoute : ScreenRoute

@Serializable
@SerialName("ProfileSetup")
data class ProfileSetupScreenRoute(
    val returnToProfile: Boolean = false,
    val requestedFactKeyName: String? = null,
    val requestContext: String? = null,
) : ScreenRoute

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

@Serializable
@SerialName("ActualProfileUpdate")
data object ActualProfileUpdateScreenRoute : ScreenRoute

// Add new routes below — generate_screen.sh inserts here.
