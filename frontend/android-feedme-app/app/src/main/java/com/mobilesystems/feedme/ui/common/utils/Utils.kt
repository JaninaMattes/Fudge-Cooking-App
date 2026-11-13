package com.mobilesystems.feedme.ui.common.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.mobilesystems.feedme.common.logging.logError
import com.mobilesystems.feedme.data.response.UserResponse
import com.mobilesystems.feedme.domain.model.LoggedInUser
import org.json.JSONObject
import java.util.Base64

/**
 * All utility functions that are commonly used.
 */

fun convertTokenToUser(
    context: Context,
    jwt: String?,
): LoggedInUser? {
    var user: LoggedInUser? = null
    try {
        if (jwt != null) {
            val decoded = decodeJWTToken(jwt)
            Log.d("Decoded token", decoded)
            val jsonObj = JSONObject(decoded)
            user =
                LoggedInUser(
                    userId = jsonObj.get("userId") as Int,
                    firstName = jsonObj.get("firstName") as String,
                    lastName = jsonObj.get("lastName") as String,
                    email = jsonObj.get("email") as String,
                )
            saveTokenToSharedPreference(context, jwt)
            saveLoggedInUserToSharedPreference(context, user)
        }
    } catch (e: Exception) {
        logError("convert token to user", e)
    }
    return user
}

// Save objects to shared preferences
fun saveObjectToSharedPreference(
    context: Context,
    preferenceFileName: String?,
    serializedObjectKey: String?,
    value: Any?,
) {
    try {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        val gson = Gson()
        val serializedObject = gson.toJson(value)
        sharedPreferencesEditor.putString(serializedObjectKey, serializedObject)
        sharedPreferencesEditor.apply()
        Log.d("SharedPreferences", "Saved $value to shared preferences!")
    } catch (e: Exception) {
        logError("save object to shared preference", e)
    }
}

// Remove object from shared preferences
fun removeObjectFromSharedPreference(
    context: Context,
    preferenceFileName: String?,
    serializedObjectKey: String?,
    value: Any?,
) {
    try {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.remove(serializedObjectKey)
        sharedPreferencesEditor.apply()
        Log.d("SharedPreferences", "Removed $value from shared preferences!")
    } catch (e: Exception) {
        logError("remove object from shared preference", e)
    }
}

// Retrieve object from shared preferences
fun <GenericClass> getSavedObjectFromPreference(
    context: Context,
    preferenceFileName: String?,
    preferenceKey: String?,
    classType: Class<GenericClass>?,
): GenericClass? {
    try {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        if (sharedPreferences.contains(preferenceKey)) {
            val gson = Gson()
            Log.d("SharedPreferences", "Retrieved object from shared preferences!")
            return gson.fromJson(sharedPreferences.getString(preferenceKey, ""), classType)
        }
    } catch (e: Exception) {
        logError("remove object from shared preference", e)
    }
    return null
}

fun decodeJWTToken(jwt: String): String {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "Requires SDK 26"
    val parts = jwt.split(".")
    return try {
        val charset = charset("UTF-8")
        val header = String(Base64.getUrlDecoder().decode(parts[0].toByteArray(charset)), charset)
        val payload = String(Base64.getUrlDecoder().decode(parts[1].toByteArray(charset)), charset)
        header
        payload
    } catch (e: Exception) {
        "Error parsing JWT: $e"
    }
}

fun saveTokenToSharedPreference(
    context: Context,
    jwt: String,
) {
    try {
        saveObjectToSharedPreference(
            context,
            "mPreference",
            "jwtToken",
            jwt,
        )
    } catch (e: Exception) {
        logError("save token to shared preference", e)
    }
}

fun saveLoggedInUserToSharedPreference(
    context: Context,
    user: LoggedInUser,
) {
    try {
        saveObjectToSharedPreference(
            context,
            "mPreference",
            "loggedInUser",
            user,
        )
    } catch (e: Exception) {
        logError("save logged in user to shared preference", e)
    }
}

fun saveUserDataToSharedPreference(
    context: Context,
    user: UserResponse,
) {
    try {
        saveObjectToSharedPreference(
            context,
            "mPreference",
            "userData",
            user,
        )
    } catch (e: Exception) {
        logError("save user data to shared preference", e)
    }
}

fun removeLoggedInUserFromSharedPreferences(
    context: Context,
    user: LoggedInUser,
) {
    try {
        removeObjectFromSharedPreference(
            context,
            "mPreference",
            "loggedInUser",
            user,
        )
    } catch (e: Exception) {
        logError("remove logged in user from shared preferences", e)
    }
}

fun removeTokenFromSharedPreferences(
    context: Context,
    jwt: String,
) {
    try {
        removeObjectFromSharedPreference(
            context,
            "mPreference",
            "jwtToken",
            jwt,
        )
    } catch (e: Exception) {
        logError("remove token from shared preferences", e)
    }
}

fun removeDataFromSharedPreferences(
    context: Context,
    user: UserResponse,
) {
    try {
        removeObjectFromSharedPreference(
            context,
            "mPreference",
            "userData",
            user,
        )
    } catch (e: Exception) {
        logError("remove data from shared preferences", e)
    }
}

fun removeAllValuesFromSharedPreferences(
    context: Context,
    preferenceFileName: String = "mPreference",
) {
    try {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        val sharedPreferencesEditor = sharedPreferences.edit()
        sharedPreferencesEditor.clear()
        sharedPreferencesEditor.apply()
        Log.d("SharedPreferences", "All entries deleted!")
    } catch (e: Exception) {
        logError("remove all values from shared preferences", e)
    }
}

fun getLoggedInUser(context: Context): LoggedInUser? {
    var loggedInUser: LoggedInUser? = null
    try {
        // Retrieve current user id stored in preference
        loggedInUser =
            getSavedObjectFromPreference(
                context,
                "mPreference",
                "loggedInUser",
                LoggedInUser::class.java,
            )
        Log.d("SharedPreferences", "Retrieved user with id ${loggedInUser?.userId} from shared preferences!")
    } catch (e: Exception) {
        logError("get logged in user", e)
    }
    return loggedInUser
}

fun getUserDataFromSharedPreference(context: Context): UserResponse? {
    var userData: UserResponse? = null
    try {
        userData =
            getSavedObjectFromPreference(
                context,
                "mPreference",
                "userData",
                UserResponse::class.java,
            )
        Log.d("SharedPreferences", "Retrieved user with id ${userData?.userId} from shared preferences!")
    } catch (e: Exception) {
        logError("get user data from shared preference", e)
    }
    return userData
}

fun getJWTToken(context: Context): String? {
    // Retrieve current user id stored in preference
    val jwt =
        getSavedObjectFromPreference(
            context,
            "mPreference",
            "jwtToken",
            String::class.java,
        )
    Log.d("SharedPreferences", "Retrieved jwt token $jwt from shared preferences!")
    return jwt
}

fun doesPreferenceExist(
    context: Context,
    preferenceFileName: String?,
    preferenceKey: String?,
): Boolean {
    var exists: Boolean = false
    try {
        val sharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
        exists = sharedPreferences.contains(preferenceKey)
    } catch (e: Exception) {
        logError("does preference exist", e)
    }
    return exists
}

fun isPreferencesSet(
    context: Context,
    preferenceFileName: String?,
    preferenceKey: String?,
    numberOfPreferences: Int,
): Boolean {
    val sharedPreferences = context.getSharedPreferences(preferenceFileName, 0)
    return sharedPreferences.all.size == numberOfPreferences
}

fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
    Intent(this, activity).also {
        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(it)
    }
}
