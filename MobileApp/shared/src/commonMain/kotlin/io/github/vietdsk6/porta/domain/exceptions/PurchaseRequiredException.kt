package io.github.vietdsk6.porta.domain.exceptions

/** Thrown when an action requires premium. Presentation catches it to open the subscription paywall. */
class PurchaseRequiredException : Exception("You need to have a premium subscription to make this operation")
