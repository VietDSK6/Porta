package io.github.vietdsk6.porta.util.inappreview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.StoreKit.SKStoreReviewController

@Composable
actual fun rememberInAppReviewManager(): InAppReviewManager = remember { InAppReviewManagerImpl() }

private class InAppReviewManagerImpl : InAppReviewManager {
    override fun requestReview() {
        SKStoreReviewController.requestReview()
    }
}
