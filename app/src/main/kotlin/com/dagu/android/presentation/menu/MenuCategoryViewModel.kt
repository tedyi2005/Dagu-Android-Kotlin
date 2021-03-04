package com.dagu.android.presentation.menu

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagu.android.BuildConfig
import com.dagu.android.data.menu.MenuCategory
import com.dagu.android.data.menu.ProductMenuRepository
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MenuCategoryViewModel @ExperimentalCoroutinesApi
@ViewModelInject constructor(
    private val productMenuRepository: ProductMenuRepository
) : ViewModel() {

// TODO update with selected location when hooking up with real location API data
val demoVendorId = "14631"

// TODO use this location if we don't know the user's location and we don't have a previous order to go off of
 val corporateVendorId = BuildConfig.CORPORATE_SHACK_VENDOR_ID

    private val _menuCategory = MutableLiveData<Result<List<MenuCategory>>>()
    val menuCategory: LiveData<Result<List<MenuCategory>>>
        get() = _menuCategory

    private val _serverError = MutableLiveData("")
    val serverError: LiveData<String> get() = _serverError

    init {
        viewModelScope.launch {
            try {
                productMenuRepository.getProductMenu(demoVendorId)
                _menuCategory.value = productMenuRepository.productMenu.value
            } catch (e: Exception) {
                _serverError.value = "An error occurred while trying to fetch the product list: $e.localizedMessage"
            }
        }
    }
}