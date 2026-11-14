package com.mobilesystems.feedme.di

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mobilesystems.feedme.BuildConfig
import com.mobilesystems.feedme.common.backgroundService.PushWorker
import com.mobilesystems.feedme.common.backgroundService.RecipeWorker
import com.mobilesystems.feedme.common.config.Constants
import com.mobilesystems.feedme.common.logging.NetworkLogInterceptor
import com.mobilesystems.feedme.common.logging.RequestIdInterceptor
import com.mobilesystems.feedme.data.datasource.AuthDataSource
import com.mobilesystems.feedme.data.datasource.AuthDataSourceImpl
import com.mobilesystems.feedme.data.datasource.InventoryDataSource
import com.mobilesystems.feedme.data.datasource.InventoryDataSourceImpl
import com.mobilesystems.feedme.data.datasource.RecipeDataSource
import com.mobilesystems.feedme.data.datasource.RecipeDataSourceImpl
import com.mobilesystems.feedme.data.datasource.ShoppingListDataSource
import com.mobilesystems.feedme.data.datasource.ShoppingListDataSourceImpl
import com.mobilesystems.feedme.data.datasource.UserDataSource
import com.mobilesystems.feedme.data.datasource.UserDataSourceImpl
import com.mobilesystems.feedme.data.datasource.WorkerDataSource
import com.mobilesystems.feedme.data.datasource.WorkerDataSourceImpl
import com.mobilesystems.feedme.data.remote.FoodTrackerApi
import com.mobilesystems.feedme.data.repository.AuthRepository
import com.mobilesystems.feedme.data.repository.AuthRepositoryImpl
import com.mobilesystems.feedme.data.repository.DashboardRepository
import com.mobilesystems.feedme.data.repository.DashboardRepositoryImpl
import com.mobilesystems.feedme.data.repository.InventoryRepository
import com.mobilesystems.feedme.data.repository.InventoryRepositoryImpl
import com.mobilesystems.feedme.data.repository.RecipeRepository
import com.mobilesystems.feedme.data.repository.RecipeRepositoryImpl
import com.mobilesystems.feedme.data.repository.ShoppingListRepository
import com.mobilesystems.feedme.data.repository.ShoppingListRepositoryImpl
import com.mobilesystems.feedme.data.repository.UserRepository
import com.mobilesystems.feedme.data.repository.UserRepositoryImpl
import com.mobilesystems.feedme.data.repository.WorkerRepository
import com.mobilesystems.feedme.data.repository.WorkerRepositoryImpl
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
 * @Module says that this class holds the dependencies that Hilt gives to
 * the other classes, for example the instance of Retrofit.
 *
 * @InstallIn says how long a dependency lives. SingletonComponent means that
 * the application holds one instance for its whole life.
 *
 *
 * Tutorial/Further resources: https://www.ericthecoder.com/2021/09/13/getting-started-with-dagger-hilt-in-an-mvvm-app-android-dependency-injection-in-2021/
 *                             https://zaidzakir.medium.com/a-simple-android-app-using-mvvm-dagger-hilt-e9f45381f1bc
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Constants reads the address out of BuildConfig, so the value is no
    // constant of the compiler.
    private val BASE_URL = Constants.BASE_URL

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

    /**
     * Writes the full request and the full answer to Logcat.
     *
     * The body can hold a password or a token, so the interceptor writes the
     * body in a debug build only. A release build gets the level NONE, and
     * [NetworkLogInterceptor] then writes one line for each request.
     *
     * @return the interceptor of the library OkHttp
     */
    @Singleton
    @Provides
    @Named("loggingInterceptor")
    fun providesHttpLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level =
                if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            // The header holds the token of the user. Logcat must not show it.
            redactHeader("Authorization")
            redactHeader("Cookie")
        }

    /**
     * Makes the HTTP client of the application.
     *
     * The order of the interceptors is important. [RequestIdInterceptor] runs
     * first and gives the request its ID, so the two other interceptors can
     * write that ID.
     *
     * @param loggingInterceptor interceptor that writes the body in a debug
     *                           build
     * @return the client that Retrofit uses
     */
    @Singleton
    @Provides
    fun providesOkHttpClient(
        @Named("loggingInterceptor") loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.MILLISECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
            .addInterceptor(RequestIdInterceptor())
            .addInterceptor(NetworkLogInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
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
    fun provideAuthDataSource(
        api: FoodTrackerApi,
        defaultDispatcher: CoroutineDispatcher,
        ioDispatcher: CoroutineDispatcher,
    ): AuthDataSource = AuthDataSourceImpl(api, defaultDispatcher, ioDispatcher)

    @Singleton
    @Provides
    fun provideAuthRepository(dataSource: AuthDataSourceImpl): AuthRepository = AuthRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideInventoryDataSource(api: FoodTrackerApi): InventoryDataSource = InventoryDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideInventoryRepository(inventoryDataSource: InventoryDataSourceImpl): InventoryRepository =
        InventoryRepositoryImpl(inventoryDataSource)

    @Singleton
    @Provides
    fun provideRecipeDataSource(api: FoodTrackerApi): RecipeDataSource = RecipeDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideRecipeRepository(
        recipeDataSource: RecipeDataSourceImpl,
        shoppingDataSource: ShoppingListDataSourceImpl,
    ): RecipeRepository = RecipeRepositoryImpl(recipeDataSource, shoppingDataSource)

    @Singleton
    @Provides
    fun provideShoppingListDataSource(api: FoodTrackerApi): ShoppingListDataSource = ShoppingListDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideShoppingListRecipeRepository(dataSource: ShoppingListDataSourceImpl): ShoppingListRepository =
        ShoppingListRepositoryImpl(dataSource)

    @Singleton
    @Provides
    fun provideDashboardRepository(
        inventoryDataSource: InventoryDataSourceImpl,
        recipeDataSourceImpl: RecipeDataSourceImpl,
        userDataSourceImpl: UserDataSourceImpl,
    ): DashboardRepository = DashboardRepositoryImpl(inventoryDataSource, recipeDataSourceImpl, userDataSourceImpl)

    @Singleton
    @Provides
    fun provideWorkerDataSource(api: FoodTrackerApi): WorkerDataSource = WorkerDataSourceImpl(api)

    @Singleton
    @Provides
    fun provideWorkerRepository(workerDataSource: WorkerDataSourceImpl): WorkerRepository =
        WorkerRepositoryImpl(workerDataSource)

    @Singleton
    @Provides
    fun provideRecipeWorker(
        appContext: Context,
        workerParams: WorkerParameters,
        workerRepository: WorkerRepositoryImpl,
    ): CoroutineWorker = RecipeWorker(appContext, workerParams, workerRepository)

    @Singleton
    @Provides
    fun providePushWorker(
        appContext: Context,
        workerParams: WorkerParameters,
        workerRepository: WorkerRepositoryImpl,
    ): CoroutineWorker = PushWorker(appContext, workerParams, workerRepository)
}
