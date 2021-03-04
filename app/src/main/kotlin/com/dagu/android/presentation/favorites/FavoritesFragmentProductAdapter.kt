package com.dagu.android.presentation.favorites

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.MenuProductItem
import com.dagu.android.databinding.ItemCompactProductWithAddButtonBinding
import com.dagu.android.util.ui.ViewUtils


class FavoritesFragmentProductAdapter(
    private val context: Context,
    private val list: List<MenuProductItem>
) :
    RecyclerView.Adapter<FavoritesFragmentProductAdapter.FavoriteFragmentProductViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteFragmentProductViewHolder {
        val itemBinding =
            ItemCompactProductWithAddButtonBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        return FavoriteFragmentProductViewHolder(
            itemBinding
        )
    }

    override fun onBindViewHolder(holder: FavoriteFragmentProductViewHolder, position: Int) {
        val itemResponse = list[position]

        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class FavoriteFragmentProductViewHolder(
        private val itemBinding: ItemCompactProductWithAddButtonBinding
    ) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(menuProductItem: MenuProductItem) {
            itemBinding.itemName.text = menuProductItem.name
            itemBinding.priceAndCalories.text =
                String.format(menuProductItem.price + " . " + menuProductItem.baseCalories + " cals")
            itemBinding.description.text = menuProductItem.description

            menuProductItem.image?.let { ViewUtils.loadImage(it, itemBinding.itemImage) }
        }
    }
}