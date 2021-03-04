package com.dagu.android.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.content.ContextCompat
import com.dagu.android.R
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.SingleClickItemViewBinding

class SingleSelectionAdapter(
    val context: Context,
    private val itemList: ArrayList<SingleViewItem>
) :
    BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var selectedPosition: Int = -1

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any {
        return itemList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val data = this.itemList[position]

        val binding = SingleClickItemViewBinding.inflate(inflater, parent, false)
        binding.name.text = data.itemName

        if (data.isValueVisible!!) {
            binding.price.text = data.itemValue
        }

        if (position == selectedPosition)
            binding.item.background = ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
        else
            binding.item.background =
                ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)

        return binding.root
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
    }

    fun getSelectedPosition(): Int {
        return selectedPosition
    }
}
