package com.dagu.android.data.order.model

import com.google.gson.annotations.SerializedName


data class Tray(

    @SerializedName("allowstip") val allowsTip: Boolean = false,
    @SerializedName("appliedrewards") val appliedRewards: List<String> = listOf(),
    @SerializedName("contactnumber") val contactNumber: String = "",
    @SerializedName("contextualpricing") val contextualPricing: String = "",
    @SerializedName("coupon") val coupon: String = "",
    @SerializedName("coupondiscount") val couponDiscount: Int = 0,
    @SerializedName("customerhandoffcharge") val customerHandOffCharge: Double = 0.0,
    @SerializedName("customfields") val customFields: List<CustomFields> = listOf(),
    @SerializedName("deliveryaddress") val deliveryAddress: DeliveryAddress? = DeliveryAddress(),
    @SerializedName("deliverymode") val deliveryMode: String = "",
    @SerializedName("discount") val discount: Int = 0,
    @SerializedName("discounts") val discounts: List<String> = listOf(),
    @SerializedName("earliestreadytime") val earliestReadyTime: String = "",
    @SerializedName("fees") val fees: List<Fees> = listOf(),
    @SerializedName("id") val id: String = "",
    @SerializedName("isupsellenabled") val isUpSellEnabled: Boolean = false,
    @SerializedName("leadtimeestimateminutes") val leadTimeEstimateMinutes: Int = 0,
    @SerializedName("mode") val mode: String = "",
    @SerializedName("products") val products: List<String> = listOf(),
    @SerializedName("salestax") val salesTax: Double = 0.0,
    @SerializedName("subtotal") val subtotal: Double = 0.0,
    @SerializedName("suggestedtipamount") val suggestedTipAmount: Double = 0.0,
    @SerializedName("suggestedtippercentage") val suggestedTipPercentage: Double = 0.0,
    @SerializedName("taxes") val taxes: List<Taxes> = listOf(),
    @SerializedName("timemode") val timeMode: String = "",
    @SerializedName("timewanted") val timeWanted: String = "",
    @SerializedName("tip") val tip: Double = 0.0,
    @SerializedName("total") val total: Double = 0.0,
    @SerializedName("totalfees") val totalFees: Double = 0.0,
    @SerializedName("validationmessages") val validationMessages: List<String> = listOf(),
    @SerializedName("vendorid") val vendorID: Int = 0,
    @SerializedName("vendoronline") val vendorOnline: Boolean = false,
    @SerializedName("wasupsold") val wasUpSold: Boolean = false
)


data class Taxes(

    @SerializedName("label") val label: String,
    @SerializedName("tax") val tax: Double
)

data class CustomFields(

    @SerializedName("id") val id: Int,
    @SerializedName("isrequired") val isRequired: Boolean,
    @SerializedName("label") val label: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("validationregex") val validationRegex: String,
    @SerializedName("value") val value: String
)

data class Fees(

    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("note") val note: String
)

data class TrayBody(
    @SerializedName("vendorid") val vendorID: Int,
    @SerializedName("authtoken") val authToken: String
)

data class TrayOrderBody(
    @SerializedName("id") val id: String,
    @SerializedName("orderref") val orderRef: String,
    @SerializedName("ignoreunavailableproducts") val ignoreUnavailableProduct: Boolean
)

data class TrayFavoriteBody(
    @SerializedName("faveID") val id: String,
    @SerializedName("ignoreunavailableproducts") val ignoreUnavailableProduct: Boolean
)

data class TrayTimeWantedBody(
    @SerializedName("ismanualfire") val isManualFire: Boolean,
    @SerializedName("year") val year: Int,
    @SerializedName("month") val month: Int,
    @SerializedName("day") val day: Int,
    @SerializedName("hour") val hour: Int,
    @SerializedName("minute") val minute: Int,
)

data class CouponBody(
    @SerializedName("couponcode") val couponCode: String
)

data class DeliveryModeBody(
    @SerializedName("deliverymode") val deliveryMode: String
)

data class CustomFieldBody(
    @SerializedName("id") val id: Int,
    @SerializedName("value") val value: String
)

data class DeliveryAddressBody(
    @SerializedName("streetaddress") val streetAddress: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("zipcode") val zipCode: String = "",
    @SerializedName("phonenumber") val phoneNumber: String = "",
    @SerializedName("isdefault") val isDefault: Boolean = false,
)

data class TipBody(
    @SerializedName("amount") val amount: Double
)


data class SubmitTrayBody(

    @SerializedName("billingmethod") val billingMethod: String,
    @SerializedName("usertype") val userType: String,
    @SerializedName("cardnumber") val cardNumber: Int,
    @SerializedName("expiryyear") val expiryYear: Int,
    @SerializedName("expirymonth") val expiryMonth: Int,
    @SerializedName("cvv") val cvv: Int,
    @SerializedName("zip") val zip: Int,
    @SerializedName("country") val country: String,
    @SerializedName("saveonfile") val saveOnFile: Boolean,
    @SerializedName("firstname") val firstName: String,
    @SerializedName("lastname") val lastName: String,
    @SerializedName("emailaddress") val emailAddress: String
)

data class DeliveryAddress(

    @SerializedName("building") val building: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("id") val id: Int = 0,
    @SerializedName("isdefault") val isDefault: Boolean = false,
    @SerializedName("phonenumber") val phoneNumber: String = "",
    @SerializedName("specialinstructions") val specialInstructions: String = "",
    @SerializedName("streetaddress") val streetAddress: String = "",
    @SerializedName("zipcode") val zipCode: Int = 0
)


