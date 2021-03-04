package com.dagu.android.data.location

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface LocationApi {

    @GET("locations")
    suspend fun getLocations(
        @Header("Authorization") authorization: String,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("placeId") placeId: String? = null,
        @Query("regionID") regionID: String? = null,
        @Query("channel") channel: String? = null
    ): LocationListResponse

    @GET("locations/{id}")
    suspend fun getSingleLocation(
        @Header("Authorization") authorization: String,
        @Path("id") locationId: Int,
        @Query("channel") channel: String?
    ): LocationSingleResponse

    @GET("regions")
    suspend fun getRegions(@Header("Authorization") authorization: String): RegionResponse

    @GET("places")
    suspend fun searchLocation(
        @Header("Authorization") authorization: String,
        @Query("input") input: String
    ): GooglePlacesResponse
}