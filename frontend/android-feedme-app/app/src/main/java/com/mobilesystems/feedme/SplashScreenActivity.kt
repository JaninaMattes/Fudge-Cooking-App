package com.mobilesystems.feedme

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.mobilesystems.feedme.ui.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The purpose of the Splash Screen is to display a screen while the application
 * fetches the relevant content if any (from network calls/database).
 *
 * Tutorials: https://medium.com/geekculture/implementing-the-perfect-splash-screen-in-android-295de045a8dc
 *            https://developer.android.com/guide/topics/ui/splash-screen
 */

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    // lazy delegate
    private val splashViewModel: SplashViewModel by viewModels()

    private val activityScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityScope.launch {
            // load if user is logged in ...
            observeSplashLiveData()
            preFetchUserData()
        }
    }

    override fun onPause() {
        // Destroy splash screen
        super.onPause()
        activityScope.cancel()
    }

    private fun preFetchUserData() {
        splashViewModel.loadLoggedInUserData()
    }

    private fun observeSplashLiveData() {
        splashViewModel.loadIsUserLoggedIn()

        val loginObserver =
            Observer<Boolean?> { isLoggedIn ->
                // The value is null until the server answers. The user then stays
                // on the screen of the start.
                if (isLoggedIn == true) {
                    // navigate to main
                    val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // navigate to login
                    val intent = Intent(this@SplashScreenActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        // observe the live data
        splashViewModel.isUserLoggedInResult.observe(this@SplashScreenActivity, loginObserver)
    }
}
