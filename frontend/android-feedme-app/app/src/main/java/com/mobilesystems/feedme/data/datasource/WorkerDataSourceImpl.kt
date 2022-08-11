package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.di.AppModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkerDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BaseDataSource(), WorkerDataSource {

    override suspend fun pollAllRecipesAPI(userId: Int): Resource<Int?> = withContext(ioDispatcher) {
        getResult { foodTrackerApi.pollAllRecipesAPI(userId) }
    }

    override suspend fun getAmountOfExpiringProductsByUserId(userId: Int): Resource<Int?> = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getAmountOfExpiringProductsByUserId(userId) }
    }

}
