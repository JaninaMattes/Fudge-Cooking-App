package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.request.ImageRequest
import com.mobilesystems.feedme.data.request.UpdateUserRequest
import com.mobilesystems.feedme.data.request.UserAllowSettingsRequest
import com.mobilesystems.feedme.data.request.UserDietryTagRequest
import com.mobilesystems.feedme.data.response.UserResponse
import com.mobilesystems.feedme.di.AppModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BaseDataSource(), UserDataSource {

    override suspend fun getUserById(userId: Int): Resource<UserResponse> = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getUserById(userId) }
    }

    override suspend fun updateUserById(request: UpdateUserRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.updateUser(request) }
    }

    override suspend fun updateUserImageById(request: ImageRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.updateUserImage(request) }
    }

    override suspend fun deleteUserById(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.deleteUser(userId) }
    }

    override suspend fun isUserLoggedIn(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getIsUserLoggedIn(userId) }
    }

    override suspend fun allowPushNotification(allowSettings: UserAllowSettingsRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.allowPushNotification(allowSettings) }
    }

    override suspend fun allowReminder(allowSettings: UserAllowSettingsRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.allowReminder(allowSettings) }
    }

    override suspend fun allowSuggestion(allowSettings: UserAllowSettingsRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.allowSuggestion(allowSettings) }
    }

    override suspend fun createDietryTag(request: UserDietryTagRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.createDietryTag(request) }
    }
}