package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ChangeLoginStatusRequest(
    private val userId: Int,
    private val loginStatus: Int
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChangeLoginStatusRequest

        if (userId != other.userId) return false
        if (loginStatus != other.loginStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + loginStatus.hashCode()
        return result
    }

    override fun toString(): String {
        return "ChangeLoginStatusRequest(userId=$userId, loginStatus=$loginStatus)"
    }

}
