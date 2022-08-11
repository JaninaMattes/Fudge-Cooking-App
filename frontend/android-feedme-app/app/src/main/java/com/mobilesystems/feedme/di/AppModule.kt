package com.mobilesystems.feedme.di

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobilesystems.feedme.common.backgroundService.PushWorker
import com.mobilesystems.feedme.common.backgroundService.RecipeWorker
import com.mobilesystems.feedme.common.config.Constants
import com.mobilesystems.feedme.data.datasource.*
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * The DI Package - Dependency Injection
 * The module class AppModule provides dependencies to other classes within its component’s scope.
 * Thereby we can differentiate between:
 *
 * @Module- denotes that this class are containers for dependencies that has to be injected Example: RetrofitInstance, RoomDBInstance ,
 * and also we specify amount of time that this will live in application lifecycle.
 *
 * @InstallIn()- tells dagger how long the scope of the module should live for , Here are some lifecycle scopes for InstallIn()
 *
 *
 * Tutorial/Further resources: https://www.ericthecoder.com/2021/09/13/getting-started-with-dagger-hilt-in-an-mvvm-app-android-dependency-injection-in-2021/
 *                             https://zaidzakir.medium.com/a-simple-android-app-using-mvvm-dagger-hilt-e9f45381f1bc
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val baseUrl = Constants.BASE_URL

    // Add the provisioning here
    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class IoDispatcher

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class MainDispatcher

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DefaultDispatcher

    @Singleton
    @Provides
    @Named("loggingInterceptor")
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Singleton
    @Provides
    fun providesOkHttpClient() : OkHttpClient{
        return OkHttpClient
            .Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideFoodTrackerApi(retrofit: Retrofit): FoodTrackerApi {
        return retrofit.create(FoodTrackerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideUserDataSource(api: FoodTrackerApi): UserDataSource = UserDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideUserRepository(dataSource: UserDataSourceImpl): UserRepository = UserRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideAuthDataSource(api: FoodTrackerApi, defaultDispatcher: CoroutineDispatcher,
                              ioDispatcher: CoroutineDispatcher): AuthDataSource = AuthDataSourceImpl(api, defaultDispatcher, ioDispatcher)

    @Singleton
    @Provides
    fun provideAuthRepository(dataSource: AuthDataSourceImpl): AuthRepository = AuthRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideInventoryDataSource(api: FoodTrackerApi): InventoryDataSource = InventoryDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideInventoryRepository(inventoryDataSource: InventoryDataSourceImpl):
            InventoryRepository = InventoryRepositoryImpl(inventoryDataSource)
    
    @Singleton
    @Provides
    fun provideRecipeDataSource(api: FoodTrackerApi): RecipeDataSource = RecipeDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideRecipeRepository(recipeDataSource: RecipeDataSourceImpl, shoppingDataSource: ShoppingListDataSourceImpl): RecipeRepository = RecipeRepositoryImpl(recipeDataSource, shoppingDataSource)

    @Singleton
    @Provides
    fun provideShoppingListDataSource(api: FoodTrackerApi): ShoppingListDataSource = ShoppingListDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideShoppingListRecipeRepository(dataSource: ShoppingListDataSourceImpl): ShoppingListRepository = ShoppingListRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideDashboardRepository(inventoryDataSource: InventoryDataSourceImpl, recipeDataSourceImpl: RecipeDataSourceImpl,
                                   userDataSourceImpl: UserDataSourceImpl): DashboardRepository =
        DashboardRepositoryImpl(inventoryDataSource, recipeDataSourceImpl, userDataSourceImpl)

    @Singleton
    @Provides
    fun provideWorkerDataSource(api: FoodTrackerApi): WorkerDataSource = WorkerDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideWorkerRepository(workerDataSource: WorkerDataSourceImpl): WorkerRepository = WorkerRepositoryImpl(workerDataSource)

    @Singleton
    @Provides
    fun provideRecipeWorker(appContext: Context, workerParams: WorkerParameters, workerRepository: WorkerRepositoryImpl) : CoroutineWorker = RecipeWorker(appContext, workerParams, workerRepository)

    @Singleton
    @Provides
    fun providePushWorker(appContext: Context, workerParams: WorkerParameters, workerRepository: WorkerRepositoryImpl) : CoroutineWorker = PushWorker(appContext, workerParams, workerRepository)

}
