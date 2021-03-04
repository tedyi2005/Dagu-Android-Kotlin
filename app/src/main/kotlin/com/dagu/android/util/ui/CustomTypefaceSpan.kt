package com.dagu.android.util.ui

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

/**
 * Utility class to allow setting custom fonts to a SpannableString.
 * Source: https://stackoverflow.com/a/63683960
 */

class CustomTypefaceSpan(private val newType: Typeface, private val useBold: Boolean? = false) :
    TypefaceSpan("") {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType, useBold)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType, useBold)
    }

    companion object {
        private fun applyCustomTypeFace(paint: Paint, tf: Typeface, useBold: Boolean? = false) {
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0
            val fake = oldStyle and tf.style.inv()
            if (useBold == true || (fake and Typeface.BOLD != 0)) {
                paint.isFakeBoldText = true
            }
            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }
            paint.typeface = tf
        }
    }
}