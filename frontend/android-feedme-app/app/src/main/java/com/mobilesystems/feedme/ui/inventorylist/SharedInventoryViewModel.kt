package com.mobilesystems.feedme.ui.inventorylist

import android.app.Application
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobilesystems.feedme.data.repository.InventoryRepositoryImpl
import com.mobilesystems.feedme.domain.model.*
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * SharedViewModel to propagate shared Data between Fragments.
 *
 * https://developer.android.com/codelabs/basic-android-kotlin-training-shared-viewmodel#4
 */
@HiltViewModel
class SharedInventoryViewModel @Inject constructor(
    androidApplication : Application,
    private val inventoryRepository: InventoryRepositoryImpl) :
    BaseViewModel(androidApplication), BaseInventoryViewModel {

    private var _inventoryList = MutableLiveData<List<Product>?>()
    private var _barcodeScanProduct = MutableLiveData<Product?>()
    private var _selectedProduct = MutableLiveData<Product?>()
    private var _selectedTagList = MutableLiveData<List<Label>?>()
    private var _allProductLabels = MutableLiveData<List<String>>()
    private var _loggedInUser = MutableLiveData<User?>()
    private var _currentUserId = MutableLiveData<Int?>()

    val inventoryList : LiveData<List<Product>?>
        get() = _inventoryList

    val barcodeScanProduct : LiveData<Product?>
        get() = _barcodeScanProduct

    val selectedProduct : LiveData<Product?>
        get() = _selectedProduct

    val selectedProductTagList : LiveData<List<Label>?>
        get() = _selectedTagList

    val allProductLabels : LiveData<List<String>>
        get() = _allProductLabels

    val loggedInUser : LiveData<User?>
        get() = _loggedInUser

    val currentUserId : LiveData<Int?>
        get() = _currentUserId

    init {
        val context = getApplication<Application>().applicationContext
        getLoggedInUserId(context)

        if (inventoryHasNoValues()) {
            // preload all values
            loadAllProductsOfInventoryList()
        }
    }

    override fun selectProduct(product: Product){
        _selectedProduct.value = product
        // get tag list of the selected product
        loadSelectedProductTagList()
    }

    override fun deleteProductByPosition(position: Int): Product? {
        // This is a coroutine scope with the lifecycle of the ViewModel
        var product: Product? = null
        val currentValues = inventoryList.value
        if (currentValues != null) {
            val tempList = currentValues.toMutableList()
            product = tempList[position]
            tempList.removeAt(position)
            _inventoryList.postValue(tempList)
        }
        return product
    }

    override fun getProductFromBarcodeScanResult(barcodeScanRes: String): Product? {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val result = inventoryRepository.getBarcodeScanResult(barcodeScanRes)
            _barcodeScanProduct.value = result
        }
        return _barcodeScanProduct.value
    }

    override fun addProductFromBarcodeScanResultToInventory(product: Product) {
        // Add newly created product
        addProductToInventoryList(product)
    }

    override fun updateProductOnInventory(product: Product) {
        // Update the values of a product that already exists in inventory
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = loggedInUser.value?.userId
            val currentValues = inventoryList.value
            var tempList: MutableList<Product> = ArrayList<Product>()
            if(userId != null) {
                if (currentValues != null) {
                    tempList = currentValues.toMutableList()
                    tempList.forEachIndexed { index, oldProduct ->
                        if (oldProduct.productId == product.productId) {
                            tempList[index] = product
                            inventoryRepository.updateProductOnInventory(userId, product)
                            updateInventoryList(tempList)
                        }
                    }
                }
            }
        }
    }

    override fun addProductToInventoryList(product: Product) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = loggedInUser.value?.userId
            val currentItems = inventoryList.value

            // Network call
            if(userId != null && userId != 0) {
                var tempList: MutableList<Product> = mutableListOf()
                // Check if currentlist is empty
                if (currentItems != null) {
                    tempList = currentItems.toMutableList()
                    // find duplicate items
                    val duplicateValue = tempList.filter { p -> p.productName == product.productName }
                    // iterate over duplicate values (best case only 1)
                    if (duplicateValue.isNotEmpty()) {
                        for (i in duplicateValue.indices) {
                            if (duplicateValue[i].productName == product.productName) {
                                val newProduct = calculateNewAmount(duplicateValue[i], product)
                                tempList = tempList.replace(duplicateValue[i], newProduct).toMutableList()
                                inventoryRepository.updateProductOnInventory(userId, newProduct)
                            }
                        }
                        // no duplicate values
                    } else {
                        val newProduct = inventoryRepository.addProductToInventory(userId, product)
                        tempList.add(newProduct)
                    }
                // empty list
                } else {
                    val newProduct = inventoryRepository.addProductToInventory(userId, product)
                    tempList.add(newProduct)
                }
                updateInventoryList(tempList)
            }
        }
    }

    private fun calculateNewAmount(product_one: Product, product_two: Product): Product{
        val newProduct: Product
        val amountOne = product_one.quantity.filter { it.isDigit() }
        val amountTwo = product_two.quantity.filter { it.isDigit() }
        var amountType = product_one.quantity.filter { it.isLetter() }
        if (amountType.isEmpty()){
            amountType = "Pieces"
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

    override fun deleteProductInInventoryList(product: Product) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = loggedInUser.value?.userId
            if(userId != null && userId != 0) {
                inventoryRepository.removeProductFromInventory(userId, product)
            }
        }
    }

    override fun loadAllProductsOfInventoryList() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = loggedInUser.value?.userId
            if(userId != null) {
                val result = inventoryRepository.loadInventoryListProducts(userId)
                filterListByExpirationDate(result)
            }
        }
    }

    override fun loadSelectedProductTagList(): LiveData<List<Label>?> {
        // Helper function to load all tags from a product
        _selectedTagList.postValue(selectedProduct.value?.labels)
        return selectedProductTagList
    }

    override fun updateInventoryList() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = loggedInUser.value?.userId
            if (userId != null && userId != 0) {
                val currentValues = inventoryList.value
                if(currentValues != null) {
                    inventoryRepository.updateProductInventoryList(userId, currentValues)
                }
            }
        }
    }

    override fun updateImage(image: Image) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val userId = getLoggedInUserId(context).value
            if(userId != null && userId != 0) {
                inventoryRepository.updateProductImage(userId, image)
            }
        }
    }

    // Only for the dropdown menu
    fun loadAllProductLabels(): List<String> {
        val labels = Label.values()
        val tempList: MutableList<String> = mutableListOf()
        labels.forEach {
            tempList.add(it.label)
        }
        // filter drop down values
        val sorted = tempList.sortedBy { it }
        _allProductLabels.postValue(sorted)
        return sorted
    }

    fun refresh(){
        // refresh after certain time for better user experience
        viewModelScope.launch {
            delay(3500)
            val userId = loggedInUser.value?.userId
            if(userId != null) {
                val result = inventoryRepository.loadInventoryListProducts(userId)
                filterListByExpirationDate(result)
            }
        }
    }

    // filter products by expiration date
    private fun filterListByExpirationDate(result: List<Product>?): LiveData<List<Product>?>{
        if(result != null) {
            val tempList = result.toMutableList()
            val cmp = compareBy<Product> { convertStringToDate(it.expirationDate) }
            _inventoryList.value = tempList.sortedWith(cmp) // ascending
        }else{
            Log.d("SharedInventoryViewModel", "Inventorylist is empty.")
        }
        return inventoryList
    }

    private fun inventoryHasNoValues(): Boolean{
        return inventoryList.value.isNullOrEmpty()
    }

    private fun isProductInInventory(product: Product): Boolean {
        inventoryList.value?.forEach { p ->
            if (p.productName == product.productName) {
                return true
            }
        }
        return false
    }

    private fun updateInventoryList(inventoryList: List<Product>){
        _inventoryList.value = inventoryList
    }

    private fun <Product> List<Product>.replace(old: Product, new: Product) = map { if (it == old) new else it }

    private fun convertStringToDate(dateStr: String): Date {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val date = sdf.parse(dateStr)
        Log.d("SharedInventoryViewModel", "Current date $date")
        return date
    }

    private fun getLoggedInUserId(context: Context): LiveData<Int?>{
        val result = getLoggedInUser(context)
        _currentUserId.value = result?.userId
        if(result?.userId != null){
            // get loggedin user from shared preferences
            // tloaded from backend
            _loggedInUser.value = User(
                userId = result.userId,
                firstName = result.firstName,
                lastName = result.lastName,
                email = result.email,
                password = "-")
        }
        return  currentUserId
    }
}