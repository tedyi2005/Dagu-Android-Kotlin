package com.dagu.android.data.authentication.api

import com.dagu.android.data.authentication.model.*

import retrofit2.http.*

interface UserAccountApi {

    @POST("userprofiles/")
    suspend fun createSSMAUser(
        @Header("Authorization") authorization: String,
        @Header("Platform-Version") platformVersion: String = "1.7.4", // TODO update with version
        @Header("Platform-OS") platformOS: String = "android", // TODO update later
        @Body createSSMAUserBody: CreateSSMAUserRequest
    ): UserData

    @POST("userprofiles/lookup/")
    suspend fun userProfileLookup(
        @Header("Authorization") authorization: String,
        @Body userProfileLookupRequest: UserProfileLookupRequest
    ): UserProfileLookupResponse

    @PATCH("userprofiles/{id}")
    suspend fun userProfileUpdate(
        @Header("Authorization") authorization: String,
        @Path("id") userID: Int?,
        @Body userUpdateBody: UpdateUserRequest
    ): UserData

    @GET("me/")
    suspend fun getUserData(
        @Header("Authorization") authorization: String
    ): UserData
}