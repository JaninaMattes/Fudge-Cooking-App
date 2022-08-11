package com.mobilesystems.feedme.ui.shoppinglist

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mobilesystems.feedme.data.repository.ShoppingListRepositoryImpl
import com.mobilesystems.feedme.domain.model.Product
import com.mobilesystems.feedme.ui.common.utils.getLoggedInUser
import com.mobilesystems.feedme.ui.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * SharedViewModel to propagate shared Data between Fragments.
 */
@HiltViewModel
class SharedShoppingListViewModel @Inject constructor(
    androidApplication: Application,
    private val shoppingListRepository: ShoppingListRepositoryImpl) :
    BaseViewModel(androidApplication), BaseShoppingListViewModel {

    // Store view data in MutableLiveData
    private var _newproduct = MutableLiveData<Product>()
    private var _currentShoppingList = MutableLiveData<List<Product>?>()
    private var _oldShoppingList = MutableLiveData<List<Product>?>()
    private var _selectedProduct = MutableLiveData<Product?>()
    private var _currentUserId = MutableLiveData<Int?>()

    // Getter and setter
    val currentShoppingList : LiveData<List<Product>?>
        get() = _currentShoppingList

    val oldShoppingList : LiveData<List<Product>?>
        get() = _oldShoppingList

    val newProduct : LiveData<Product?>
        get() = _newproduct

    val selectedProduct : LiveData<Product?>
        get() = _selectedProduct

    val currentUserId : LiveData<Int?>
        get() = _currentUserId

    init {
        val context = getApplication<Application>().applicationContext
        getCurrentUserId(context)

        if (currentShophasNoValues()) {
            // preload all values
            loadAllCurrentShoppingListProducts()
        }

        if (oldShophasNoValues()) {
            // preload all shoppinglist values
            loadAllOldShoppingListProducts()
        }
    }

    fun refresh(){
        // refresh after certain time for better user experience
        viewModelScope.launch {
            delay(3500)
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                val result = shoppingListRepository.loadCurrentShoppingListProducts(userId)
                _currentShoppingList.value = result
            }
        }
    }

    override fun loadAllCurrentShoppingListProducts() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                val result = shoppingListRepository.loadCurrentShoppingListProducts(userId)
                _currentShoppingList.value = result
            }
        }
    }

    override fun loadAllOldShoppingListProducts() {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                val result = shoppingListRepository.loadOldShoppingListProducts(userId)
                passToOldShoppingList(result)
            }
        }
    }

    override fun saveCurrentState() {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                shoppingListRepository.updateCurrentShoppingList(userId, currentShoppingList.value)
                shoppingListRepository.updateOldShoppingList(userId, oldShoppingList.value)
            }
        }
    }

    override fun setProduct(product: Product) {
        // Set selected product from list
        _selectedProduct.value = product
    }

    override fun setNewProduct(product: Product){
        // Set newly created product
        _newproduct.value = product
    }

    override fun addNewProductToCurrentShoppingList(product: Product) {
        viewModelScope.launch {
            val userId = this@SharedShoppingListViewModel.currentUserId.value
            val currentItems = currentShoppingList.value
            var tempList: MutableList<Product> = mutableListOf()

            // add product to shoppinglist
            if (userId != null && userId != 0) {
                    // Check if currentlist is empty
                if (currentItems != null) {
                    tempList = currentItems as MutableList<Product>
                    // find duplicate items
                    val duplicateValue = tempList.filter { p -> p.productName == product.productName }
                    // duplicate values
                    if (duplicateValue.isNotEmpty()) {
                        Log.d("SharedShoppingListViewModel", "Duplicate product found")
                        for (p in duplicateValue) {
                            if (p.productName == product.productName) {
                                val newProduct = calculateNewProductAmount(p, product, currentShoppingList = true)
                                tempList =
                                    tempList.replace(p, newProduct) as MutableList<Product>// Replace with new product
                                passToCurrentShoppingList(tempList)
                                // update product
                                shoppingListRepository.updateSingleProductOnCurrentShoppingList(userId, newProduct)
                                Log.d("SharedShoppingListViewModel", "Update product to current shoppinglist.")
                            }
                        }
                    // no duplicate values
                    } else {
                        // create new product
                        val updatedProduct = shoppingListRepository.addNewProductToCurrentShoppingList(userId, product)
                        tempList.add(updatedProduct)
                        passToCurrentShoppingList(tempList)
                        Log.d("SharedShoppingListViewModel", "Add product to current shoppinglist.")
                    }
                // empty list or no duplicate values
                } else {
                    // create new product
                    val updatedProduct = shoppingListRepository.addNewProductToCurrentShoppingList(userId, product)
                    tempList.add(updatedProduct)
                    passToCurrentShoppingList(tempList)
                    Log.d("SharedShoppingListViewModel", "Add product to current shoppinglist.")
                }
            }
            loadAllCurrentShoppingListProducts()
        }
    }

    override fun addProductToCurrentShoppingList(product: Product){
        viewModelScope.launch {
            val userId = this@SharedShoppingListViewModel.currentUserId.value
            // add product to shoppinglist
            if (userId != null && userId != 0) {

                // remove from old shoppinglist
                updateOldShopList(product)

                // Check if currentlist is empty and update current shoppinglist
                val curShoppingList = currentShoppingList.value
                var tempList: MutableList<Product> = mutableListOf()

                // not empty
                if (curShoppingList != null) {
                    tempList = curShoppingList as MutableList<Product>
                    // find duplicate items (expected only one)
                    val duplicateValue = tempList.filter { p -> p.productName == product.productName }

                    // duplicate values
                    if (duplicateValue.isNotEmpty()) {
                        Log.d("SharedShoppingListViewModel", "Duplicate product found")
                        for (p in duplicateValue) {
                            if (p.productName == product.productName) {
                                val newProduct = calculateNewProductAmount(p, product, currentShoppingList = true)
                                // replace old product with new product values
                                tempList = tempList.replace(p, newProduct) as MutableList<Product>
                                passToCurrentShoppingList(tempList)
                                shoppingListRepository.updateSingleProductOnCurrentShoppingList(userId, newProduct)
                                Log.d("SharedShoppingListViewModel", "Update product in current shoppinglist.")
                            }
                        }
                    } else {
                        // no duplicate values
                        tempList.add(product)
                        passToCurrentShoppingList(tempList)
                        shoppingListRepository.addProductToCurrentShoppingList(userId, product)
                        Log.d("SharedShoppingListViewModel", "Add product in current shoppinglist.")
                    }
                // empty list
                } else {
                    tempList.add(product)
                    passToCurrentShoppingList(tempList)
                    shoppingListRepository.addProductToCurrentShoppingList(userId, product)
                    Log.d("SharedShoppingListViewModel", "Add product in current shoppinglist.")
                }
            }
        }
    }

    override fun addProductToOldShoppingList(product: Product) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                // update current shoppinglist
                    updateCurShopList(product)

                val oldShoppingList = oldShoppingList.value
                var tempList: MutableList<Product> = mutableListOf()
                // update old shoppinglist with product
                val newProduct = createProductForOldShoppingList(product)

                // Check if currentlist is empty
                if (oldShoppingList != null) {
                    tempList = oldShoppingList as MutableList<Product>
                    // find duplicate items (expected only single product)
                    val duplicateValue= tempList.filter { p -> p.productName == newProduct.productName }

                    // duplicate values
                    if (duplicateValue.isNotEmpty()) {
                        Log.d("SharedShoppingListViewModel", "Duplicate product found")
                        for (p in duplicateValue) {
                            if (p.productName == newProduct.productName) {
                                tempList = tempList.replace(p, newProduct) as MutableList<Product>
                                passToOldShoppingList(tempList)
                                shoppingListRepository.updateSingleProductOnOldShoppingList(userId, newProduct)
                                Log.d("SharedShoppingListViewModel", "Update product in old shoppinglist.")
                            }
                        }
                    // no duplicate values
                    } else {
                        tempList.add(newProduct)
                        passToOldShoppingList(tempList)
                        shoppingListRepository.addProductToOldShoppingList(userId, newProduct)
                        Log.d("SharedShoppingListViewModel", "Add product to old shoppinglist. ")
                    }
                // empty list
                } else {
                    tempList.add(newProduct)
                    passToOldShoppingList(tempList)
                    shoppingListRepository.addProductToOldShoppingList(userId, newProduct)
                    Log.d("SharedShoppingListViewModel", "Add product to old shoppinglist.")
                }
            }
        }
    }

    override fun updateProductOnOldShoppingList(product: Product) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                shoppingListRepository.updateSingleProductOnOldShoppingList(userId, product)
                Log.d("SharedShoppingListViewModel", "Update product on old shoppinglist.")
            }
        }
    }

    override fun updateCurrentShoppingList(shoppingList: List<Product>?) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null) {
                shoppingListRepository.updateCurrentShoppingList(userId, shoppingList)
            }
        }
    }

    override fun updateProductOnCurrentShoppingList(product: Product) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null && userId != 0) {
                shoppingListRepository.updateSingleProductOnCurrentShoppingList(userId, product)
                Log.d("SharedShoppingListViewModel", "Update product on current shoppinglist.")
            }
        }
    }

    override fun updateOldShoppingList(shoppingList: List<Product>?) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null) {
                shoppingListRepository.updateOldShoppingList(userId, shoppingList)
            }
        }
    }

    override fun deleteCurrentProductByPosition(position: Int): Product? {
        // This is a coroutine scope with the lifecycle of the ViewModel
        var product: Product? = null
        val currentValues = currentShoppingList.value
        if (currentValues != null) {
            val tempList = currentValues.toMutableList()
            product = tempList[position]
            tempList.removeAt(position)
            _currentShoppingList.postValue(tempList)
        }
        return product
    }

    override fun removeProductFromCurrentShoppingList(product: Product) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null) {
                val currentValues = currentShoppingList.value
                var tempList: MutableList<Product> = ArrayList()
                if (currentValues != null) {
                    tempList = currentValues as MutableList<Product>
                    tempList.remove(product)
                }
                shoppingListRepository.removeProductFromCurrentShoppingList(userId, product)
                _currentShoppingList.value = tempList
                Log.d("SharedShoppingListViewModel", "Remove product from current shoppinglist.")
            }
        }
    }

    override fun deleteOldProductByPosition(position: Int): Product? {
        // This is a coroutine scope with the lifecycle of the ViewModel
        var product: Product? = null
        val currentValues = oldShoppingList.value
        if (currentValues != null) {
            val tempList = currentValues.toMutableList()
            product = tempList[position]
            tempList.removeAt(position)
            _oldShoppingList.postValue(tempList)
        }
        return product
    }

    override fun removeProductFromOldShoppingList(product: Product) {
        // This is a coroutine scope with the lifecycle of the ViewModel
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId != null) {
                val currentValues = oldShoppingList.value
                var tempList: MutableList<Product> = ArrayList()
                if (currentValues != null) {
                    tempList = currentValues as MutableList<Product>
                    tempList.remove(product)
                }
                shoppingListRepository.removeProductFromOldShoppingList(userId, product)
                _oldShoppingList.value = tempList
                Log.d("SharedShoppingListViewModel", "Remove product from old shoppinglist.")
            }
        }
    }

    private fun currentShophasNoValues(): Boolean{
        return _currentShoppingList.value.isNullOrEmpty()
    }

    private fun oldShophasNoValues(): Boolean{
        return _oldShoppingList.value.isNullOrEmpty()
    }

    private fun calculateNewProductAmount(product_one: Product, product_two: Product, currentShoppingList: Boolean): Product{
        val newProduct: Product
        var amountType = product_one.quantity.filter { it.isLetter() }
        if (amountType.isEmpty()) {
            amountType = "piece"
        }

        if(currentShoppingList) {
            val amountOne = product_one.quantity.filter { it.isDigit() }
            val amountTwo = product_two.quantity.filter { it.isDigit() }
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
        } else {
            newProduct = product_one.copy(
                productId = product_one.productId,
                productName = product_one.productName,
                expirationDate = "-",
                labels = product_one.labels,
                quantity = "1 $amountType",
                manufacturer = "-",
                nutritionValue = product_one.nutritionValue,
                productImage = product_one.productImage
            )
        }
        return newProduct
    }

    private fun createProductForOldShoppingList(product: Product): Product{
        val newProduct: Product
        var amountType = product.quantity.filter { it.isLetter() }
        if (amountType.isEmpty()) {
            amountType = "piece"
        }
        newProduct = product.copy(
            productId = product.productId,
            productName = product.productName,
            expirationDate = "-",
            labels = product.labels,
            quantity = "1 $amountType",
            manufacturer = "-",
            nutritionValue = product.nutritionValue,
            productImage = product.productImage
        )
        return newProduct
    }

    private fun <Product> List<Product>.replace(old: Product, new: Product) = map { if (it == old) new else it }


    private fun filterDuplicates(result: List<Product>?): List<Product>?{
        var noDuplicates: List<Product>? = null
        if(result != null) {
            noDuplicates = result.distinctBy { it.productName }
        }
        return noDuplicates
    }

    private fun updateOldShopList(product: Product){
        val oldShoppingList = oldShoppingList.value
        if(oldShoppingList != null){
            val tempList: MutableList<Product> = oldShoppingList as MutableList<Product>
            tempList.remove(product)
            passToOldShoppingList(tempList)
        }
    }

    private fun updateCurShopList(product: Product){
        val curShoppingList = currentShoppingList.value
        if(curShoppingList != null){
            val tempList: MutableList<Product> = curShoppingList as MutableList<Product>
            tempList.remove(product)
            passToCurrentShoppingList(tempList)
        }
    }

    private fun passToCurrentShoppingList(productList: List<Product>?){
        _currentShoppingList.value = productList
    }

    private fun passToOldShoppingList(productList: List<Product>?){
        _oldShoppingList.value = filterDuplicates(productList)
    }

    private fun getCurrentUserId(context: Context): LiveData<Int?>{
        val result = getLoggedInUser(context)
        _currentUserId.value = result?.userId
        return  currentUserId
    }
}
