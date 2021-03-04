package com.dagu.android.data.menu

import com.google.gson.annotations.SerializedName

class MenuProductItem {

    @SerializedName("price")
    var price: String? = null

    @SerializedName("basecalories")
    var baseCalories: String? = null

    @SerializedName("image")
    var image: String? = null

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("minimumquantity")
    var minimumQuantity: String? = null

    @SerializedName("description")
    var description: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("goodingredients")
    var goodIngredients: String? = null

    @SerializedName("favorite")
    var favorite: Boolean = false

    @SerializedName("date_ordered")
    var dateOrdered: String? = null

}