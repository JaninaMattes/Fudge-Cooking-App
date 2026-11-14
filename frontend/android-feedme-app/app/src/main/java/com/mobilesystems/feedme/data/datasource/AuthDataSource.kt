package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.request.ChangeLoginStatusRequest

interface AuthDataSource {
    suspend fun logout(request: ChangeLoginStatusRequest): Resource<Int?>

    suspend fun updateLogin(request: ChangeLoginStatusRequest): Resource<Int?>

    suspend fun isUserLoggedIn(userId: Int): Resource<Boolean>
}
