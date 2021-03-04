package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName
import com.dagu.android.util.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class RecentOrderResponse(

    @SerializedName("orders") val orders: List<Orders>
)

data class OrderChoices(

    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int
)

data class OrderCustomFields(

    @SerializedName("id") val id: Int,
    @SerializedName("isrequired") val isRequired: Boolean,
    @SerializedName("label") val label: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("validationregex") val validationRegex: String,
    @SerializedName("value") val value: String
)

data class OrderDeliveryAddress(

    @SerializedName("building") val building: String,
    @SerializedName("city") val city: String,
    @SerializedName("id") val id: Int,
    @SerializedName("isdefault") val isDefault: Boolean,
    @SerializedName("phonenumber") val phoneNumber: String,
    @SerializedName("specialinstructions") val specialInstructions: String,
    @SerializedName("streetaddress") val streetAddress: String,
    @SerializedName("zipcode") val zipCode: Int
)

data class OrderDiscount(
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String
)

data class OrderFees(

    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String
)

data class Orders(

    @SerializedName("arrivalstatus") val arrivalStatus: String,
    @SerializedName("billingaccountid") val billingAccountid: String,
    @SerializedName("billingaccountids") val billingAccountids: String,
    @SerializedName("contextualpricing") val contextualPricing: String,
    @SerializedName("customerhandoffcharge") val customerHandOffCharge: Double,
    @SerializedName("customfields") val customFields: List<OrderCustomFields>,
    @SerializedName("deliveryaddress") val deliverAaddress: OrderDeliveryAddress,
    @SerializedName("deliverymode") val deliveryMode: String,
    @SerializedName("discount") val discount: Double,
    @SerializedName("discounts") val discounts: List<OrderDiscount>,
    @SerializedName("fees") val fees: List<OrderFees>,
    @SerializedName("hasolopass") val hasOloPass: Boolean,
    @SerializedName("id") val id: String,
    @SerializedName("iseditable") val isEditable: Boolean,
    @SerializedName("mode") val mode: String,
    @SerializedName("oloid") val oloid: Int,
    @SerializedName("orderref") val orderref: String,
    @SerializedName("posreferenceresponse") val posReferenceResponse: String,
    @SerializedName("products") val products: List<OrderProducts>,
    @SerializedName("readytime") val readyTime: String,
    @SerializedName("salestax") val salesTax: Double,
    @SerializedName("status") val status: String,
    @SerializedName("subtotal") val subtotal: Double,
    @SerializedName("taxes") val taxes: List<OrderTaxes>,
    @SerializedName("timemode") val timeMode: String,
    @SerializedName("timeplaced") val timePlaced: String,
    @SerializedName("tip") val tip: Float,
    @SerializedName("total") val total: Double,
    @SerializedName("totalfees") val totalFees: Double,
    @SerializedName("trackingeventsubscriptionid") val trackingEventSubscriptionId: String,
    @SerializedName("vendorextref") val vendorExtRef: Int,
    @SerializedName("vendorid") val vendorId: Int,
    @SerializedName("vendorname") val vendorName: String,
    @SerializedName("unavailableproducts") val unavailableProducts: List<OrderProducts>
) : Serializable {

    fun getSubtotalPlusTaxes() : Double {
        return subtotal + getAggregatedTaxes()
    }

    fun getAggregatedTaxes() : Double {
        var sumOfTaxes = 0.0
        for (tax in taxes) {
            sumOfTaxes += tax.tax
        }
        return sumOfTaxes
    }

    fun getOrderProductNames(): String {
        val productNames = arrayListOf<String>()
        for (product in products) {
            productNames.add(product.name)
        }
        return productNames.joinToString()
    }

    fun getOrderDate(): String? {
        if (timePlaced.isEmpty())
            return timePlaced

        val currentDate = getCurrentDate(Constants.SS_DATE_FORMAT)
        val yesterdayDate = getYesterdayDate(Constants.SS_DATE_FORMAT)

        var displayDate: String? = null
        when {
            compareDates(currentDate, timePlaced) == 0 -> {
                displayDate = Constants.TODAY
            }
            compareDates(yesterdayDate, timePlaced) == 0 -> {
                displayDate = Constants.YESTERDAY
            }
            displayDate.isNullOrEmpty() -> {
                // Order date
                val inputFormat = SimpleDateFormat(Constants.SS_DATE_FORMAT, Locale.getDefault())
                val orderDate = inputFormat.parse(timePlaced)
                displayDate = orderDate?.toString(Constants.MM_DD_YYYY_FORMAT)
            }
        }

        return displayDate
    }
}

data class OrderProducts(

    @SerializedName("choices") val choices: List<OrderChoices>,
    @SerializedName("custompassthroughdata") val customPassThroughData: String,
    @SerializedName("name") val name: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("specialinstructions") val specialInstructions: String,
    @SerializedName("totalcost") val totalCost: Double
) {

    fun getExtras(): String {
        val extras = arrayListOf<String>()
        for (choice in choices) {
            extras.add(choice.name)
        }
        return extras.joinToString()
    }
}

data class OrderTaxes(

    @SerializedName("label") val label: String,
    @SerializedName("tax") val tax: Double
)