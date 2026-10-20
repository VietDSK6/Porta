package io.github.vietdsk6.porta.subscription.revenuecat

import io.github.vietdsk6.porta.subscription.api.GrantedAccess
import io.github.vietdsk6.porta.subscription.api.SubscriptionProviderUser
import com.revenuecat.purchases.kmp.models.CustomerInfo

internal fun CustomerInfo.asSubscriptionProviderUser(): SubscriptionProviderUser {
    val grantedAccesses =
        entitlements.active.values.associate { entitlement ->
            entitlement.identifier to
                GrantedAccess(
                    id = entitlement.identifier,
                    expirationDateMillis = entitlement.expirationDateMillis,
                    isLifetime = entitlement.expirationDateMillis == null && entitlement.willRenew,
                    willRenew = entitlement.willRenew,
                    productIdentifier = entitlement.productIdentifier,
                )
        }

    return SubscriptionProviderUser(
        grantedAccesses = grantedAccesses,
        activeSubscriptionIds = activeSubscriptions,
    )
}
