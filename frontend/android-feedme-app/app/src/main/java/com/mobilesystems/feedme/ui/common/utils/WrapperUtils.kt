package com.mobilesystems.feedme.ui.common.utils

import android.graphics.Bitmap
import android.util.Log
import com.mobilesystems.feedme.data.request.*
import com.mobilesystems.feedme.data.response.*
import com.mobilesystems.feedme.domain.model.*

fun convertAllowSettingRequest(userId: Int, allow: Boolean): UserAllowSettingsRequest {
    var allowed = 0
    if (allow) {
        allowed = 1
    }
    return UserAllowSettingsRequest(userId, allowed)
}

fun convertUserDietryTagRequest(user: User): UserDietryTagRequest?{
    val userId = user.userId
    val foodTypeList = user.dietaryPreferences
    var dietryTag: ProductTagRequest? = null
    var userDietryTagRequest: UserDietryTagRequest? = null

    try {
        if (foodTypeList != null) {
            for (label in foodTypeList) {
                dietryTag = ProductTagRequest(0, label.toString())
            }
        } else {
            dietryTag = ProductTagRequest(0, "")
        }
        userDietryTagRequest = UserDietryTagRequest(userId, dietryTag)
    }catch (e: Exception){
        e.stackTrace
    }
    return userDietryTagRequest
}

fun convertUpdateImageRequest(userId: Int, image: Image?): ImageRequest? {
    Log.d("Utils", "Convert image.")
    var userImageRequest: ImageRequest? = null
    var imageByteStr: String? = null
    try {
        if(image != null){
            if(image.bitmap != null){
                val imageBytes = convertBitmapToByteArray(image.bitmap)
                imageByteStr = bytesToBase64(imageBytes) // base64 encoded string
                Log.d("Utils", "Base64 encoded the image string.")
            }
            userImageRequest = ImageRequest(image.imageId, image.imageName, image.imageUrl, imageByteStr)
        }
    }catch (e: Exception){
        e.stackTrace
    }
    return userImageRequest
}

fun convertUpdateUserRequest(user: User): UpdateUserRequest {
    val userId = user.userId
    val email = user.email
    val firstName = user.firstName
    val lastName = user.lastName
    return UpdateUserRequest(firstName, lastName, email, userId)
}

fun convertUserResponse(userResponse: UserResponse?) : User? {
    var user: User? = null
    try {
        if (userResponse != null) {
            Log.d("Utils", "Convert $userResponse object to User.")
            val dietaryPreferences: MutableList<FoodType> = mutableListOf()
            if (userResponse.dietaryPreferences != null) {
                for (tag in userResponse.dietaryPreferences) {
                    val dietPref = FoodType.from(tag.label)
                    if (dietPref != null) {
                        dietaryPreferences.add(dietPref)
                        Log.d("Utils", "User convert dietry prefs to $dietPref")
                    }
                }
            } else {
                Log.e("Utils", "User dietary preferences are null.")
            }
            var settings: Settings? = null
            if (userResponse.userSettings != null) {
                val reminder = userResponse.userSettings.remindBeforeProductExpiration
                val suggestion = userResponse.userSettings.suggestProductsForShoppingList
                val pushNotification = userResponse.userSettings.allowPushNotifications
                settings = Settings(reminder, pushNotification, suggestion)
                Log.e("Utils", "Converted user setting $settings.")
            }

            var userImage: Image? = null
            if (userResponse.userImage != null) {
                var nbitmap: Bitmap? = null
                if(!userResponse.userImage.base64ImageString.isNullOrEmpty()){
                    val byteArray = base64ToBytes(userResponse.userImage.base64ImageString)
                    if(byteArray != null) {
                        nbitmap = convertByteArrayToBitmap(byteArray)
                        Log.e("Utils", "Converted to bitmap $nbitmap.")
                    }else{
                        Log.e("Utils", "User image byte array is empty!")
                    }
                }
                userImage = Image(
                    imageId = userResponse.userImage.imageId,
                    imageName = userResponse.userImage.imageName,
                    imageUrl = userResponse.userImage.imageUrl,
                    bitmap = nbitmap
                )
            }else{
                Log.e("Utils", "User image is empty.")
            }

            user = User(
                userId = userResponse.userId,
                firstName = userResponse.firstName,
                lastName = userResponse.lastName,
                email = userResponse.email,
                password = userResponse.password,
                userSettings = settings,
                dietaryPreferences = dietaryPreferences,
                userImage = userImage
            )
            Log.d("Utils", "Konvertierter User $user")
        } else {
            Log.d("Utils", "UserResponse is null!")
        }
    }catch(e: Exception){
        e.stackTrace
        Log.d("Utils", "Exception $e")
    }
    return user
}
fun convertExpiringProductList(expProductListResponse: ExpiringProductResponse?) : List<Product>{
    val products: MutableList<Product> = mutableListOf()
    try {
        if(expProductListResponse!= null && expProductListResponse.isNotEmpty()) {
            for(product in expProductListResponse) {
                var image: Image? = null
                val productTags: MutableList<Label> = mutableListOf()
                if (product.productImage != null) {
                    // only convert image if available
                    var nbitmap: Bitmap? = null
                    if(!product.productImage.base64ImageString.isNullOrEmpty()){
                        val byteArray = base64ToBytes(product.productImage.base64ImageString)
                        if(byteArray != null) {
                            nbitmap = convertByteArrayToBitmap(byteArray)
                        }
                    }
                    image = Image(
                        imageId = product.productImage.imageId,
                        imageName = product.productImage.imageName,
                        imageUrl = product.productImage.imageUrl,
                        bitmap = nbitmap
                    )
                }
                if (product.productTags != null) {
                    for (tag in product.productTags) {
                        val nlabel = Label.from(tag.label)
                        if (nlabel != null) {
                            productTags.add(nlabel)
                        }else{
                            Log.d("Utils", "Label is null.")
                        }
                    }
                    val nProduct = Product(
                        productId = product.productId,
                        productName = product.productName,
                        expirationDate = convertDateFormat(product.expirationDate),
                        labels = productTags,
                        quantity = product.quantity,
                        manufacturer = product.manufacturer,
                        nutritionValue = product.nutritionValue,
                        productImage = image
                    )
                    products.add(nProduct)
                }
            }
        }
    } catch (e: Exception) {
        e.stackTrace
        Log.e("Convert expiring products.", "Error $e")
    }
    return products
}

fun convertRecipeResponse(recipeListResponse: RecipeListResponse?) : List<Recipe> {
    val recipes: MutableList<Recipe> = mutableListOf()

    try {
        if(recipeListResponse != null && recipeListResponse.isNotEmpty()) {
            Log.d("Utils", "Convert ${recipeListResponse.size} objects to Recipelist.")
            for(recipe in recipeListResponse) {

                Log.d("Utils", "Convert $recipe object to Recipe.")
                val ingredients = recipe.ingredients
                val nIngredients: MutableList<Product> = mutableListOf()
                if (ingredients != null && ingredients.isNotEmpty()) {
                    Log.d("Utils", "Convert $ingredients object to Ingredient.")
                    for (ingredient in ingredients) {
                        val nIngredient = Product(
                            productId = ingredient.ingredientId,
                            productName = ingredient.ingredientName,
                            expirationDate = "",
                            labels = null,
                            quantity = ingredient.quantity,
                            manufacturer = "",
                            nutritionValue = "",
                            productImage = null
                        )
                        nIngredients.add(nIngredient)
                    }
                }

                val nRecipe = Recipe(
                    recipeId = recipe.recipeId,
                    recipeName = recipe.recipeName,
                    recipeLabel = recipe.recipeLabel,
                    recipeNutrition = recipe.recipeNutrition,
                    description = recipe.description,
                    cummulativeRating = recipe.cummulativeRating,
                    amountOfRatings = recipe.amountOfRatings,
                    difficulty = recipe.difficulty,
                    cookingTime = recipe.cookingTime,
                    instructions = recipe.instructions,
                    imageUrl = recipe.imageUrl,
                    ingredients = nIngredients.toList()
                )
                recipes.add(nRecipe)
            }
        }
    } catch (e: Exception) {
        e.stackTrace
        Log.e("Convert Recipe", "Error $e")
    }
    return recipes
}

fun convertBarcodeResultToProduct(product: BarcodeProductResponse?): Product? {
    var nProduct: Product? = null
    try{
        // create new image th
        val productImage = Image(
            imageId = 0,
            imageName = "product image",
            imageUrl = "",
            bitmap = null
        )
        // create product
        val labels: MutableList<Label> = arrayListOf()
        nProduct = Product(
            productId = 0,
            productName = product?.label ?: "Please insert name.",
            expirationDate = "Please insert date.",
            labels = labels,
            quantity = "1 piece",
            manufacturer = product?.brand ?: "Please insert manufacturer.",
            nutritionValue = product?.nutrients ?: " kcal",
            productImage = productImage
        )
    } catch (e: Exception) {
        e.stackTrace
        Log.e("Convert Barcode", "Error $e")
    }
    return nProduct
}

fun convertInventoryListResponse(inventoryListResponse: InventoryListResponse?) : List<Product>{
    val products: MutableList<Product> = mutableListOf()
    try{
        if(inventoryListResponse != null) {
            Log.d("Utils", "Convert $inventoryListResponse object to Productlist.")
            if (inventoryListResponse.inventoryList.isNotEmpty()) {
                for (product in inventoryListResponse.inventoryList) {
                    Log.d("Utils", "Convert ${product.productId} object to Product.")
                    val nImage = createProductImg(product)
                    val productTags = createProductTags(product)
                    val nProduct = Product(
                        productId = product.productId,
                        productName = product.productName,
                        expirationDate = convertDateFormat(product.expirationDate),
                        labels = productTags,
                        quantity = product.quantity,
                        manufacturer = product.manufacturer,
                        nutritionValue = product.nutritionValue,
                        productImage = nImage
                    )
                    products.add(nProduct)
                }
            }
        }
    } catch (e: Exception) {
        e.stackTrace
        Log.e("Convert Inventorylist", "Error $e")
    }
    return products
}

private fun createProductTags(product: ProductResponse): MutableList<Label> {
    val productLabels = mutableListOf<Label>()
    if (product.productTags != null) {
        for (tag in product.productTags) {
            val nlabel = Label.from(tag.label)
            if (nlabel != null) {
                productLabels.add(nlabel)
            }else{
                Log.d("Utils", "Label is null.")
            }
        }
    }
    return productLabels
}

private fun createProductImg(product: ProductResponse): Image?{
    var nImage: Image? = null
    if (product.productImage != null) {
        // only convert image if available
        var nBitmap: Bitmap? = null
        if(!product.productImage.base64ImageString.isNullOrEmpty()){
            val byteArray = base64ToBytes(product.productImage.base64ImageString)
            if(byteArray != null) {
                nBitmap = convertByteArrayToBitmap(byteArray)
            }
        } else{
            Log.d("Utils", "No bitmap found.")
        }
        nImage = Image(
            imageId = product.productImage.imageId,
            imageName = product.productImage.imageName,
            imageUrl = product.productImage.imageUrl,
            bitmap = nBitmap
        )
    }
    return nImage
}

fun convertShoppingListResponse(shoppingListResponse: ShoppingListResponse?) : List<Product> {
    val products: MutableList<Product> = mutableListOf()
    try{
        if (shoppingListResponse != null) {
            Log.d("Utils", "Convert ShoppingList $shoppingListResponse object to Shoppinglist.")
            if (shoppingListResponse.shoppingList.isNotEmpty()) {
                for (product in shoppingListResponse.shoppingList) {
                    Log.d("Utils", "Convert $product object to Product.")
                    val newProduct = Product(
                        productId = product.productId,
                        productName = product.productName,
                        expirationDate = convertDateFormat(product.expirationDate),
                        labels = null,
                        quantity = product.quantity,
                        manufacturer = product.manufacturer,
                        nutritionValue = product.nutritionValue,
                        productImage = null
                    )
                    products.add(newProduct)
                }
            }
        }
    } catch (e: Exception) {
        e.stackTrace
        Log.e("Convert Shoppinglist", "Error $e")
    }
    Log.d("Utils","Konvertierte Shoppingliste: $products")
    return products
}

fun convertProductRequest(userId: Int, product: Product): ProductRequest?{
    var productRequest: ProductRequest? = null
    val productTags: MutableList<ProductTagRequest> = mutableListOf()
    val productImage: ImageRequest?
    var newTag: ProductTagRequest?

    try {
        //product
        val productId = product.productId
        val productName = product.productName
        val quantity = product.quantity
        var expirationDate = product.expirationDate
        val nutritionValue = product.nutritionValue
        val manufacturer = product.manufacturer
        val image = product.productImage
        val labels: MutableList<Label>? = product.labels

        if(expirationDate.isEmpty()){
            expirationDate = "2022-02-02"
        }else{
            expirationDate = reconvertDateFormat(product.expirationDate)
        }

        if(image != null){
            val newImageId = image.imageId
            val newImageName = image.imageName
            val newImageUrl = image.imageUrl
            val newImageBitmap = image.bitmap
            var newImageBase64Str: String? = null
            if(newImageBitmap != null){
                val newImageByteArray: ByteArray? = convertBitmapToByteArray(newImageBitmap)
                newImageBase64Str = bytesToBase64(newImageByteArray)
            }
            productImage = ImageRequest(newImageId, newImageName, newImageUrl, newImageBase64Str)
        }else{
            productImage = ImageRequest(productId, "product image", "", null)
        }

        if(labels != null) {
            for(label in labels){
                newTag = ProductTagRequest(0, label.toString())
                productTags.add(newTag)
            }
        }else{
            newTag = ProductTagRequest(0, "")
            productTags.add(newTag)
        }

        productRequest = ProductRequest(
            userId,
            productId,
            productName,
            expirationDate,
            quantity,
            manufacturer,
            nutritionValue,
            productImage,
            productTags
        )
        Log.d("Utils", "Converted added product to $productRequest")
    } catch(e: Exception){
        e.stackTrace
        Log.e("Convert Shoppinglist Product", "Error $e")
    }
    return productRequest
}

fun convertProductWithNewProductId(productIdResult: ProductIdResponse, product: Product): Product {
    return Product(
        productIdResult.productId,
        product.productName,
        product.expirationDate,
        product.labels,
        product.quantity,
        product.manufacturer,
        product.nutritionValue,
        product.productImage)
}

fun convertShoppingListProductIDRequest(userId: Int, product: Product): ShoppingListProductIDRequest {
    return ShoppingListProductIDRequest(userId, product.productId)
}

// extract needed user information and omit password
fun extractNecessaryUserData(user: UserResponse): UserResponse {
    return UserResponse(
        userId = user.userId,
        firstName = user.firstName,
        lastName = user.lastName,
        email = user.email,
        password = "",
        userSettings = user.userSettings,
        userImage = user.userImage
    )
}

fun convertUserDataToUser(user: UserResponse): User?{
    var result: User? = null
    try{
        result = convertUserResponse(user)
    }catch (e: Exception){
        e.printStackTrace()
    }
    return result
}