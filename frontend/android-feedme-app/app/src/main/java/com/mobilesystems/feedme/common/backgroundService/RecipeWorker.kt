package com.mobilesystems.feedme.common.backgroundService

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobilesystems.feedme.data.repository.WorkerRepositoryImpl
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecipeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val workerRepository: WorkerRepositoryImpl)
    : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            //get userId
            val context = applicationContext
            val userId = getLoggedInUserId(context)
            Log.d("RecipeWorker", "UserId: $userId")

            if (userId != null) {
                //network call
                workerRepository.pollAllRecipesAPI(userId)
            }
        }catch(e:Exception){
            return Result.failure()
        }
        return Result.success()
    }

    //get userId of current logged-in-user
    private fun getLoggedInUserId(context: Context): Int? {
        val result = getLoggedInUser(context)
        return result?.userId
    }
}