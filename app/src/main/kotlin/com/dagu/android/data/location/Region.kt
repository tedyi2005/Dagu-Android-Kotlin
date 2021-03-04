package com.dagu.android.data.location

import com.google.gson.annotations.SerializedName

data class RegionResponse(@SerializedName("result") val regions: List<Region>)

data class Region(

    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("count") val count: Int? = null,
    @SerializedName("ssmaId") val ssmaId: Int? = null
)