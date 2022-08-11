package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UpdateUserRequest(
    private val firstName: String,
    private val lastName: String,
    private val email: String,
    private val userId: Int
): Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateUserRequest

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (userId != other.userId) return false


        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }

    override fun toString(): String {
        return "UpdateUserRequest (firstName='$firstName', lastName='$lastName', " +
                "email='$email', userId=$userId)"
    }
}
