package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDietryTagRequest(
    private val userId: Int,
    private val dietryTag: ProductTagRequest?,
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDietryTagRequest

        if (userId != other.userId) return false
        if (dietryTag != other.dietryTag) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + dietryTag.hashCode()
        return result
    }

    override fun toString(): String {
        return "UserDietryTagRequest (userId=$userId, dietryTag='$dietryTag')"
    }

}
