package com.mobilesystems.feedme.data.repository

import android.util.Log
import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.datasource.AuthDataSourceImpl
import com.mobilesystems.feedme.data.request.LoginRequest
import com.mobilesystems.feedme.data.request.ChangeLoginStatusRequest
import com.mobilesystems.feedme.data.request.RegisterRequest
import com.mobilesystems.feedme.domain.model.User
import javax.inject.Inject

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
class AuthRepositoryImpl @Inject constructor(
    private val dataSourceImpl: AuthDataSourceImpl) : AuthRepository{

    // in-memory cache of the loggedInUser object
    var user: User? = null
        private set

    var token: String? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null
    }

    override suspend fun login(email: String, password: String) : Resource<Map<String, String>> {
        // handle login
        val request = LoginRequest(email, password)
        val result = dataSourceImpl.login(request)
        setLoggedInUserToken(result.data?.get("token"))
        return result
    }

    override suspend fun register(firstName: String, lastName: String, email: String, password: String) : Resource<Map<String, String>> {
        // handle register
        val request = RegisterRequest(firstName, lastName, email, password)
        val result = dataSourceImpl.register(request)
        setLoggedInUserToken(result.data?.get("token"))
        return result
    }

    override suspend fun logout(userId: Int) {
        val loginStatus = 0
        val request = ChangeLoginStatusRequest(userId, loginStatus)
        dataSourceImpl.logout(request)
        Log.d("LoginStatusRequest", request.toString())
        this.user = null
    }

    override suspend fun updateLogin(userId: Int) {
        val request = ChangeLoginStatusRequest(userId, 1)
        Log.d("LoginStatusRequest", request.toString())
        dataSourceImpl.updateLogin(request)
    }

    override suspend fun isUserLoggedIn(userId: Int): Boolean {
        var isUserLoggedIn = false
        if(userId != 0){
            val result = dataSourceImpl.isUserLoggedIn(userId)
            isUserLoggedIn = result.data == true
        }
        return isUserLoggedIn
    }

    private fun setLoggedInUserToken(token: String?) {
        this.token = token
    }
}