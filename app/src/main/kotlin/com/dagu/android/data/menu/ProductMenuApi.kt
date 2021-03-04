package com.dagu.android.data.menu

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ProductMenuApi {

    @GET("locations/{locationId}/menus")
    suspend fun getMenusForLocation(
        @Header("Authorization") authorization: String,
        @Path("locationId") locationId: String): List<MenuCategory>
}