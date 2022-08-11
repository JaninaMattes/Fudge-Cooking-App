package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Response
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.di.AppModule
import com.mobilesystems.feedme.domain.model.Recipe
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RecipeDataSourceImpl @Inject constructor(
    private val foodTrackerApi: FoodTrackerApi,
    @AppModule.DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    @AppModule.IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    BaseDataSource(), RecipeDataSource {

    override suspend fun getAllRecipesByUserId(userId: Int) = withContext(ioDispatcher) {
        getResult { foodTrackerApi.getAllRecipesByUserId(userId) }
    }

    override suspend fun getBestRatedRecipesByUserId(userId: Int)= withContext(ioDispatcher) {
        getResult { foodTrackerApi.getBestRatedRecipesByUserId(userId) }
    }

    override suspend fun updateRatingByRecipeId(recipeId: Int, rating: Float) {
        TODO("Not yet implemented")
    }

    override suspend fun addRecipeToFavoritesByUserId(userId: Int, recipeId: Int) {
        TODO("Not yet implemented")
    }
}