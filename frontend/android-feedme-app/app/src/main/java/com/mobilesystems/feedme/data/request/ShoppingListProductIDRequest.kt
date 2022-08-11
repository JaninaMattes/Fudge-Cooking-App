package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShoppingListProductIDRequest(
    private val userId: Int,
    private val productId: Int
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShoppingListProductIDRequest

        if (userId != other.userId) return false
        if (productId != other.productId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + productId.hashCode()

        return result
    }

    override fun toString(): String {
        return "ShoppingListDeleteProductRequest( userId='$userId', id='$productId')"
    }
}
