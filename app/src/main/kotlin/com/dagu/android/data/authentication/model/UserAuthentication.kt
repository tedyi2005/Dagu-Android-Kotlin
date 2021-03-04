package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName

data class UserAuthentication(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("scope")
    val scope: String
)