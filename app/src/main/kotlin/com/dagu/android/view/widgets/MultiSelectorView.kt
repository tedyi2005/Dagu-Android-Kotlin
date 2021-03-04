package com.dagu.android.view.widgets

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.dagu.android.R
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.MultiClickItemViewBinding


class MultiSelectorView(context: Context?) : FrameLayout(context!!) {
    private var binding: MultiClickItemViewBinding =
        MultiClickItemViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun display(singleViewItem: SingleViewItem, isSelected: Boolean) {
        binding.multiSelectName.text = singleViewItem.itemName
        display(isSelected)
    }

    private fun display(isSelected: Boolean) {
        binding.multiSelectName.background = if (isSelected)
            ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
        else
            ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)
    }

}