package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("user")
    val updateBasicUserDataRequest: UpdateBasicUserDataRequest
)

data class UpdateBasicUserDataRequest(
    @SerializedName("first_name")
    val firstName: String? = null,
    @SerializedName("last_name")
    val lastName: String? = null,
    @SerializedName("password")
    var newPassword: String? = null
)

data class UpdateOloContactDetailsRequest(
    @SerializedName("contactdetails")
    val contactDetails: String
)

