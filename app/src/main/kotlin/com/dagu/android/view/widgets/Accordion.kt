package com.dagu.android.view.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.res.getStringOrThrow
import androidx.core.content.withStyledAttributes
import com.dagu.android.R
import com.dagu.android.databinding.ViewAccordionBinding

/**
 * General purpose accordion which can be styled via different heading levels. Stick whatever views
 * you want into it and they will stack vertically. The accordion will hide or show the child content
 * on click.
 *
 * There are currently two heading levels.
 *   1: – e.g. the menu link in the main menu nav drawer
 *   2: - e.g. accordions in the PDP
 *
 * Attributes:
 *   - headingText: Text to display above the accordion content
 *                  Default: ""
 *   - headingLevel: Controls the look of the heading. See above for supported heading levels
 *                   and examples
 *                   Default: 1
 *   - isExpanded: Whether the accordion is currently expanded. Use this if you need to display
 *                 the accordion as expanded by default. Most accordions will be collapsed by default
 *                 Default: false
 */
class Accordion @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var binding: ViewAccordionBinding
    private val contentView: LinearLayout?

    // View Attributes
    private var headingText: String = ""
    private var headingLevel: Int = 1
    private var isExpanded: Boolean = false

    init {
        isClickable = true
        binding = ViewAccordionBinding.inflate(LayoutInflater.from(context), this, true)
        contentView = binding.content

        context.withStyledAttributes(attrs, R.styleable.Accordion) {
            headingText = getStringOrThrow(R.styleable.Accordion_headingText)
            headingLevel = getInt(R.styleable.Accordion_headingLevel, 1)
            isExpanded = getBoolean(R.styleable.Accordion_isExpanded, false)
        }

        binding.apply {
            headingText.text = this@Accordion.headingText
            render()
        }
    }

    private fun render() {
        renderHeading()
        renderExpansionState()
    }

    private fun renderHeading() {
        val heading = binding.headingText
        val headingLayoutParams: LayoutParams = heading.layoutParams as LayoutParams

        when(headingLevel) {
            1 -> {
                headingLayoutParams.width = LayoutParams.WRAP_CONTENT
            }
            2-> {
                headingLayoutParams.width = 0
                headingLayoutParams.weight = 1f
            }
        }

        heading.layoutParams = headingLayoutParams
    }

    private fun expandOrCollapse() {
        isExpanded = !isExpanded
        renderExpansionState()
    }

    private fun renderExpansionState() {
        // Expand or collapse the accordion
        when(isExpanded) {
            true -> {
                binding.content.visibility = VISIBLE
                binding.headingChevron.setImageResource(R.drawable.ic_chevron_up_black)
            }
            false -> {
                binding.content.visibility = GONE
                binding.headingChevron.setImageResource(R.drawable.ic_chevron_down_black)
            }
        }
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (contentView == null) {
            super.addView(child, index, params)
        } else {
            contentView.addView(child, index, params)
        }
    }

    override fun performClick(): Boolean {
        if (super.performClick()) return true

        expandOrCollapse()
        return false
    }
}