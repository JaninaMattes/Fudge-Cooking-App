package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RecipeRequest(
    private val userId: Int,
    private val amount: Int = 7
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RecipeRequest

        if (userId != other.userId) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

    override fun toString(): String {
        return "RecipeRequest( userId='$userId', amount='$amount')"
    }
}
