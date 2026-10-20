package io.github.vietdsk6.porta.presentation.components.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import io.github.vietdsk6.porta.presentation.components.ads.interstitial.InterstitialAdLoader
import io.github.vietdsk6.porta.presentation.components.ads.rewarded.RewardedAdLoader

class AdsManagerImpl(private val context: Context) : AdsManager {
    override fun initialize() {
        MobileAds.initialize(context)
    }

    override val interstitialAdLoader: FullScreenAdLoader by lazy { InterstitialAdLoader(context) }
    override val rewardedAdLoader: FullScreenAdLoader by lazy { RewardedAdLoader(context) }
}
