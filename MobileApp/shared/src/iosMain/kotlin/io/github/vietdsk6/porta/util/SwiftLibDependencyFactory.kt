package io.github.vietdsk6.porta.util

import io.github.vietdsk6.porta.data.source.featureflag.FeatureFlagManager
import io.github.vietdsk6.porta.presentation.components.ads.AdsManager
import io.github.vietdsk6.porta.presentation.components.ads.IosAdsDisplayer
import io.github.vietdsk6.porta.util.analytics.Analytics

/**
This factory is used to help to use swift libraries in KMP. Actual implementations are provided in swift.
 */
interface SwiftLibDependencyFactory {
    fun provideFeatureFlagManagerImpl(): FeatureFlagManager
    fun provideFirebaseAnalyticsImpl(): Analytics
    fun provideAdsManagerImpl(): AdsManager
    fun provideIosAdsDisplayer(): IosAdsDisplayer
}
