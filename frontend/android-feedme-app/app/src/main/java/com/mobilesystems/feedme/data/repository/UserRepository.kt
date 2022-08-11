package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.data.response.UserResponse
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.User

interface UserRepository {

    suspend fun getLoggedInUser(userId: Int): User?

    suspend fun preFetchLoggedInUser(userId: Int): UserResponse?

    suspend fun updateLoggedInUser(user: User)

    suspend fun updateUserImage(userId: Int, image: Image): Int

    suspend fun createDietryTag(user: User)

    suspend fun isUserLoggedIn(userId: Int): Boolean

    suspend fun deleteUser(userId: Int)

    suspend fun allowPushNotification(userId: Int, allowSetting: Boolean)

    suspend fun allowReminder(userId: Int, allowSetting: Boolean)

    suspend fun allowSuggestion(userId: Int, allowSetting: Boolean)

}