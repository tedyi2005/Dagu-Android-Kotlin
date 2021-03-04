package com.dagu.android.data.order

import com.dagu.android.data.order.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class OrderRepository @Inject constructor(
    private val orderApi: OrderApi,
) {

    private val _tray = MutableStateFlow<Tray>(Tray())
    val tray: StateFlow<Tray> get() = _tray

    suspend fun createTray(vendorId: Int, authToken: String, oloKey: String) {
        val response = orderApi.createTray(oloKey, TrayBody(vendorId, authToken))
        _tray.value = response

    }

    suspend fun createTrayFromOrder(
        basketOrderBody: TrayOrderBody,
        oloKey: String,
        oloAuthToken: String
    ) {
        val response = orderApi.createTrayFromOrder(
            oloKey, oloAuthToken,
            basketOrderBody
        )
        _tray.value = response
    }

    suspend fun createTrayFromFavorite(
        basketFavoriteBody: TrayFavoriteBody,
        oloKey: String,
        oloAuthToken: String
    ) {
        val response = orderApi.createTrayFromFavorite(
            oloKey, oloAuthToken,
            basketFavoriteBody
        )
        _tray.value = response
    }

    suspend fun setPickupTime(
        basketTimeWantedBody: TrayTimeWantedBody,
        oloKey: String,
        basketID: String
    ) {
        val response =
            orderApi.createTrayFromTimeWanted(basketID, oloKey, basketTimeWantedBody)
        _tray.value = response
    }

    suspend fun getTray(oloKey: String, basketID: String) {
        val response = orderApi.getTray(basketID, oloKey)
        _tray.value = response
    }

    suspend fun addCoupon(
        promoCode: String,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.addCoupon(basketID, oloKey, CouponBody(promoCode))
        _tray.value = response
    }

    suspend fun validateTray(oloKey: String, basketID: String) {
        val response = orderApi.validateTray(basketID, oloKey)
        _tray.value = response
    }

    suspend fun batchAddProductToTray(
        batchAddProductBody: BatchAddProductBody,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.batchAddProduct(basketID, oloKey, batchAddProductBody)
        _tray.value = response
    }

    suspend fun addProductInTray(
        productBody: ProductBody,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.addProduct(basketID, oloKey, productBody)
        _tray.value = response
    }

    suspend fun updateProductInTray(
        productBody: ProductBody,
        oloKey: String,
        productID: Int,
        basketID: String
    ) {
        val response = orderApi.updateProduct(basketID, productID, oloKey, productBody)
        _tray.value = response
    }

    suspend fun deleteProductInTray(
        productBody: ProductBody,
        oloKey: String,
        basketID: String,
        recentTrayId: Int
    ) {
        val response = orderApi.deleteProduct(basketID, recentTrayId, oloKey, productBody)
        _tray.value = response
    }


    suspend fun setDeliveryMode(
        deliveryModeBody: DeliveryModeBody,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.setDeliveryMode(basketID, oloKey, deliveryModeBody)
        _tray.value = response
    }


    suspend fun setDeliveryAddress(
        deliveryAddressBody: DeliveryAddressBody,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.setDeliveryAddress(basketID, oloKey, deliveryAddressBody)
        _tray.value = response
    }

    suspend fun setCustomField(
        customFieldBody: CustomFieldBody,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.setCustomField(basketID, oloKey, customFieldBody)
        _tray.value = response
    }

    suspend fun setTipAmount(
        amount: Double,
        oloKey: String,
        basketID: String
    ) {
        val response = orderApi.setTip(basketID, oloKey, TipBody(amount))
        _tray.value = response
    }
}