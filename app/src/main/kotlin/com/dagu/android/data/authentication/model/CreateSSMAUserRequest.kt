package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName

data class CreateSSMAUserRequest(
    @SerializedName("needs_to_enter_new_password") val needsToEnterNewPassword: Boolean = false,
    @SerializedName("grant_flow_type") val grantFlowType: String = "password",
    @SerializedName("user") val user: SSMAUser,
    @SerializedName("fav_item_chain_id") val favItemChainId: String? = null
)

data class SSMAUser(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)

data class CreateSSMAUserError(
    @SerializedName("error") val error: Error
)

data class Error(@SerializedName("user") val userError: UserError)

data class UserError(@SerializedName("username") val userName: List<String>)