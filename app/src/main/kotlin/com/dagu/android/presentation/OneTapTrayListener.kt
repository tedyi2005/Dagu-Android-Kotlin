package com.dagu.android.presentation

import android.widget.ImageView
import com.dagu.android.data.menu.Option
import com.dagu.android.data.menu.Product

interface OneTapTrayListener {
    fun onProductAdded(product: Product, imageView: ImageView,isDrink:Boolean)
    fun onOptionAdded(option: Option)
}