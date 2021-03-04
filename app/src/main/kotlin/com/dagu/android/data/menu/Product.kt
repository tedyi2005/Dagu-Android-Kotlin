package com.dagu.android.data.menu

import com.google.gson.annotations.SerializedName
import com.dagu.android.util.Constants
import java.io.Serializable
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

// We need this to be Serializable so we can pass product object to the PDP fragment
class Product(

    @SerializedName("kiosk_image")
    val kioskImage: String? = null,

    @SerializedName("images")
    val images: Images? = null,

    @SerializedName("basecalories")
    var baseCalories: String? = null,

    @SerializedName("caloriesseparator")
    val caloriesSeparator: String? = null,

    @SerializedName("chainproductid")
    val chainProductId: Int? = null,

    @SerializedName("cost")
    var cost: Double? = null,

    @SerializedName("base_price")
    val basePrice: Double? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("displayid")
    val displayId: Any? = null,

    @SerializedName("endhour")
    val endHour: Int? = null,

    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("imagefilename")
    val imageFileName: Any? = null,

    @SerializedName("name")
    var name: String? = null,

    @SerializedName("favorite")
    var favorite: Boolean = false,

    @SerializedName("producttags")
    val productTag: String? = null,

    // To store a rotation degree value (e.g 5f, -5f , 10f, etc)
    var currentProductTagRotation: Float = 0f,

    @SerializedName("maxcalories")
    val maxCalories: String? = null,

    @SerializedName("maximumquantity")
    val maximumQuantity: String? = null,

    @SerializedName("minimumquantity")
    val minimumQuantity: String? = null,

    @SerializedName("menuitemlabels")
    val menuItemLabels: List<Any>? = null,

    @SerializedName("starthour")
    val startHour: Int? = null,

    @SerializedName("allergens")
    val allergens: String? = null,

    @SerializedName("goodingredients")
    val goodIngredients: String? = null,

    @SerializedName("platform")
    val platform: String? = null,

    @SerializedName("attributes")
    val attributes: String? = null,

    @SerializedName("categorized_options")
    var categorizedOptions: List<CategorizedOption>? = null
) : Serializable {

    fun advanceProductTagRotation(): Float {
        // fix product tag rotation degree
        val tagDegreeList = listOf(10F, 5F, -5F, -10F)

        // get tag status
        val newRotationValue = if (currentProductTagRotation == 0F) {
            // Random selection of status from list
            val random = Random()
            tagDegreeList[random.nextInt(tagDegreeList.size)]
        } else {
            // get index of current status
            val index = tagDegreeList.indexOf(currentProductTagRotation)
            // if index is last one then circulate to first index
            if (index >= tagDegreeList.size - 1)
                tagDegreeList[0]
            else
            // select next status by list index
                tagDegreeList[index + 1]
        }

        // store current tag status for next call
        currentProductTagRotation = newRotationValue
        return currentProductTagRotation
    }

    fun getCategorizedOptionForType(type: String): CategorizedOption {
        var categorizedItem = CategorizedOption()
        categorizedOptions?.let {
            for (categorizedOption in it) {
                if (categorizedOption.type.equals(type)) {
                    categorizedItem = categorizedOption
                    break
                }
            }
        }
        return categorizedItem
    }

    fun collectFlavouredOptions(): List<Option> {
        val options = ArrayList<Option>()
        categorizedOptions?.let { categorizedOptions ->
            for (categorizedOption in categorizedOptions) {
                when (categorizedOption.type) {
                    CategorizedOption.TYPE_SIZE,
                    CategorizedOption.TYPE_FLAVOR,
                    CategorizedOption.TYPE_SIZE_FLAVOR -> {
                        categorizedOption.options?.let { options.addAll(it) }
                    }
                }
            }
        }
        return options
    }

    fun getIngredientsAndAllergens(): String? {

        val builder = StringBuilder()
        if (goodIngredients != null) {
            builder.append(goodIngredients + "\n\n")
        }

        if (allergens != null) {
            builder.append(allergens)
        }

        return builder.toString()
    }

    fun getBestCostForDisplay(): BigDecimal {
        var bestCostForDisplay = cost
        if (bestCostForDisplay == 0.0) {
            categorizedOptions?.let { categorizedOptions ->
                for (categorizedOption in categorizedOptions) {
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
        return bestCostForDisplay?.toBigDecimal() ?: BigDecimal.ZERO
    }

    fun getDisplayCost(): String {
        return "$" + getBestCostForDisplay().toString()
    }

    fun getDisplayCalories(): String? {
        var baseCaloriesForDisplay = baseCalories
        if (baseCaloriesForDisplay == null || baseCaloriesForDisplay.isEmpty()) {
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
        return "  · $baseCaloriesForDisplay  cals"
    }

    fun getDisplayAttributes(): String {
        return if (attributes.isNullOrEmpty())
            ""
        else
            "  ·  " + attributes.split(",").joinToString()
    }
}
