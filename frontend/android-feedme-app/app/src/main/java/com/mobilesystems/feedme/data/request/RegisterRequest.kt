package com.mobilesystems.feedme.data.request

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Parcelable data class to send register request in POST request object body.
 * https://developer.android.com/kotlin/parcelize
 */
@Parcelize
data class RegisterRequest(
    private val firstName: String,
    private val lastName: String,
    private val email: String,
    private val password: String
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterRequest

        if (firstName != other.firstName) return false
        if (lastName != other.lastName) return false
        if (email != other.email) return false
        if (password != other.password) return false

        return true
    }

    override fun hashCode(): Int {
        var result = firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        return result
    }

    override fun toString(): String {
        return "RegisterRequest(firstName='$firstName', lastName='$lastName', email='$email', password='$password')"
    }


}
