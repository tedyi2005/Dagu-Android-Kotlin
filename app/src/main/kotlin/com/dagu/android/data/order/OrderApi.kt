package com.dagu.android.data.order


import com.dagu.android.data.order.model.*
import retrofit2.http.*

interface OrderApi {
    @POST("baskets/create")
    suspend fun createTray(
        @Query("key") oloKey: String,
        @Body trayBody: TrayBody
    ): Tray

    @POST("baskets/createfromorder")
    suspend fun createTrayFromOrder(
        @Query("key") oloKey: String,
        @Query("authtoken") authToken: String,
        @Body trayBody: TrayOrderBody
    ): Tray

    @POST("baskets/createfromfave")
    suspend fun createTrayFromFavorite(
        @Query("key") oloKey: String,
        @Query("authtoken") authToken: String,
        @Body trayBody: TrayFavoriteBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/timewanted")
    suspend fun createTrayFromTimeWanted(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body trayBody: TrayTimeWantedBody
    ): Tray

    @GET("baskets/{olo_basket_id}")
    suspend fun getTray(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
    ): Tray

    //Ask about valid code
    @POST("baskets/{olo_basket_id}/coupon")
    suspend fun addCoupon(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body couponBody: CouponBody
    ): Tray

    @POST("baskets/{olo_basket_id}/products/batch")
    suspend fun batchAddProduct(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body batchAddProductBody: BatchAddProductBody
    ): Tray

    @POST("baskets/{olo_basket_id}/products")
    suspend fun addProduct(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body productBody: ProductBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/products/{product_id}")
    suspend fun updateProduct(
        @Path("olo_basket_id") oloTrayID: String,
        @Path("product_id") productId: Int,
        @Query("key") oloKey: String,
        @Body productBody: ProductBody
    ): Tray

    @DELETE("baskets/{olo_basket_id}/products/{recent_basket_product_id}")
    suspend fun deleteProduct(
        @Path("olo_basket_id") oloTrayID: String,
        @Path("recent_basket_product_id") recentTrayProductId: Int,
        @Query("key") oloKey: String,
        @Body productBody: ProductBody
    ): Tray

    @POST("baskets/{olo_basket_id}/validate")
    suspend fun validateTray(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
    ): Tray

    @POST("baskets/{olo_basket_id}/submit")
    suspend fun submitTray(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body submitTrayBody: SubmitTrayBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/deliverymode")
    suspend fun setDeliveryMode(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body deliveryModeBody: DeliveryModeBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/customfields")
    suspend fun setCustomField(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body customFieldBody: CustomFieldBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/deliveryaddress")
    suspend fun setDeliveryAddress(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body deliveryAddressBody: DeliveryAddressBody
    ): Tray

    @PUT("baskets/{olo_basket_id}/tip")
    suspend fun setTip(
        @Path("olo_basket_id") oloTrayID: String,
        @Query("key") oloKey: String,
        @Body tipBody: TipBody
    ): Tray


}