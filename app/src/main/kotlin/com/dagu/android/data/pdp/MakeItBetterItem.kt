package com.dagu.android.data.pdp

import com.google.gson.annotations.SerializedName

class MakeItBetterItem(
    @SerializedName("id") var id: Int?,
    @SerializedName("item_image") var itemImage: String?,
    @SerializedName("item_name") var itemName: String?,
    @SerializedName("item_price") var itemPrice: String?
) {

}