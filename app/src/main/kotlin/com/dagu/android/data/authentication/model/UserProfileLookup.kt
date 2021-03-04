package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName


data class UserProfileLookupRequest(val email: String)

data class UserProfileLookupResponse(
    @SerializedName("first_name")
    val firstName: String?, // Ben
    @SerializedName("grant_type")
    val grantType: String?, // password
    @SerializedName("provider")
    val provider: Any? // null
)