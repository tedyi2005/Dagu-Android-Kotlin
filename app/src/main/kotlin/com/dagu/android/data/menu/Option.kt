package com.dagu.android.data.menu

import com.google.gson.annotations.SerializedName
import com.dagu.android.util.Constants

class Option {

    @SerializedName("id")
    val id: Int? = null

    @SerializedName("name")
    val name: String? = null

    @SerializedName("cost")
    val cost: Double? = null

    @SerializedName("adjustsparentcalories")
    val adjustsParentCalories: Boolean? = null

    @SerializedName("adjustsparentprice")
    val adjustsParentPrice: Boolean? = null

    @SerializedName("basecalories")
    val baseCalories: String? = null

    @SerializedName("caloriesseparator")
    val caloriesSeparator: Any? = null

    @SerializedName("chainoptionid")
    val chainOptionId: Int? = null

    @SerializedName("children")
    val children: Boolean? = null

    @SerializedName("isdefault")
    val isDefault: Boolean? = null

    @SerializedName("categorized_options")
    var categorizedOptions: List<CategorizedOption>? = null

    fun getDisplayCost(): String? {
        var bestCostForDisplay = cost
        if (bestCostForDisplay == 0.0) {
            categorizedOptions?.let {
                for (categorizedOption in it) {
                    if (categorizedOption.type.equals(Constants.FLAVOR)) {
                        bestCostForDisplay = categorizedOption.options!![0].cost
                        break
                    } else if (categorizedOption.type.equals(Constants.SIZE)) {
                        bestCostForDisplay = categorizedOption.options!![0].cost
                        break
                    }
                }
            }
        }
        return "$" + bestCostForDisplay.toString()
    }

    fun getDisplayCalories(): String? {
        var baseCaloriesForDisplay = baseCalories
        if (baseCaloriesForDisplay.isNullOrEmpty()) {

            categorizedOptions?.let {
                for (categorizedOption in it) {
                    if (categorizedOption.type.equals(Constants.FLAVOR)) {
                        baseCaloriesForDisplay = categorizedOption.options!![0].baseCalories
                        break
                    } else if (categorizedOption.type.equals(Constants.SIZE)) {
                        baseCaloriesForDisplay = categorizedOption.options!![0].baseCalories
                        break
                    }
                }
            }
        }
        if (baseCaloriesForDisplay == null)
            return ""
        return "  .  $baseCaloriesForDisplay  cals"
    }

    fun toProduct(): Product {
        return Product(
            name = name,
            cost = cost,
            baseCalories = baseCalories
        )
    }
}
