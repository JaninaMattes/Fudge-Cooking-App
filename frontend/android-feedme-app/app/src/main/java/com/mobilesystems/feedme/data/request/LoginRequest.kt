package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Parcelable data class to send register request in POST request object body.
 * https://developer.android.com/kotlin/parcelize
 */
@Parcelize
data class LoginRequest(
    private val email: String,
    private val password: String
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LoginRequest

        if (email != other.email) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int {
        var result = email.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }

    override fun toString(): String {
        return "LoginRequest( email='$email', password='$password')"
    }
}
