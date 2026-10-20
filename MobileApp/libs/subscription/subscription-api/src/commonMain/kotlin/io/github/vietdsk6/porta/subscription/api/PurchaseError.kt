package io.github.vietdsk6.porta.subscription.api

data class PurchaseError(
    val message: String,
    val code: String? = null,
)
