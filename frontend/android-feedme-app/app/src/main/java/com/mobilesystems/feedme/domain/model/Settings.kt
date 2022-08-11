package com.mobilesystems.feedme.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Settings(
    val reminderProductExp: Boolean? = null,
    val allowPushNotifications: Boolean? = null,
    val suggestRecipes: Boolean? = null,
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Settings

        if (reminderProductExp != other.reminderProductExp) return false
        if (allowPushNotifications != other.allowPushNotifications) return false
        if (suggestRecipes != other.suggestRecipes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reminderProductExp?.hashCode() ?: 0
        result = 31 * result + (allowPushNotifications?.hashCode() ?: 0)
        result = 31 * result + (suggestRecipes?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Settings(reminderProductExp=$reminderProductExp, " +
                "allowPushNotifications=$allowPushNotifications, suggestRecipes=$suggestRecipes)"
    }


}