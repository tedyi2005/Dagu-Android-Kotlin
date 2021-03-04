package com.dagu.android.data.payment

data class PaymentMethod(
    val name: String,
    val lastFour: String,
    val default: Boolean,
    val expired: Boolean
)