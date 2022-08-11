package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource

interface WorkerDataSource {

    suspend fun pollAllRecipesAPI(userId: Int): Resource<Int?>

    suspend fun getAmountOfExpiringProductsByUserId(userId: Int): Resource<Int?>

}