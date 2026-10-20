package io.github.vietdsk6.porta.presentation.components.premium

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.github.vietdsk6.porta.designsystem.components.premium.PremiumFeatureUiState
import io.github.vietdsk6.porta.designsystem.components.premium.SuccessfulPurchaseContent
import io.github.vietdsk6.porta.util.Constants.subscriptionUrl
import io.github.vietdsk6.porta.util.inappreview.rememberInAppReviewTrigger
import io.github.vietdsk6.porta.util.logging.AppLogger
import io.github.vietdsk6.porta.util.logging.logSuccessfulPurchase

@Composable
fun SuccessfulPurchaseView(
    features: List<PremiumFeatureUiState> = emptyList(),
    expirationDate: String? = null,
    isLifetime: Boolean = false,
    isRecurring: Boolean = true,
    modifier: Modifier = Modifier,
    onContinue: () -> Unit = {},
) {
    val inAppReviewTrigger = rememberInAppReviewTrigger()
    LaunchedEffect(Unit) {
        AppLogger.logSuccessfulPurchase()
        inAppReviewTrigger.triggerAfterSuccessfulPurchase()
    }

    SuccessfulPurchaseContent(
        subscriptionUrl = subscriptionUrl,
        features = features,
        expirationDate = expirationDate,
        isLifetime = isLifetime,
        isRecurring = isRecurring,
        modifier = modifier,
        onContinue = onContinue,
    )
}
