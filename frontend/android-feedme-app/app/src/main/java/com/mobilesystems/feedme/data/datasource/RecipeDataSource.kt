package com.mobilesystems.feedme.data.datasource

import com.mobilesystems.feedme.common.networkresult.Resource
import com.mobilesystems.feedme.data.response.RecipeListResponse
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.common.networkresult.Response

interface RecipeDataSource {

    suspend fun getAllRecipesByUserId(userId: Int): Resource<RecipeListResponse>

    suspend fun getBestRatedRecipesByUserId(userId: Int): Resource<RecipeListResponse>

    suspend fun updateRatingByRecipeId(recipeId: Int, rating: Float)

    suspend fun addRecipeToFavoritesByUserId(userId: Int, recipeId: Int)
}