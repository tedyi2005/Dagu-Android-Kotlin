package com.dagu.android.data.user

data class UserAddress(
    val name: String,
    val firstLine: String,
    val secondLine: String? = null,
    val instructions: String? = null,
)