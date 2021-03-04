package com.dagu.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import com.google.gson.JsonParser
import com.dagu.android.R
import com.dagu.android.databinding.BubbleToastLayoutBinding
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

fun Activity.makeStatusBarTransparent() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            statusBarColor = Color.TRANSPARENT
        }
    }
}

fun View.setMarginTop(marginTop: Int) {
    val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
    menuLayoutParams.setMargins(0, marginTop, 0, 0)
    this.layoutParams = menuLayoutParams
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

/**
 * Extension method to convert part of a TextView matching with a given string into a link
 *
 * @param links     a vararg of Pair objects consisting of a String to be matched on the TextView
 * which will be converted into a link and its corresponding View.OnClickListener
 */
fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                // use this to change the link color
                textPaint.color = textPaint.linkColor
                // toggle below value to enable/disable
                // the underline shown below the clickable text
                textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                link.second.onClick(view)
            }
        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
//      if(startIndexOfLink == -1) continue // todo if you want to verify your texts contains links text
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun View.show() {
    visibility = View.VISIBLE
}

fun show(vararg views: View) {
    views.forEach { it.show() }
}

fun View.hide() {
    visibility = View.GONE
}

fun hide(vararg views: View) {
    views.forEach { it.hide() }
}

fun showShortToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun toggleRuleStatus(context: Context, ruleTextView: TextView, isValid: Boolean) {
    ruleTextView.apply {
        if (isValid) {
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_general_check_small, 0, 0, 0
            )
            compoundDrawables[0]?.setTint(
                ResourcesCompat.getColor(resources, R.color.primary_green, null)
            )
        } else {
            setTextColor(ContextCompat.getColor(context, R.color.grey_disabled_button))
            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_general_circle_small, 0, 0, 0
            )
            compoundDrawables[0]?.setTint(
                ResourcesCompat.getColor(resources, R.color.grey_disabled_button, null)
            )
        }
    }
}

fun togglePasswordVisibility(
    context: Context,
    passwordEditText: EditText,
    passwordTextView: TextView,
    toggle: Boolean
) {
    if (toggle) {
        passwordEditText.transformationMethod =
            HideReturnsTransformationMethod.getInstance()
        passwordTextView.text = context.getString(R.string.hide_password)
        passwordTextView.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.ic_eye_visibility_hidden, 0
        )
    } else {
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        passwordTextView.text = context.getString(R.string.show_password)
        passwordTextView.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, R.drawable.ic_eye_visibility_visible, 0
        )
    }
    passwordEditText.setSelection(passwordEditText.length())
}


/**
 * Retrieve a color from the current [android.content.res.Resources.Theme].
 */
@ColorInt
@SuppressLint("Recycle")
fun Context.themeColor(
    @AttrRes themeAttrId: Int
): Int {
    return obtainStyledAttributes(
        intArrayOf(themeAttrId)
    ).use {
        it.getColor(0, Color.MAGENTA)
    }
}

fun Throwable.getApiErrorMessage(): String {
    return if (this is HttpException) {
        val errorJsonString = this.response()?.errorBody()?.string()
        val parsedString = JsonParser().parse(errorJsonString)
        parsedString.asJsonObject["error"].asString
    } else {
        this.message!!
    }
}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDate(format: String): String {
    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
    return simpleDateFormat.format(Calendar.getInstance().time)
}

fun getYesterdayDate(format: String): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
    return simpleDateFormat.format(calendar.time)
}

fun compareDates(firstStringDate: String, secondStringDate: String): Int? {
    // compare day only
    val format = SimpleDateFormat("dd", Locale.getDefault())
    val firstDate = format.parse(firstStringDate)
    val secondDate = format.parse(secondStringDate)
    return firstDate?.compareTo(secondDate)
}

// Returns a Double as a USD cost with two decimals (i.e: "$20.36")
fun Double.toDisplayCost(): String {
    return "$" + String.format("%.2f", this)
}

fun Context.showToast(message: String, duration: Int) {
    Toast.makeText(this, message, duration).show()
}

fun showLongBubbledToast(context: Context, message: String) {
    val bubbleToastBinding = BubbleToastLayoutBinding.inflate(LayoutInflater.from(context))
    bubbleToastBinding.message.text = message
    val toast = Toast(context)
    toast.duration = Toast.LENGTH_LONG
    // gravity, xOffset, yOffset
    toast.setGravity(Gravity.BOTTOM, 0, 0)
    toast.view = bubbleToastBinding.root //setting the view of custom toast layout
    toast.show()
}

fun String.appendDollarSign(): String {
    return if (this.isNullOrEmpty() || this == "0")
        "$0.0"
    else
        "$$this"
}