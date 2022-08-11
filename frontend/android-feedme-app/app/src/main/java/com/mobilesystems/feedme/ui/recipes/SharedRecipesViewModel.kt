package com.mobilesystems.feedme.ui.recipes

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobilesystems.feedme.data.repository.InventoryRepositoryImpl
import com.mobilesystems.feedme.data.repository.RecipeRepositoryImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.common.utils.containsSubstring
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sharedviewmodel to propagate data between fragments
 *
 * Documentations for usage of Context in MVVM:
 *  https://stackoverflow.com/questions/51451819/how-to-get-context-in-android-mvvm-viewmodel/51452435
 */
@HiltViewModel
class SharedRecipesViewModel @Inject constructor(
    androidApplication: Application,
    private val recipeRepository: RecipeRepositoryImpl,
    private val inventoryRepository: InventoryRepositoryImpl) :
    BaseViewModel(androidApplication), BaseRecipeViewModel  {

    private var _recipeList = MutableLiveData<List<Recipe>?>()
    private var _inventoryList = MutableLiveData<List<Product>?>()
    private var _shoppingList = MutableLiveData<List<Product>?>()
    private var _selectedRecipe = MutableLiveData<Recipe?>()
    private var _selectedRecipeIngredients = MutableLiveData<List<Product>?>()
    private var _availableIngredients = MutableLiveData<List<Product>?>()
    private var _notAvailableIngredients = MutableLiveData<List<Product>?>()
    private var _loggedInUser = MutableLiveData<User?>()
    private var _currentUserId = MutableLiveData<Int?>()

    val recipeList : LiveData<List<Recipe>?>
        get() = _recipeList

    val inventoryList : LiveData<List<Product>?>
        get() = _inventoryList

    val selectedRecipe : LiveData<Recipe?>
        get() = _selectedRecipe

    val shoppingList : MutableLiveData<List<Product>?>
        get() = _shoppingList

    val selectedRecipeIngredients : LiveData<List<Product>?>
        get() = _selectedRecipeIngredients

    val availableIngredients : LiveData<List<Product>?>
        get() = _availableIngredients

    val notAvailableIngredients : LiveData<List<Product>?>
        get() = _notAvailableIngredients

    val loggedInUser : LiveData<User?>
        get() = _loggedInUser

    val currentUserId : LiveData<Int?>
        get() = _currentUserId


    init {

        val context = getApplication<Application>().applicationContext
        getLoggedInUserId(context)

        if(recipeListHasNoValues()) {
            // preload all values
            loadMatchingRecipes()
        }

        if(inventoryListHasNoValues()){
            // preload all values
            loadInventoryList()
        }

        if(shoppingListHasNoValues()){
            // preload all values
            loadShoppingList()
        }

    }

    override fun selectedRecipe(recipe: Recipe){
        // selected recipe from list
        _selectedRecipe.value = recipe
        loadAllRecipeIngredients()
    }

    override fun updateRecipe(recipe: Recipe) {
        // Update if rating of recipe has changed
        viewModelScope.launch {
            _selectedRecipe.value = recipe
            recipeRepository.rateRecipe(recipe)
            addRecipeToFavorites(recipe.recipeId)
        }
    }

    override fun addRecipeToFavorites(recipeId: Int) {
        // For future features
        viewModelScope.launch {
            try{
                val userId = currentUserId.value
                if(userId != null) {
                    recipeRepository.addRecipeToFavoriteList(userId, recipeId)
                }
            }catch (e: Exception){
                Log.d("Recipe", "Error occured $e")
                e.stackTrace
            }
        }
    }

    override fun loadAllRecipeIngredients(): LiveData<List<Product>?> {
        // Helper function
        _selectedRecipeIngredients.value = selectedRecipe.value?.ingredients
        // filter the ingredients which are currently unavailable
        filterAvailableAndUnavailableIngredients()
        return selectedRecipeIngredients
    }

    override fun loadAllUnAvailableIngredients(): LiveData<List<Product>?> {
        // Helper function
        return notAvailableIngredients
    }

    override fun loadAllAvailableIngredients(): LiveData<List<Product>?> {
        // Helper function
        return availableIngredients
    }

    override fun saveCurrentShoppingState() {
        viewModelScope.launch {
            try{
                val userId = currentUserId.value
                if (userId != null) {
                    //recipeRepository.updateCurrentShoppingList(userId, shoppingList.value)
                }
            }catch (e: Exception){
                Log.d("Recipe", "Error occured $e")
                e.stackTrace
            }
        }
    }

    override fun exportUnavailableIngredientsToShoppingList() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {

            val userId = currentUserId.value
            val currentValues = shoppingList.value
            var tempList: MutableList<Product>
            tempList = currentValues as MutableList<Product>
            if (userId != null) {
                // Check if ingredientlist is empty
                _notAvailableIngredients.value?.forEach {
                    val duplicateValue = tempList.filter { p -> p.productName == it.productName }

                    //if ingredient is on shoppinglist, replace with new amount
                    if (duplicateValue.isNotEmpty()) {
                        for (i in duplicateValue.indices) {
                            if (duplicateValue[i].productName == it.productName) {
                                val newProduct = calculateNewAmount(
                                    duplicateValue[i],
                                    it
                                )
                                tempList = tempList.replace(
                                    duplicateValue[i],
                                    newProduct
                                ) as MutableList<Product>// Replace with new product
                                try{
                                    recipeRepository.updateSingleProductOnCurrentShoppingList(
                                        userId,
                                        newProduct
                                    )
                                }catch (e: Exception){
                                    Log.d("Recipe", "Error occured $e")
                                    e.stackTrace
                                }
                            }
                        }
                    }
                    //if ingredient is not on shoppinglist, add new product
                    else {
                        try{
                            val updatedProduct =
                                recipeRepository.addNewProductToCurrentShoppingList(userId, it)
                            tempList.add(updatedProduct)
                        }catch (e: Exception){
                            Log.d("Recipe", "Error occured $e")
                            e.stackTrace
                        }
                    }
                }
            }
            _shoppingList.value = tempList
        }
    }

    private fun <Product> List<Product>.replace(old: Product, new: Product) = map { if (it == old) new else it }

    private fun calculateNewAmount(product_one: Product, product_two: Product): Product{
        val newProduct: Product
        val amountOne = product_one.quantity.filter { it.isDigit() }
        val amountTwo = product_two.quantity.filter { it.isDigit() }
        var amountType = product_one.quantity.filter { it.isLetter() }
        if (amountType.isEmpty()){
            amountType = "piece"
        }

        val newAmount = amountOne.toInt() + amountTwo.toInt()

        newProduct = product_one.copy(
            productId = product_one.productId,
            productName = product_one.productName,
            expirationDate = product_one.expirationDate,
            labels = product_one.labels,
            quantity = "$newAmount $amountType",
            manufacturer = product_one.manufacturer,
            nutritionValue = product_one.nutritionValue,
            productImage = product_one.productImage
        )
        return newProduct
    }

    override fun loadShoppingList() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if(userId != null) {
                try{
                    _shoppingList.value = recipeRepository.loadCurrentShoppingListProducts(userId)
                }catch (e: Exception){
                    Log.d("Recipe", "Error occured $e")
                    e.stackTrace
                }
            }
        }
    }

    override fun loadInventoryList() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if(userId != null) {
                try{
                    _inventoryList.value = inventoryRepository.loadInventoryListProducts(userId)
                }catch (e: Exception){
                    Log.d("Recipe", "Error occured $e")
                    e.stackTrace
                }
            }
        }
    }

    override fun loadMatchingRecipes() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if(userId != null) {
                try{
                    val result = recipeRepository.loadAllRecipesBasedOnInventory(userId)
                    // filter by rating
                    _recipeList.value = filterListByRating(filterDuplicates(result))
                }catch (e: Exception){
                    Log.d("Recipe", "Error occured $e")
                    e.stackTrace
                }
            }
        }
    }

    override fun removeRecipeByPosition(position: Int) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val currentValues = recipeList.value
            var tempList: MutableList<Recipe> = ArrayList()
            if (currentValues != null) {
                tempList = currentValues as MutableList<Recipe>
                tempList.removeAt(position)
            }
            _recipeList.value = tempList
        }
    }

    fun isProductAvailable(productList: List<Product>?, product: Product): Boolean {
        var available = false
        viewModelScope.launch {
            if (productList != null) {
                if (containsSubstring(productList, product)) {
                    available = true
                }
            }
        }
        return available
    }

    private fun recipeListHasNoValues(): Boolean{
        return _recipeList.value.isNullOrEmpty()
    }

    private fun inventoryListHasNoValues(): Boolean{
        return _inventoryList.value.isNullOrEmpty()
    }

    private fun shoppingListHasNoValues(): Boolean{
        return _shoppingList.value.isNullOrEmpty()
    }

    private fun filterAvailableAndUnavailableIngredients(){
        val tempListAvailable = arrayListOf<Product>()
        val tempListUnAvailable = arrayListOf<Product>()
        val ingList = selectedRecipeIngredients.value

        ingList?.forEach {
            if (isProductAvailable(inventoryList.value, it)) {
                tempListAvailable.add(it)
            } else {
                tempListUnAvailable.add(it)
            }
            _availableIngredients.value = tempListAvailable
            _notAvailableIngredients.value = tempListUnAvailable
        }
    }

    private fun getLoggedInUserId(context: Context): LiveData<Int?>{
        val result = getLoggedInUser(context)
        _currentUserId.value = result?.userId
        if(result?.userId != null){
            // get loggedin user from shared preferences
            _loggedInUser.value = User(
                userId = result.userId,
                firstName = result.firstName,
                lastName = result.lastName,
                email = result.email,
                password = "")
        }
        return  currentUserId
    }

    private fun filterListByRating(result: List<Recipe>?): List<Recipe>?{
        var filtered: List<Recipe>? = null
        if(result != null) {
            val sorted = result.sortedBy { it.cummulativeRating }
            filtered = sorted.reversed() // ascending
        }
        return filtered
    }

    private fun filterDuplicates(result: List<Recipe>?): List<Recipe>?{
        var noDuplicates: List<Recipe>? = null
        if(result != null) {
            noDuplicates = result.distinctBy { it.recipeName }
        }
        return noDuplicates
    }
}