package com.dagu.android.data.authentication.model
import com.google.gson.annotations.SerializedName


data class OloUserContactDetailsResponse(
    @SerializedName("contactdetails")
    val contactDetails: String? // 1234567890
)