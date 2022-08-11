package com.mobilesystems.feedme.data.remote

import com.mobilesystems.feedme.data.request.*
import com.mobilesystems.feedme.data.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 *  The Network Layer with Retrofit Service class.
 *
 *  FoodTrackerApi interface makes use of the Retrofit Http Client library, with: *
 *  @Path	Variable substitution for the API endpoint (i.e. username will be swapped for {username} in the URL endpoint).
 *  @Query	Specifies the query key name with the value of the annotated parameter.
 *  @Body	Payload for the POST call (serialized from a Java object to a JSON string)
 *  @Header	Specifies the header with the value of the annotated parameter
 *
 *  Retrofit Tutorial: https://square.github.io/retrofit/
 *                     https://github.com/square/retrofit/issues/3626
 *                     https://johncodeos.com/how-to-make-post-get-put-and-delete-requests-with-retrofit-using-kotlin/
 *
 * @author Janina Mattes, Patricia Maier
 */
interface FoodTrackerApi {

    /******************************************
     * All requests to user service
     * ****************************************
     */
    @POST("/api/users/login")
    suspend fun loginUser(@Body request: LoginRequest) : Response<Map<String, String>>

    @POST("/api/users/register")
    suspend fun registerUser(@Body request: RegisterRequest) : Response<Map<String, String>>

    @PUT("/api/users/update/logout")
    suspend fun logoutUser(@Body request: ChangeLoginStatusRequest) : Response<Int>

    @PUT("/api/users/update/login")
    suspend fun putLoginUser(@Body request: ChangeLoginStatusRequest) : Response<Int>

    @GET("/api/users/{userId}")
    suspend fun getUserById(@Path("userId") userId: Int) : Response<UserResponse>

    @GET("/api/users/isloggedin/{userId}")
    suspend fun getIsUserLoggedIn(@Path("userId") userId: Int) : Response<Boolean>

    @PUT("/api/users/update/userprofile")
    suspend fun updateUser(@Body request: UpdateUserRequest) : Response<UserIdResponse>

    @PUT("/api/users/update/image")
    suspend fun updateUserImage(@Body request: ImageRequest) : Response<ImageIdResponse>

    @PUT("/api/users/update/pushNotification")
    suspend fun allowPushNotification(@Body allowSettings: UserAllowSettingsRequest): Response<Int>

    @PUT("/api/users/update/reminder")
    suspend fun allowReminder(@Body allowSettings: UserAllowSettingsRequest): Response<Int>

    @PUT("/api/users/update/suggestion")
    suspend fun allowSuggestion(@Body allowSettings: UserAllowSettingsRequest): Response<Int>

    @PUT("/api/users/create/dietrytag")
    suspend fun createDietryTag(@Body request: UserDietryTagRequest): Response<DietryTagResponse>

    @DELETE("/api/users/delete/{userId}")
    suspend fun deleteUser(@Path("userId") userId: Int) : Response<UserIdResponse>


    /***********************************************
     * All requests to recipe service (external api)
     * *********************************************
     */
    @GET("/api/recipe/rest/recipes/{userId}")
    suspend fun getAllRecipesByUserId(@Path("userId") userId: Int) : Response<RecipeListResponse>

    @GET("/api/recipe/rest/recipes/rating/{userId}")
    suspend fun getBestRatedRecipesByUserId(@Path("userId") userId: Int) : Response<RecipeListResponse>

    @GET("/api/recipe/rest/poll/{userId}")
    suspend fun pollAllRecipesAPI(@Path("userId") userId: Int): Response<Int>


    /********************************************
     * All requests to inventory service
     * ******************************************
     */
    @GET("/api/products/all/expiring/{userId}")
    suspend fun getExpiringProductsByUserId(@Path("userId") userId: Int) : Response<ExpiringProductResponse>

    @GET("/api/products/count/expiring/{userId}")
    suspend fun getAmountOfExpiringProductsByUserId(@Path("userId") userId: Int) : Response<Int>

    @GET("/api/products/all/{userId}")
    suspend fun getAllProductsByUserId(@Path("userId") userId: Int) : Response<InventoryListResponse>

    @POST("/api/products/add")
    suspend fun addProductToInventory(@Body request: ProductRequest) : Response<ProductIdResponse>

    @PUT("/api/products/update")
    suspend fun updateProductOnInventory(@Body request: ProductRequest) : Response<ProductIdResponse>

    @PUT("/api/products/update/image")
    suspend fun updateProductImage(@Body request: ImageRequest) : Response<ImageIdResponse>

    @PUT("/api/products/all/update")
    suspend fun updateInventoryList(@Body request: InventoryListRequest) : Response<Int>

    @HTTP(method = "DELETE", path = "/api/products/delete", hasBody = true)
    suspend fun deleteProductFromInventory(@Body request: DeleteProductRequest) : Response<Int>


    /*******************************************
     * Get product values from barcode api
     * *****************************************
     */
    @GET("/api/barcode/rest/{barcodeCode}")
    suspend fun getProductFromBarcode(@Path("barcodeCode") barcodeCode: String) : Response<BarcodeProductResponse>


    /*******************************************
     * All request to shoppinglist service
     * *****************************************
     */
    @GET("/api/shoppinglist/current/{userId}")
    suspend fun loadAllProductsInCurrentShoppingList(@Path("userId") userId: Int): Response<ShoppingListResponse>

    @GET("/api/shoppinglist/old/{userId}")
    suspend fun loadAllProductsInOldShoppingList(@Path("userId") userId: Int): Response<ShoppingListResponse>

    @PUT("/api/shoppinglist/current/update/all")
    suspend fun updateCurrentShoppingList(@Body request: ShoppingListRequest): Response<Int>

    @PUT("/api/shoppinglist/old/update/all")
    suspend fun updateOldShoppingList(@Body request: ShoppingListRequest): Response<Int>

    @POST("/api/shoppinglist/current/create")
    suspend fun createProductToCurrentShoppingList(@Body request: ProductRequest): Response<ProductIdResponse>

    @POST("/api/shoppinglist/current/add")
    suspend fun addProductToCurrentShoppingList(@Body request: ShoppingListProductIDRequest): Response<Int>
    
    @POST("/api/shoppinglist/old/add")
    suspend fun addProductToOldShoppingList(@Body request: ShoppingListProductIDRequest): Response<Int>

    @PUT("/api/shoppinglist/current/update/single")
    suspend fun updateSingleProductOnCurrentShoppingList(@Body request: ProductRequest): Response<Int>

    @PUT("/api/shoppinglist/old/update/single")
    suspend fun updateSingleProductOnOldShoppingList(@Body request: ProductRequest): Response<Int>

    @HTTP(method = "DELETE", path = "/api/shoppinglist/current/delete/single", hasBody = true)
    suspend fun removeProductFromCurrentShoppingList(@Body IDRequest: ShoppingListProductIDRequest): Response<Int>

    @HTTP(method = "DELETE", path = "/api/shoppinglist/old/delete/single", hasBody = true)
    suspend fun removeProductFromOldShoppingList(@Body IDRequest: ShoppingListProductIDRequest): Response<Int>
}