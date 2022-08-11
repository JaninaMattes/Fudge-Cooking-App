package com.mobilesystems.feedme.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OldShoppingList(
    override val shoppingListId: Int,
    override val shoppingList: List<Product>? = null,
) : ShoppingList(shoppingListId, shoppingList), Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OldShoppingList

        if (shoppingListId != other.shoppingListId) return false
        if (shoppingList != other.shoppingList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shoppingListId
        result = 31 * result + (shoppingList?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "OldShoppingList(shoppingListId=$shoppingListId, shoppingListProducts=$shoppingList)"
    }

}
