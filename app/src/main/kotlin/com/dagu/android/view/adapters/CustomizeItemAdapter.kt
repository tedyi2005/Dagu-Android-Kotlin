package com.dagu.android.view.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.pdp.MakeItBetterItem
import com.dagu.android.databinding.MakeItBetterItemBinding

class CustomizeItemAdapter(private val context: Context, private val list: List<MakeItBetterItem>) :
    RecyclerView.Adapter<CustomizeItemAdapter.CustomizeItemHolderView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizeItemHolderView {
        val itemBinding =
            MakeItBetterItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CustomizeItemHolderView(itemBinding)
    }

    override fun onBindViewHolder(holder: CustomizeItemHolderView, position: Int) {
        val itemResponse = list[position]
        holder.bind(itemResponse, position, context)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class CustomizeItemHolderView(private val itemBinding: MakeItBetterItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        private val selectedItems = arrayListOf<Int>()

        fun bind(makeItBetterItem: MakeItBetterItem, position: Int, context: Context) {
            itemBinding.image.visibility = View.GONE
            itemBinding.itemName.text = makeItBetterItem.itemName
            itemBinding.priceAndCalories.text = makeItBetterItem.itemPrice

            // for every item, check to see if it exists in the selected items array
            if (selectedItems.contains(position)) {
                // if the item is selected, let the user know by adding a dark layer above it
                itemBinding.addItem.setImageResource(R.drawable.icon_selected_option_green)
                itemBinding.item.background =
                    ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
            } else {
                // else, keep it as it is
                itemBinding.addItem.setImageResource(R.drawable.icon_unselected_option_gray)
                itemBinding.item.background =
                    ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)
            }

            itemBinding.item.setOnClickListener {
                selectItem(position, context)
            }
        }

        private fun selectItem(position: Int, context: Context) {
            // If the "selectedItems" list contains the item, remove it and set it's state to normal
            if (selectedItems.contains(position)) {
                selectedItems.remove(position)
                itemBinding.addItem.setImageResource(R.drawable.icon_unselected_option_gray)
                itemBinding.item.background =
                    ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)
            } else {
                // Else, add it to the list and add a darker shade over the image, letting the user know that it was selected
                selectedItems.add(position)
                itemBinding.addItem.setImageResource(R.drawable.icon_selected_option_green)
                itemBinding.item.background =
                    ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
            }
        }
    }
}

