package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.domain.model.User

interface AuthRepository {

    suspend fun login(email: String, password: String) : Resource<Map<String, String>>

    suspend fun register(firstName: String, lastName: String, email: String, password: String) : Resource<Map<String, String>>

    suspend fun logout(userId: Int)

    suspend fun updateLogin(userId: Int)

    suspend fun isUserLoggedIn(userId: Int): Boolean
}