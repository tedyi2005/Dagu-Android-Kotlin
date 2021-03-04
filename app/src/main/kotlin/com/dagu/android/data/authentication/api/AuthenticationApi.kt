package com.dagu.android.data.authentication.api

import com.dagu.android.data.authentication.model.UserAuthentication
import okhttp3.ResponseBody
import retrofit2.http.*

interface AuthenticationApi {

    @FormUrlEncoded
    @POST("token/")
    suspend fun getToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("username", encoded = true) username: String,
        @Field("password") password: String
    ): UserAuthentication

    @FormUrlEncoded
    @POST("token/")
    suspend fun getRefreshToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") password: String
    ): UserAuthentication

    @FormUrlEncoded
    @POST("login/google/")
    suspend fun googleSignIn(
        @Header("Authorization") authorization: String,
        @Header("Platform-Version") platformVersion: String = "1.7.4", // TODO update with version
        @Header("Platform-OS") platformOS: String = "android",
        @Field("grant_type", encoded = true) grantType: String,
        @Field("id_token",encoded = true) tokenId: String
    ): UserAuthentication

    @FormUrlEncoded
    @POST("forgotten/")
    suspend fun requestPasswordRecovery(
        @Header("Authorization") authorization: String,
        @Field("email", encoded = true) email: String
    ):ResponseBody

    @FormUrlEncoded
    @POST("social/facebook/")
    suspend fun loginWithFacebook(
        @Header("Authorization") authorization: String,
        @Header("Platform-Version") platformVersion: String = "1.7.4", // TODO update with version
        @Header("Platform-OS") platformOS: String = "android", // TODO update later
        @Field("grant_type", encoded = true) grantType: String = "implicit",
        @Field("access_token", encoded = true) accessToken: String): UserAuthentication

}
