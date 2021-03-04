package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.MenuProductItem
import com.dagu.android.databinding.ItemMenuProductBinding
import com.dagu.android.util.ui.ViewUtils
import com.dagu.android.view.adapters.ItemClickListener


class ProductMenuAdapter(
    private val context: Context,
    private val list: List<MenuProductItem>,
    private val itemClickListener: ItemClickListener
) :
    RecyclerView.Adapter<ProductMenuAdapter.ProductMenuHolderView>() {
    var onAddButtonClick: ((MenuProductItem, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductMenuHolderView {
        val itemBinding =
            ItemMenuProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductMenuHolderView(itemBinding)
    }

    override fun onBindViewHolder(holder: ProductMenuHolderView, position: Int) {
        val itemResponse = list[position]

        holder.bind(itemResponse, itemClickListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ProductMenuHolderView(val itemBinding: ItemMenuProductBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(menuProductItem: MenuProductItem, itemClickListener: ItemClickListener) {
            itemBinding.itemName.text = menuProductItem.name
            itemBinding.priceAndCalories.text =
                String.format(menuProductItem.price + " . " + menuProductItem.baseCalories + " cals")
            itemBinding.description.text = menuProductItem.description

            menuProductItem.image?.let { ViewUtils.loadImage(it, itemBinding.itemImage) }

            itemBinding.usualLabel.visibility =
                if (menuProductItem.favorite) View.VISIBLE else View.GONE

            itemBinding.root.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }
    }
}