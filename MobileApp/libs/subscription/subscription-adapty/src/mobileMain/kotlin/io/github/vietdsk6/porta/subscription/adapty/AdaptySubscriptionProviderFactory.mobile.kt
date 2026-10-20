package io.github.vietdsk6.porta.subscription.adapty

import io.github.vietdsk6.porta.subscription.api.SubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderFactory
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = AdaptySubscriptionProvider()

            override fun createProviderUi(): SubscriptionProviderUi = AdaptySubscriptionProviderUi()
        }
