package com.mobilesystems.feedme.ui.search

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.bumptech.glide.load.engine.Resource
import com.mobilesystems.feedme.data.repository.InventoryRepositoryImpl
import com.mobilesystems.feedme.data.repository.RecipeRepositoryImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SharedViewModel to propagate shared Data between Fragments.
 */
@HiltViewModel
class SearchSearchViewModel@Inject constructor(
    androidApplication: Application,
    private val inventoryRepository: InventoryRepositoryImpl,
    private val recipeRepository: RecipeRepositoryImpl) : BaseViewModel(androidApplication), BaseSearchViewModel {

    // listen to multiple livedata
    val mediatorLiveDataSearchResult = MediatorLiveData<Resource<List<Any>>>()

    private var _recipeList = MutableLiveData<List<Recipe>?>()
    private var _inventoryList = MutableLiveData<List<Product>?>()
    private var _searchResultRecipeList = MutableLiveData<List<Recipe>?>()
    private var _searchResultInventoryList = MutableLiveData<List<Product>?>()
    private var _searchResult = MutableLiveData<List<Any>?>()
    private var _currentUser = MutableLiveData<Int?>()

    val recipeList : LiveData<List<Recipe>?>
        get() = _recipeList

    val inventoryList : LiveData<List<Product>?>
        get() = _inventoryList

    val searchResultRecipeList : LiveData<List<Recipe>?>
        get() = _searchResultRecipeList

    val searchResultInventoryList : LiveData<List<Product>?>
        get() = _searchResultInventoryList

    val searchResult: LiveData<List<Any>?>
        get() = _searchResult

    val currentUser : LiveData<Int?>
        get() = _currentUser

    init {
        val context = getApplication<Application>().applicationContext
        getCurrentUser(context)

        loadAllRecipes()
        loadAllProductsFromInventory()
    }

    override fun loadAllRecipes() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUser.value
            if(userId != null) {
                _recipeList.value = recipeRepository.loadAllRecipesBasedOnInventory(userId)
            }
        }
    }

    override fun loadAllProductsFromInventory() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUser.value
            if(userId != null) {
                _inventoryList.value = inventoryRepository.loadInventoryListProducts(userId)
            }
        }
    }

    override fun searchGlobal(searchText: String): LiveData<List<Any>?> {
        val tempProductList = searchProduct(searchText)
        val tempRecipeList = searchRecipe(searchText)
        _searchResult.value = _searchResult.value?.plus(listOf(tempProductList))
        _searchResult.value = _searchResult.value?.plus(listOf(tempRecipeList))

        return searchResult
    }

    override fun searchProduct(searchText: String): LiveData<List<Product>?> {
        // Helper function search product
        val result: List<Product>?
        val tempList = inventoryList.value
        if(tempList != null) {
            result = tempList.filter { it.productName == searchText }
            _searchResultInventoryList.value = result
        }
        return searchResultInventoryList
    }

    override fun searchRecipe(searchText: String): LiveData<List<Recipe>?> {
        // Helper functino search recipe
        val result: List<Recipe>?
        val tempList = recipeList.value
        if(tempList != null) {
            result = tempList.filter { it.recipeName == searchText }
            _searchResultRecipeList.value = result
        }
        return searchResultRecipeList
    }

    private fun getCurrentUser(context: Context): LiveData<Int?>{
        val result = getLoggedInUser(context)
        _currentUser.value = result?.userId
        return  currentUser
    }
}