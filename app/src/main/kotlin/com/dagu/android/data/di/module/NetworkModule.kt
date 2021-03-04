package com.dagu.android.data.di.module

import com.google.gson.GsonBuilder
import com.dagu.android.data.authentication.api.AuthenticationApi
import com.dagu.android.data.authentication.api.OloUserAccountApi
import com.dagu.android.data.authentication.api.UserAccountApi
import com.dagu.android.data.location.LocationApi
import com.dagu.android.data.menu.ProductMenuApi
import com.dagu.android.data.order.OrderApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object NetworkModule {

    @Provides
    @OtherRetrofitClient
    @Singleton
    fun getClient(): Retrofit {
        val builder = GsonBuilder().setLenient().create()
        val baseUrl = "https://staging.ssma24.com/v1.0/"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder))
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .build()
    }

    @Provides
    @AuthRetrofitClient
    @Singleton
    fun getAuthClient(): Retrofit {
        val builder = GsonBuilder().setLenient().create()
        val baseUrl = "https://staging.ssma24.com/oauth/"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder))
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .build()
    }

    @Provides
    @OloRetrofitClient
    @Singleton
    fun getOloClient(): Retrofit {
        val builder = GsonBuilder().setLenient().create()
        val baseUrl = "https://ordering.api.olosandbox.com/v1.1/"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder))
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .build()
    }

    @Provides
    @LocalHostClient
    @Singleton
    fun getLocalHost(): Retrofit {
        val builder = GsonBuilder().setLenient().create()
        // TODO: Remember to change this to your mock API local IP:port configuration.
        val baseUrl = "http://10.0.2.2:3001/"
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder))
            .baseUrl(baseUrl)
            .client(getHttpClient())
            .build()
    }

    private fun getHttpClient(): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        val timeOut: Long = 30
        builder.addInterceptor(httpLoggingInterceptor).addInterceptor { chain ->
            val request = chain.request()
            chain.proceed(request)
        }
            .followRedirects(false)
            .connectTimeout(timeOut, TimeUnit.SECONDS)
            .writeTimeout(timeOut, TimeUnit.SECONDS)
            .readTimeout(timeOut, TimeUnit.SECONDS)
            .build()

        return builder.build()
    }

    // API related dependencies

    @Provides
    fun getAuthenticationAPI(@AuthRetrofitClient retrofit: Retrofit): AuthenticationApi =
        retrofit.create(AuthenticationApi::class.java)

    @Provides
    fun getUserAccountAPI(@OtherRetrofitClient retrofit: Retrofit): UserAccountApi =
        retrofit.create(UserAccountApi::class.java)

    @Provides
    fun getOloUserAccountAPI(@OloRetrofitClient retrofit: Retrofit): OloUserAccountApi =
        retrofit.create(OloUserAccountApi::class.java)

    @Provides
    fun getLocationAPI(@LocalHostClient retrofit: Retrofit): LocationApi =
        retrofit.create(LocationApi::class.java)

    @Provides
    fun getOrderAPI(@OloRetrofitClient retrofit: Retrofit): OrderApi =
        retrofit.create(OrderApi::class.java)

    @Provides
    fun getProductMenuAPI(@OtherRetrofitClient retrofit: Retrofit): ProductMenuApi =
        retrofit.create(ProductMenuApi::class.java)

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OtherRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OloRetrofitClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LocalHostClient