package io.github.vietdsk6.porta.subscription.adapty

import io.github.vietdsk6.porta.subscription.api.NoOpSubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.NoOpSubscriptionProviderUi
import io.github.vietdsk6.porta.subscription.api.SubscriptionProvider
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderFactory
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderUi

internal actual val subscriptionProviderFactory: SubscriptionProviderFactory
    get() =
        object : SubscriptionProviderFactory {
            override fun createProvider(): SubscriptionProvider = NoOpSubscriptionProvider

            override fun createProviderUi(): SubscriptionProviderUi = NoOpSubscriptionProviderUi
        }
