package com.dagu.android.data.menu

import com.google.gson.annotations.SerializedName

data class MenuCategory (

    @SerializedName("image")
    var image: String? = null,

    @SerializedName("capacity")
    var capacity: Int? = null,

    @SerializedName("category_olo_id")
    var categoryOloId: Int? = null,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("description")
    var description: String? = null,

    @SerializedName("platform")
    var platform: String? = null,

    @SerializedName("products")
    var products: List<Product>? = null
)