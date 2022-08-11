package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.request.LoginRequest
import com.mobilesystems.feedme.data.request.ChangeLoginStatusRequest
import com.mobilesystems.feedme.data.request.RegisterRequest
import com.mobilesystems.feedme.di.AppModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 *
 * Tutorial Android Coroutines: https://developer.android.com/kotlin/coroutines/coroutines-best-practices
 */
class AuthDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : BaseDataSource(), AuthDataSource {

    /**
     * As this operation is manually retrieving the news from the server
     * using a blocking HttpURLConnection, it needs to move the execution
     * to an IO dispatcher to make it main-safe.
     */
    suspend fun login(request: LoginRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.loginUser(request) }
    }

    suspend fun register(request: RegisterRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.registerUser(request) }
    }

    override suspend fun logout(request: ChangeLoginStatusRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.logoutUser(request) }
    }

    override suspend fun updateLogin(request: ChangeLoginStatusRequest) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.putLoginUser(request) }
    }

    override suspend fun isUserLoggedIn(userId: Int): Resource<Boolean> = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getIsUserLoggedIn(userId) }
    }
}