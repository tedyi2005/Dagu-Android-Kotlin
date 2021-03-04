package com.dagu.android.data.order.model

import com.google.gson.annotations.SerializedName

data class BatchAddProductBody(

    @SerializedName("products") val products: List<BatchProducts>
)

data class BatchProducts(

    @SerializedName("choices") val choices: List<Choices>,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("productid") val productId: Int
)

data class Choices(

    @SerializedName("quantity") val quantity: Int,
    @SerializedName("choiceid") val choiceId: Int
)

data class ProductBody(
    @SerializedName("productid") val productId: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("options") val options: String
)

