package com.dagu.android.data.menu

import com.dagu.android.BuildConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.dagu.android.data.repository.Result
import okhttp3.Credentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class ProductMenuRepository @Inject constructor(private val productMenuApi: ProductMenuApi) {

    private val basicAuthHeader: String by lazy {
        Credentials.basic(BuildConfig.SSMA_CLIENT_ID, BuildConfig.SSMA_CLIENT_SECRET)
    }

    private val _productMenu = MutableStateFlow<Result<List<MenuCategory>>>(Result.Loading(true))
    val productMenu: StateFlow<Result<List<MenuCategory>>> get() = _productMenu

    suspend fun getProductMenu(locationId: String) {
        _productMenu.value = Result.Loading(true)
        val result = productMenuApi.getMenusForLocation(basicAuthHeader, locationId)
        _productMenu.value = Result.Loading(false)
        _productMenu.value = Result.Success(result)
    }
}