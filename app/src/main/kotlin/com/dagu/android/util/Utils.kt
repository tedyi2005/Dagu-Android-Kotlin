package com.dagu.android.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Patterns
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dagu.android.R
import java.io.IOException
import java.util.*
import androidx.annotation.Nullable
import java.text.SimpleDateFormat

class Utils {

    companion object {

        private val fixedWhiteGreenColorList = listOf(R.color.primary_green, R.color.white)
        private const val ORDINAL_INDICATOR_TH = "th"
        private const val ORDINAL_INDICATOR_RD = "rd"
        private const val ORDINAL_INDICATOR_ND = "nd"
        private const val ORDINAL_INDICATOR_ST = "st"

        fun getJsonDataFromAsset(context: Context, fileName: String): String? {
            val jsonString: String
            try {
                jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                return null
            }
            return jsonString
        }

        fun getDevicePixelWidth(context: Context): Int {
            val metrics = context.resources.displayMetrics
            return metrics.widthPixels
        }

        fun getDevicePixelHeight(context: Context): Int {
            val metrics = context.resources.displayMetrics
            return metrics.heightPixels
        }

        fun getDeviceAspectRatio(context: Context): Double {
            val metrics = context.resources.displayMetrics
            return metrics.heightPixels.toDouble() / metrics.widthPixels.toDouble()
        }

        fun getDrawableFromFileName(fileName: String, context: Context): Drawable? {
            val resourceId =
                context.resources.getIdentifier(fileName, "drawable", context.packageName)
            return context.getDrawable(resourceId)
        }

        fun setTextOrHide(newText: String?, textView: TextView) {
            if (newText.isNullOrBlank()) {
                textView.visibility = View.GONE
            } else {
                textView.text = newText
                textView.visibility = View.VISIBLE
            }
        }

        // Get random color (green or white)
        private fun getRandomWhiteAndGreenColor(): Int {
            // get random background color
            return Random().nextInt(fixedWhiteGreenColorList.size)
        }

        fun setRandomWhiteAndGreenColorScheme(context: Context, textView: TextView) {
            getRandomWhiteAndGreenColor().let { backgroundColor ->
                if (backgroundColor == 0)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            context, fixedWhiteGreenColorList[backgroundColor + 1]
                        )
                    )
                else
                    textView.setTextColor(
                        ContextCompat.getColor(
                            context, fixedWhiteGreenColorList[backgroundColor - 1]
                        )
                    )
                textView.setBackgroundResource(fixedWhiteGreenColorList[backgroundColor])
            }

        }

        fun stripNonNumericChars(@Nullable string: String?): String? {
            return string?.replace("\\D".toRegex(), "")
        }

        fun isValidEmail(email: String?): Boolean {
            return if (email.isNullOrEmpty())
                false
            else
                Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun getInitials(firstName: String?, lastName: String?): String {
            return "${(firstName?.firstOrNull())?:""}${(lastName?.firstOrNull())?:""}"
        }

        private fun getOrdinalIndicator(date: Date): String {
            val day = Calendar.getInstance().apply { time = date }.get(Calendar.DAY_OF_MONTH)
            if (day == 11 || day == 12 || day == 13) {
                return ORDINAL_INDICATOR_TH
            }
            return when (day % 10) {
                1 -> ORDINAL_INDICATOR_ST
                2 -> ORDINAL_INDICATOR_ND
                3 -> ORDINAL_INDICATOR_RD
                else -> ORDINAL_INDICATOR_TH
            }
        }

        fun formatOrderDate(dateString: String): String {
            val inputFormat = SimpleDateFormat(Constants.SS_DATE_FORMAT, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            val dayOrdinalIndicator = getOrdinalIndicator(date!!)
            return inputFormat.apply {
                applyPattern("EEEE, MMM d'$dayOrdinalIndicator at' hh:mm a")
            }.format(date)
        }
    }
}
