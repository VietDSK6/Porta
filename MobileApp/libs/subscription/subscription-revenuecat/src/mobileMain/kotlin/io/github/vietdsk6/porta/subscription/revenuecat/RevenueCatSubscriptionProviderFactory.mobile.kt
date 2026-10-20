package io.github.vietdsk6.porta.subscription.revenuecat

import io.github.vietdsk6.porta.subscription.api.SubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderFactory
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = RevenueCatSubscriptionProvider()

            override fun createProviderUi(): SubscriptionProviderUi = RevenueCatSubscriptionProviderUi()
        }
