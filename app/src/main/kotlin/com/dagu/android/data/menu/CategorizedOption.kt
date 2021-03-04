package com.dagu.android.data.menu

import com.google.gson.annotations.SerializedName

class CategorizedOption {
    companion object {
        const val TYPE_SIZE = "SIZE"
        const val TYPE_FLAVOR = "FLAVOR"
        const val TYPE_SIZE_FLAVOR = "SIZE/FLAVOR"
        const val TYPE_UNCLASSIFIED = "UNCLASSIFIED"
        const val TYPE_ADDITION = "ADDITION"
    }

    @SerializedName("id")
    val id: Int? = null

    @SerializedName("description")
    val description: String? = null

    @SerializedName("type")
    val type: String? = null

    @SerializedName("mandatory")
    val mandatory: Boolean? = null

    @SerializedName("options")
    var options: List<Option>? = null
}