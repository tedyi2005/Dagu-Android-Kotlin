package com.dagu.android.presentation.menu

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.data.menu.MenuProductItem
import com.dagu.android.data.menu.ProductMenuRepository
import com.dagu.android.util.Utils
import dagger.hilt.android.qualifiers.ActivityContext


class FavoritesViewModel @ViewModelInject constructor(
    private val productMenuRepository: ProductMenuRepository,
    @ActivityContext private val context: Context
) : ViewModel() {

    private val _menuItems = MutableLiveData<List<MenuProductItem>>()
    val menuItems: LiveData<List<MenuProductItem>>
        get() = _menuItems

    fun fetchMenuProducts() {
        // populating static data for now
        val data: String? = Utils.getJsonDataFromAsset(context, "items.json")
        val listType = object : TypeToken<List<MenuProductItem>>() {}.type
        val menuProductItems: List<MenuProductItem> = Gson().fromJson(data, listType)
        _menuItems.value = menuProductItems
    }
}