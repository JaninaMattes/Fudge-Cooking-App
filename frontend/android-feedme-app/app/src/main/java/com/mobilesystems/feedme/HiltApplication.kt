package com.mobilesystems.feedme

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.DelegatingWorkerFactory
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mobilesystems.feedme.common.backgroundService.PushWorker
import com.mobilesystems.feedme.common.backgroundService.PushWorkerFactory
import com.mobilesystems.feedme.common.backgroundService.RecipeWorker
import com.mobilesystems.feedme.common.backgroundService.RecipeWorkerFactory
import com.mobilesystems.feedme.common.notification.NotificationSchedule
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * The Hilt Application class is used by hilt to setup its own classes that can use dependency injection,
 * the annotation lets hilt attach itself to the application lifecycle.
 */

@HiltAndroidApp
class HiltApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var myPushWorkerFactory: PushWorkerFactory

    @Inject
    lateinit var myRecipeWorkerFactory: RecipeWorkerFactory

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    /**
     * Gives WorkManager the factories of the workers of this application.
     *
     * WorkManager 2.9 reads a property here. The version before read the
     * method getWorkManagerConfiguration().
     */
    override val workManagerConfiguration: Configuration
        get() {
            val workerFactory = DelegatingWorkerFactory()
            workerFactory.addFactory(myRecipeWorkerFactory)
            workerFactory.addFactory(myPushWorkerFactory)

            return Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        // WorkManager
        val context = applicationContext
        delayedInit(context)
    }

    private fun delayedInit(context: Context) {
        applicationScope.launch {
            setupWork(context)
        }
    }

    /**
     * Plans the two pieces of work that run once a day.
     *
     * The notification arrives at the hour of [NotificationSchedule], and the
     * recipes are read a quarter of an hour earlier, so that the notification
     * holds fresh suggestions. Both use UPDATE, so that a telephone which
     * still holds the old plan of every fifteen minutes receives the new one.
     */
    private fun setupWork(context: Context) {
        val constraints: Constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        val recipeWork =
            PeriodicWorkRequestBuilder<RecipeWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(
                    NotificationSchedule.refreshDelayMillis(),
                    TimeUnit.MILLISECONDS,
                )
                .build()

        val pushWork =
            PeriodicWorkRequestBuilder<PushWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .setInitialDelay(
                    NotificationSchedule.notificationDelayMillis(),
                    TimeUnit.MILLISECONDS,
                )
                .build()

        val manager = WorkManager.getInstance(context)
        manager.enqueueUniquePeriodicWork(
            RECIPE_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            recipeWork,
        )
        manager.enqueueUniquePeriodicWork(
            PUSH_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            pushWork,
        )
    }

    private companion object {
        /** Name of the daily work that reads the recipes. */
        const val RECIPE_WORK = "recipeWork"

        /** Name of the daily work that sends the notification. */
        const val PUSH_WORK = "Push Work"
    }
}
