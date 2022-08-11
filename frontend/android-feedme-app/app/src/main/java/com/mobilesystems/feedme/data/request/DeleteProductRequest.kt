package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DeleteProductRequest(
    private val userId: Int,
    private val productId: Int,
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeleteProductRequest

        if (productId != other.productId) return false
        if (userId != other.userId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = productId
        result = 31 * result + userId
        return result
    }

    override fun toString(): String {
        return "DeleteProductRequest(productId=$productId, userId=$userId)"
    }

}
