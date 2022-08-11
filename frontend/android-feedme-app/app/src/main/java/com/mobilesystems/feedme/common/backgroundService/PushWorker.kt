package com.mobilesystems.feedme.common.backgroundService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobilesystems.feedme.R
import com.mobilesystems.feedme.data.repository.WorkerRepositoryImpl
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val workerRepository: WorkerRepositoryImpl)
    : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "fudgeChannel01"
    }

    override suspend fun doWork(): Result {
        try {
            //get userId
            val context = applicationContext
            val userId = getLoggedInUserId(context)
            Log.d("PushWorker", "UserId: $userId")

            if (userId != null) {
                //network call
                val result = workerRepository.getAmountOfExpiringProductsByUserId(userId)
                Log.d("PushWorker", "Anzahl ablaufender Produkte " + result.toString())
                if (result != null) {
                    if(result != 0) {
                        showNotification(context, result)
                    }
                }
            }
        }catch(e: Exception){
            return Result.failure()
        }
        return Result.success()
    }

    //get userId of current logged-in-user
    private fun getLoggedInUserId(context: Context): Int? {
        val result = getLoggedInUser(context)
        return result?.userId
    }

    //PushNotification for expiring products
    private fun showNotification(context: Context, result: Int) {
        val builder = context.let {
            NotificationCompat.Builder(it, CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_fridgeicon)
                setContentTitle("Expiring products in inventory")
                setContentText("You have $result expiring products in your inventory!")
                priority = NotificationCompat.PRIORITY_DEFAULT
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Expiring_Channel"
            val channelDescription = "ExpiringProduct_Channel for expiring products"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            val notifyManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifyManager.createNotificationChannel(channel)
        }

        with(context.let { NotificationManagerCompat.from(it) })
        {
            builder.let { this.notify(NOTIFICATION_ID, it.build()) }
        }
    }

}