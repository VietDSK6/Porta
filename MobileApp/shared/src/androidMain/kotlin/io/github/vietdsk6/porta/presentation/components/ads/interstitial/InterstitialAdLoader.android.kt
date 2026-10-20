package io.github.vietdsk6.porta.presentation.components.ads.interstitial

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.github.vietdsk6.porta.presentation.components.ads.AdsConfig
import io.github.vietdsk6.porta.presentation.components.ads.FullScreenAdLoader
import io.github.vietdsk6.porta.util.logging.AppLogger

class InterstitialAdLoader(private val context: Context) : FullScreenAdLoader {
    var interstitialAd: InterstitialAd? = null

    override fun load() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdsConfig.getInterstitialAdId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(p0: InterstitialAd) {
                    super.onAdLoaded(p0)
                    AppLogger.d("Interstitial ad is loaded")
                    interstitialAd = p0
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    AppLogger.e("Error loading interstitial ad: ${p0.message}")
                    interstitialAd = null
                }
            },
        )
    }
}
