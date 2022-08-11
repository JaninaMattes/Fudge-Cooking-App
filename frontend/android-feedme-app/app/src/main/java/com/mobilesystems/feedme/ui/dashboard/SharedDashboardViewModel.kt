package com.mobilesystems.feedme.ui.dashboard

import android.app.Application
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobilesystems.feedme.data.repository.AuthRepositoryImpl
import com.mobilesystems.feedme.data.repository.DashboardRepositoryImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.domain.model.Recipe
import com.mobilesystems.feedme.domain.model.User
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import com.mobilesystems.feedme.ui.common.utils.removeAllValuesFromSharedPreferences
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * SharedViewModel to propagate shared Data between Fragments.
 */
@HiltViewModel
class SharedDashboardViewModel @Inject constructor(
    androidApplication : Application,
    private val dashboardRepository: DashboardRepositoryImpl,
    private val authRepository: AuthRepositoryImpl) :
    BaseViewModel(androidApplication), BaseDashboardViewModel{

    private var _expProductsList = MutableLiveData<List<Product>?>()
    private var _noOneRecipesList = MutableLiveData<List<Recipe>?>()
    private var _selectedProduct = MutableLiveData<Product?>()
    private var _selectedRecipe = MutableLiveData<Recipe?>()
    private var _loggedInUser = MutableLiveData<User?>()
    private var _currentUserId = MutableLiveData<Int?>()

    val expProductList : LiveData<List<Product>?>
        get() = _expProductsList

    val noOneRecipesList : LiveData<List<Recipe>?>
        get() = _noOneRecipesList

    val selectedProduct : LiveData<Product?>
        get() = _selectedProduct

    val selectedRecipe : LiveData<Recipe?>
        get() = _selectedRecipe

    val loggedInUser : LiveData<User?>
        get() = _loggedInUser

    val currentUserId : LiveData<Int?>
        get() = this._currentUserId

    init {
        // Get user id
        val context = getApplication<Application>().applicationContext
        getLoggedInUserId(context)

        // Preload user
        if(currentUserId.value != 0){
            loadLoggedInUser()
            updateLogin()
        }

        if (productsHasNoValues()) {
            // preload all values
            loadExpiringProducts()
        }
        if (recipesHasNoValues()) {
            // preload all values
            loadNumberOneRecipes()
        }
    }

    override fun selectProduct(product: Product){
        _selectedProduct.value = product
        // get tag list of the selected product
    }

    override fun selectedRecipe(recipe: Recipe){
        // selected recipe from list
        _selectedRecipe.value = recipe

    }
    override fun loadExpiringProducts() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {

            try {
                Log.d("Dashboard", "Load expiring products.")
                val userId = currentUserId.value
                if(userId != null) {
                    val result = dashboardRepository.getAllExpiringProducts(userId)
                    // filter by date
                    filterListByExpirationDate(result)
                    Log.d("Dashboard", "Load expiring products:  ${result.size}.")
                }
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }
    }

    override fun loadNumberOneRecipes() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            try{
                Log.d("Dashboard", "Load recipes.")
                val userId = currentUserId.value
                if(userId != null) {
                    val result  = dashboardRepository.getNumberOneRecipes(userId)
                    filterListByRating(result)
                    Log.d("Dashboard", "Load no one recipes:  ${result.size}.")
                }
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }
    }

    override fun loadLoggedInUser() {
        viewModelScope.launch {
            try{
                Log.d("Dashboard", "Load current user.")
                val userId = currentUserId.value
                if(userId != null) {
                    try {
                        val result = dashboardRepository.getCurrentLoggedInUser(userId)
                        _loggedInUser.value = result
                    } catch (error: Throwable){
                        // Notify view login attempt failed
                        Log.e("Dashboard", "error during login $error")
                        error.stackTrace
                    }
                }
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }
    }

    override fun logout(){
        viewModelScope.launch {
            try{
                val context = getApplication<Application>().applicationContext
                val userId = currentUserId.value
                if(userId != null && userId != 0){
                    authRepository.logout(userId)
                    Log.d("Logout", "UserId: " + userId.toString())
                    removeAllValuesFromSharedPreferences(context)
                }
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }
    }

    override fun updateLogin() {
        viewModelScope.launch {
            try{
                val userId = currentUserId.value
                if(userId != null && userId != 0){
                    authRepository.updateLogin(userId)
                    Log.d("Update Login", "User $userId")
                }
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }
    }

    private fun productsHasNoValues(): Boolean{
        return _expProductsList.value.isNullOrEmpty()
    }

    private fun recipesHasNoValues(): Boolean{
        return _noOneRecipesList.value.isNullOrEmpty()
    }

    private fun filterDuplicates(result: List<Recipe>?): List<Recipe>?{
        var noDuplicates: List<Recipe>? = null
        if(result != null) {
            noDuplicates = result.distinctBy { it.recipeName }
        }
        return noDuplicates
    }

    private fun filterListByRating(result: List<Recipe>?): LiveData<List<Recipe>?>{
        if(result != null) {
            try{
                val bestRating = filterDuplicates(result)?.filter { it.cummulativeRating > 4.3 } as MutableList<Recipe>
                val cmp = compareBy<Recipe> { it.cummulativeRating }
                _noOneRecipesList.value = bestRating.sortedWith(cmp).reversed() // ascending
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }else{
            Log.d("SharedDashboardViewModel", "Expiring products list is empty.")
        }
        return noOneRecipesList
    }

    private fun filterListByExpirationDate(result: List<Product>?): LiveData<List<Product>?>{
        if(result != null) {
            try{
                val tempList = result as MutableList<Product>
                val cmp = compareBy<Product> { convertStringToDate(it.expirationDate) }
                _expProductsList.value = tempList.sortedWith(cmp) // ascending
                Log.d("SharedDashboardViewModel", "List sorted by expiration date.")
            } catch (error: Throwable){
                // Notify view login attempt failed
                Log.e("Dashboard", "error $error")
                error.stackTrace
            }
        }else{
            Log.d("SharedDashboardViewModel", "Expiring products list is empty.")
        }
        return expProductList
    }

    private fun getLoggedInUserId(context: Context): LiveData<Int?>{
        try{
            val result = getLoggedInUser(context)
            _currentUserId.value = result?.userId
            if(result?.userId != null){
                // get loggedin user from shared preferences loaded from backend
                _loggedInUser.value = User(
                    userId = result.userId,
                    firstName = result.firstName,
                    lastName = result.lastName,
                    email = result.email,
                    password = "-")
            }
        } catch (error: Throwable){
            // Notify view login attempt failed
            Log.e("Dashboard", "error $error")
            error.stackTrace
        }
        return  currentUserId
    }

    private fun convertStringToDate(dateStr: String): Date {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val date = sdf.parse(dateStr)
        Log.d("SharedInventoryViewModel", "Current date $date")
        return date
    }
}