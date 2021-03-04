package com.dagu.android.data.authentication.api


import com.dagu.android.data.authentication.model.*
import retrofit2.http.*

interface OloUserAccountApi {
    @POST("users/getorcreate/")
    suspend fun getOrCreateOloUser(
        @Query("key") oloKey: String,
        @Body oloAuthenticationRequest: OloAuthenticationRequest
    ): OloAuthenticationResponse

    @PUT("users/{oloAuthToken}/contactdetails/")
    suspend fun updateOloContactDetails(
        @Path("oloAuthToken") oloAuthToken: String,
        @Query("key") oloKey: String,
        @Body updateOloContactDetailsRequest: UpdateOloContactDetailsRequest
    ): OloUserContactDetailsResponse

    @GET("users/{oloAuthToken}/contactdetails/")
    suspend fun getOloContactDetails(
        @Path("oloAuthToken") oloAuthToken: String,
        @Query("key") oloKey: String
    ): OloUserContactDetailsResponse

    @GET("users/{oloAuthToken}/faves/")
    suspend fun getOloUserFaves(
        @Path("oloAuthToken") oloAuthToken: String,
        @Query("key") oloKey: String
    ): UserFavoriteResponse

    @GET("users/{oloAuthToken}/recentorders/")
    suspend fun getOloUserRecentOrders(
        @Path("oloAuthToken") oloAuthToken: String,
        @Query("key") oloKey: String
    ): RecentOrderResponse
}
