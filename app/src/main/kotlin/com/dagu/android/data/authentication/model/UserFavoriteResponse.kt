package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName


data class UserFavoriteResponse(

    @SerializedName("faves") val faves: List<Faves>
)

data class FavoriteChoices(

    @SerializedName("name") val name: String,
    @SerializedName("optionid") val optionId: Int,
    @SerializedName("quantity") val quantity: Int
)

data class Faves(

    @SerializedName("disabled") val disabled: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("isdefault") val isDefault: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("online") val online: Boolean,
    @SerializedName("products") val products: List<FavoriteProducts>,
    @SerializedName("vendorid") val vendorId: Int,
    @SerializedName("vendorname") val vendorName: String
)

data class FavoriteProducts(

    @SerializedName("choices") val choices: List<FavoriteChoices>,
    @SerializedName("disabled") val disabled: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("productid") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("recipient") val recipient: String,
    @SerializedName("specialinstructions") val specialInstructions: String
)