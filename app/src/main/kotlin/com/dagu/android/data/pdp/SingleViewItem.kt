package com.dagu.android.data.pdp

import com.google.gson.annotations.SerializedName

data class SingleViewItem(
    @SerializedName("id") var id: Int?,
    @SerializedName("itemName") var itemName: String?,
    @SerializedName("itemValue") var itemValue: String?,
    @SerializedName("isValueVisible") var isValueVisible: Boolean?
)