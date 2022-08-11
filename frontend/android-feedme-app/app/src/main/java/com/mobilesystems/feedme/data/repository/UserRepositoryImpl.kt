package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.data.datasource.UserDataSourceImpl
import com.mobilesystems.feedme.data.response.UserResponse
import com.mobilesystems.feedme.domain.model.Image
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.common.utils.*
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val dataSourceImpl: UserDataSourceImpl)
    : UserRepository {

    override suspend fun preFetchLoggedInUser(userId: Int): UserResponse?{
        // get currently logged in user
        return dataSourceImpl.getUserById(userId).data
    }

    override suspend fun getLoggedInUser(userId: Int): User? {
        // get currently logged in user
        val result = dataSourceImpl.getUserById(userId)
        // convert userResponse object to user object
        Log.d("User Repository", "Load user $result")
        val user = convertUserResponse(result.data)
        Log.d("User Repository", "Konvertierter User $user")
        return user
    }

    override suspend fun updateLoggedInUser(user: User) {
        val request = convertUpdateUserRequest(user)
        if(request != null){
            // update current user
            val result = dataSourceImpl.updateUserById(request)
            Log.d("User Repository", "Update user $result")
        }
    }

    override suspend fun updateUserImage(userId: Int, image: Image): Int {
        var result = 0
        val request = convertUpdateImageRequest(userId, image)
        if(request != null){
            // update current user
            Log.d("UserRepository", "Update image $image.")
            val res = dataSourceImpl.updateUserImageById(request)
            if(res.data != null) {
                result = res.data.imageId
            }
            Log.d("User Repository", "Update user image $result")
        }
        return result
    }

    override suspend fun isUserLoggedIn(userId: Int): Boolean {
        val result = dataSourceImpl.isUserLoggedIn(userId)
        Log.d("User Repository", "Get is user logged in, $result")
        return result.data ?: false
    }

    override suspend fun deleteUser(userId: Int) {
        dataSourceImpl.deleteUserById(userId)
    }

    override suspend fun allowPushNotification(userId: Int, allowSetting: Boolean){
        val request = convertAllowSettingRequest(userId, allowSetting)
        dataSourceImpl.allowPushNotification(request)
    }

    override suspend fun allowReminder(userId: Int, allowSetting: Boolean){
        val request = convertAllowSettingRequest(userId, allowSetting)
        dataSourceImpl.allowReminder(request)
    }

    override suspend fun allowSuggestion(userId: Int, allowSetting: Boolean){
        val request = convertAllowSettingRequest(userId, allowSetting)
        dataSourceImpl.allowSuggestion(request)
    }

    override suspend fun createDietryTag(user: User){
        val request = convertUserDietryTagRequest(user)
        if(request != null){
            dataSourceImpl.createDietryTag(request)
        }
    }
}