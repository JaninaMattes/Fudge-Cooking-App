package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import com.mobilesystems.feedme.domain.model.Product
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShoppingListRequest(
    private val userId: Int,
    private val shoppingList: List<Product>?
): Parcelable{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShoppingListRequest

        if (userId != other.userId) return false
        if (shoppingList != other.shoppingList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + shoppingList.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoginRequest( userId='$userId', shoppingList='$shoppingList')"
    }
}
