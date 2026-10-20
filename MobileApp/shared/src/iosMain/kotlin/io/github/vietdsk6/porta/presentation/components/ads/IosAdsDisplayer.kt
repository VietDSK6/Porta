package io.github.vietdsk6.porta.presentation.components.ads

interface IosAdsDisplayer {

    fun provideInterstitialAdDisplayer(adLoader: FullScreenAdLoader): FullScreenAdDisplayer
    fun provideRewardedAdDisplayer(
        adLoader: FullScreenAdLoader,
        onRewarded: (AdsRewardItem) -> Unit,
    ): FullScreenAdDisplayer
}
