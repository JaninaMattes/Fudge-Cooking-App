package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserAllowSettingsRequest(
    private val userId: Int,
    private val allow: Int
): Parcelable{

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAllowSettingsRequest

        if (userId != other.userId) return false
        if (allow != other.allow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + allow.hashCode()
        return result
    }

    override fun toString(): String {
        return "UserAllowSettingsRequest (userId=$userId, allow='$allow')"
    }
}
