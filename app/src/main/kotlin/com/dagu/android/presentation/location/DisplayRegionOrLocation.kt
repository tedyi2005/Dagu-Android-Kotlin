package com.dagu.android.presentation.location

import java.io.Serializable

data class DisplayRegionOrLocation(
    val regionId: String? = null,
    val placeId: String? = null,
    val name: String,
    val shackCount: Int? = null,
    val locationAddress: String? = null
) : Serializable