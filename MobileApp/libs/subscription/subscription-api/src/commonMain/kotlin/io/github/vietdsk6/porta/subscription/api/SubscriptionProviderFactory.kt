package io.github.vietdsk6.porta.subscription.api

interface SubscriptionProviderFactory {
    companion object

    fun createProvider(): SubscriptionProvider

    fun createProviderUi(): SubscriptionProviderUi
}
