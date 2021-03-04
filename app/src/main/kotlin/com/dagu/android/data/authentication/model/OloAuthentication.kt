package com.dagu.android.data.authentication.model
import com.google.gson.annotations.SerializedName


data class OloAuthenticationRequest(
    @SerializedName("provider")
    val provider: String?, // fuzz-sm
    @SerializedName("providertoken")
    val providerToken: String?, // {{olo_provider_token}}
    @SerializedName("provideruserid")
    val providerUserId: String? // {{user_email}}
)

data class OloAuthenticationResponse(
    @SerializedName("authorizationcode")
    val authorizationCode: Any?, // null
    @SerializedName("authtoken")
    val authToken: String?, // FCMs9EEz08NFqejEzxaEQmD5Yq52cyHT
    @SerializedName("basketid")
    val basketId: Any?, // null
    @SerializedName("contactnumber")
    val contactNumber: Any?, // null
    @SerializedName("emailaddress")
    val emailAddress: Any?, // null
    @SerializedName("expiresin")
    val expiresIn: Any?, // null
    @SerializedName("firstname")
    val firstName: Any?, // null
    @SerializedName("lastname")
    val lastName: Any?, // null
    @SerializedName("provider")
    val provider: String?, // fuzz-sm
    @SerializedName("providertoken")
    val providerToken: String?, // zCiyqNOyPbPf8EhOfil0YAtKlea5rR
    @SerializedName("provideruserid")
    val providerUserId: String?, // jlbounteous11@gmail.com
    @SerializedName("refreshtoken")
    val refreshToken: Any? // null
)