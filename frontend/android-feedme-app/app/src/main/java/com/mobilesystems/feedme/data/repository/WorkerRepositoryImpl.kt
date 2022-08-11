package com.mobilesystems.feedme.data.repository

import com.mobilesystems.feedme.data.datasource.WorkerDataSourceImpl
import javax.inject.Inject

class WorkerRepositoryImpl @Inject constructor(
    private val workerDataSource: WorkerDataSourceImpl)
    : WorkerRepository {

    override suspend fun pollAllRecipesAPI(userId: Int){
        workerDataSource.pollAllRecipesAPI(userId)
    }

    override suspend fun getAmountOfExpiringProductsByUserId(userId: Int): Int? {
        val result = workerDataSource.getAmountOfExpiringProductsByUserId(userId)
        return result.data
    }
}