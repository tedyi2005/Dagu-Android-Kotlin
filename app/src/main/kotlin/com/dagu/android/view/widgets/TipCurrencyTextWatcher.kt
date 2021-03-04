package com.dagu.android.view.widgets

import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import androidx.annotation.NonNull
import com.dagu.android.util.Utils
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*


class TipCurrencyTextWatcher constructor(@NonNull textInputEditText: EditText) : TextWatcher {
    private var numberFormat: NumberFormat? = null
    private var editText: EditText? = null
    private var maxDecimalValue: BigDecimal? = null
    private var valueBeforeChange: BigDecimal? = null
    private val focusChangeListener: OnFocusChangeListener

    companion object {
        private const val MAX_TIP_LIMIT = 99999.99
    }

    init {
        // Set max value limit
        val maxValue = BigDecimal(MAX_TIP_LIMIT)
        focusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                Handler().post { editText?.text?.let { editText?.setSelection(it.length) } }
            }
        }
        editText = textInputEditText
        editText?.onFocusChangeListener = focusChangeListener
        editText?.inputType = editText?.inputType?.or(2)!!
        // get $ currency format
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        numberFormat = currencyFormat
        maxDecimalValue = maxValue.setScale(numberFormat!!.maximumFractionDigits, 4)
    }

    override fun beforeTextChanged(text: CharSequence, start: Int, count: Int, after: Int) {
        if (text.isNotEmpty()) {
            valueBeforeChange = BigDecimal(Utils.stripNonNumericChars(text.toString()))
            valueBeforeChange =
                valueBeforeChange!!.setScale(numberFormat!!.maximumFractionDigits, 4)
                    .divide(BigDecimal(100))
        } else {
            valueBeforeChange = BigDecimal.ZERO
        }
    }

    override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(editable: Editable) {
        editText?.removeTextChangedListener(this)

        var value: BigDecimal = if (editable.isEmpty())
            BigDecimal(0)
        else
            BigDecimal(Utils.stripNonNumericChars(editable.toString()))

        value = value.setScale(numberFormat!!.maximumFractionDigits, 4)
            .divide(BigDecimal(100))
        if (maxDecimalValue != null && value > maxDecimalValue)
            editText?.setText(numberFormat!!.format(valueBeforeChange))
        else
            editText?.setText(numberFormat!!.format(value))

        editText?.text?.let { editText?.setSelection(it.length) }
        editText?.addTextChangedListener(this)
    }
}
