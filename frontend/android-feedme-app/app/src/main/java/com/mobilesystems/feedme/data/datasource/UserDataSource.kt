package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.request.ImageRequest
import com.mobilesystems.feedme.data.request.UpdateUserRequest
import com.mobilesystems.feedme.data.request.UserAllowSettingsRequest
import com.mobilesystems.feedme.data.request.UserDietryTagRequest
import com.mobilesystems.feedme.data.response.DietryTagResponse
import com.mobilesystems.feedme.data.response.ImageIdResponse
import com.mobilesystems.feedme.data.response.UserIdResponse
import com.mobilesystems.feedme.data.response.UserResponse

interface UserDataSource {

    suspend fun createDietryTag(request: UserDietryTagRequest): Resource<DietryTagResponse>

    suspend fun getUserById(userId: Int) : Resource<UserResponse?>

    suspend fun updateUserById(request: UpdateUserRequest) : Resource<UserIdResponse>

    suspend  fun updateUserImageById(request: ImageRequest): Resource<ImageIdResponse>

    suspend fun deleteUserById(userId: Int): Resource<UserIdResponse>

    suspend fun isUserLoggedIn(userId: Int): Resource<Boolean>

    suspend fun allowPushNotification(allowSettings: UserAllowSettingsRequest): Resource<Int>

    suspend fun allowReminder(allowSettings: UserAllowSettingsRequest): Resource<Int>

    suspend fun allowSuggestion(allowSettings: UserAllowSettingsRequest): Resource<Int>

}