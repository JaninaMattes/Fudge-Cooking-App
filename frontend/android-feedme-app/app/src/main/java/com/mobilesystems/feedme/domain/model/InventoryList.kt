package com.mobilesystems.feedme.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InventoryList(
    val inventoryListId: Int,
    val inventoryList: List<Product>? = null
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InventoryList

        if (inventoryListId != other.inventoryListId) return false
        if (inventoryList != other.inventoryList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = inventoryListId
        result = 31 * result + (inventoryList?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "InventoryList(inventoryListId=$inventoryListId, inventoryList=$inventoryList)"
    }


}
