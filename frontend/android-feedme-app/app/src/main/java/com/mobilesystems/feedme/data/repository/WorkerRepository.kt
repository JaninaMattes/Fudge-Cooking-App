package com.mobilesystems.feedme.data.repository

interface WorkerRepository {

    suspend fun pollAllRecipesAPI(userId: Int)

    suspend fun getAmountOfExpiringProductsByUserId(userId: Int): Int?

}