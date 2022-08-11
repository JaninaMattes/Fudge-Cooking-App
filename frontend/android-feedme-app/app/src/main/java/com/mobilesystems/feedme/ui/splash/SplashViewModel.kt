package com.mobilesystems.feedme.ui.splash

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.mobilesystems.feedme.data.repository.AuthRepositoryImpl
import com.mobilesystems.feedme.data.repository.UserRepositoryImpl
import com.mobilesystems.feedme.ui.common.utils.*
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
                      androidApplication : Application,
                      private val loginRepository: AuthRepositoryImpl,
                      private val userRepository: UserRepositoryImpl):
    BaseViewModel(androidApplication), BaseSplashViewModel {

    private val _isUserLoggedInResult = MutableLiveData<Boolean?>()
    val isUserLoggedInResult: LiveData<Boolean?>
        get() = _isUserLoggedInResult

    private var _currentUserId = MutableLiveData<Int?>()
    val currentUserId : LiveData<Int?>
        get() = _currentUserId

    init{
        val context = getApplication<Application>().applicationContext
        // pre-fetch user from sharedPreferences
        getCurrentUserId(context)
    }

    override fun loadIsUserLoggedIn(){
        viewModelScope.launch {
            delay(1500)
            val userId = currentUserId.value
            // load user
            if(userId != null && userId != 0) {
                val result = loginRepository.isUserLoggedIn(userId)
                _isUserLoggedInResult.value = result
            }else{
                _isUserLoggedInResult.value = false
            }
        }
    }

    override fun loadLoggedInUserData() {
        viewModelScope.launch {
            // check if entry in preference already exists
            if(!doesPreferenceExist(context, "mPreference", "userData")){
                val userId = currentUserId.value
                if(userId != null && userId != 0) {
                    // load user data
                    val result = userRepository.preFetchLoggedInUser(userId)
                    if(result != null) {
                        // store only necessary pre-fetched data, omit password
                        val context = getApplication<Application>().applicationContext
                        saveUserDataToSharedPreference(context, extractNecessaryUserData(result))
                    }
                }
            }else{
                Log.d("SplashViewModel", "Preference already exists!")
            }
        }
    }

    private fun getCurrentUserId(context: Context): LiveData<Int?>{
        val result = getLoggedInUser(context)
        _currentUserId.value = result?.userId
        return currentUserId
    }
}