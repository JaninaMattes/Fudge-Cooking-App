package com.mobilesystems.feedme

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.*
import com.mobilesystems.feedme.common.backgroundService.*
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The Hilt Application class is used by hilt to setup its own classes that can use dependency injection,
 * the annotation lets hilt attach itself to the application lifecycle.
 */

@HiltAndroidApp
class HiltApplication : Application(), Configuration.Provider{

    @Inject
    lateinit var myPushWorkerFactory: PushWorkerFactory
    @Inject
    lateinit var myRecipeWorkerFactory: RecipeWorkerFactory

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    //configure WorkManager
    override fun getWorkManagerConfiguration(): Configuration {
        val myWorkerFactory = DelegatingWorkerFactory()
        myWorkerFactory.addFactory(myRecipeWorkerFactory)
        myWorkerFactory.addFactory(myPushWorkerFactory)

        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(myWorkerFactory)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        //WorkManager
        val context = applicationContext
        delayedInit(context)
    }

    private fun delayedInit(context: Context) {
        applicationScope.launch {
            setupWork(context)
        }
    }

    private fun setupWork(context: Context){
        //set constraints: Network connection
        val constraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        //WorkManager for pushNotification

        val pushWorkRequest =
            PeriodicWorkRequestBuilder<PushWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork("Push Work", ExistingPeriodicWorkPolicy.KEEP, pushWorkRequest)

        //WorkManager for Recipes

        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        // Set Execution time
        dueDate.set(Calendar.HOUR_OF_DAY, 7)
        dueDate.set(Calendar.MINUTE, 3)
        dueDate.set(Calendar.SECOND, 0)
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        val recipeWorkRequest =
            OneTimeWorkRequestBuilder<RecipeWorker>()
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("recipeWork", ExistingWorkPolicy.KEEP, recipeWorkRequest)
    }

}