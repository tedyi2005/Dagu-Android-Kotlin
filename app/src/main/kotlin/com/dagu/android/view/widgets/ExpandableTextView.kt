package com.dagu.android.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.dagu.android.R

class ExpandableTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val title: TextView
    private val description: TextView
    private val plus: View
    private val moreDetails: View
    private var collapse = false
    fun setText(title: String?, text: String?) {
        resetViews()
        if (text == null) {
            return
        }
        visibility = View.VISIBLE
        this.title.text = title
        description.text = text
    }

    private fun resetViews() {
        visibility = View.GONE
        description.visibility = View.GONE
        moreDetails.visibility = View.GONE
    }

    override fun onClick(view: View) {
        if (collapse) {
            plus.rotation = 0F
            description.visibility = View.GONE
            moreDetails.visibility = View.GONE
            collapse = false
        } else {
            plus.rotation = 180F
            description.visibility = View.VISIBLE
            moreDetails.visibility = View.VISIBLE
            collapse = true
        }
    }

    init {
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.expandable_text, this, true)
        title = findViewById<View>(R.id.title) as TextView
        description = findViewById<View>(R.id.description) as TextView
        plus = findViewById(R.id.plus)
        moreDetails = findViewById(R.id.more_details)
        setOnClickListener(this)
        moreDetails.setOnClickListener {
            // Action on "More details" text
        }
    }
}
