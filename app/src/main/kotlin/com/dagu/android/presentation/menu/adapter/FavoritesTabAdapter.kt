package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.menu.Product
import com.dagu.android.databinding.FavoriteTabItemBinding
import com.dagu.android.presentation.menu.MenuCategoryFragmentDirections
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils


class FavoritesTabAdapter(
    private val context: Context,
    private val list: List<Product>
) :
    RecyclerView.Adapter<FavoritesTabAdapter.ProductMenuHolderView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductMenuHolderView {
        val itemBinding =
            FavoriteTabItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductMenuHolderView(context, itemBinding)
    }

    override fun onBindViewHolder(holder: ProductMenuHolderView, position: Int) {
        val itemResponse = list[position]

        holder.bind(itemResponse, position, list.size)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ProductMenuHolderView(
        private val context: Context,
        private val itemBinding: FavoriteTabItemBinding
    ) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            product: Product,
            position: Int,
            size: Int
        ) {
            Utils.setTextOrHide(product.name, itemBinding.itemName)
            itemBinding.priceAndCalories.text =
                String.format(product.getDisplayCost() + product.getDisplayCalories())
            Utils.setTextOrHide(product.description, itemBinding.description)
            if (product.images?.imageLg?.isNotEmpty() == true)
                ViewUtils.loadImage(product.images.imageLg, itemBinding.itemImage)

            Utils.setTextOrHide(product.productTag, itemBinding.usualLabel)
            if (!product.productTag.isNullOrEmpty()) {
                itemBinding.usualLabel.rotation = product.advanceProductTagRotation()
                Utils.setRandomWhiteAndGreenColorScheme(context, itemBinding.usualLabel)
            }

            itemBinding.usualLabel.visibility =
                if (product.favorite) View.VISIBLE else View.GONE

            // Big padding, this is used at the start of the first item, and at the end of the last
            // one:
            val bigPadding = context.resources.getDimensionPixelSize(
                R.dimen.size_22dp
            )

            // Small padding, this is used between items:
            val smallPadding = context.resources.getDimensionPixelSize(
                R.dimen.size_5dp
            )

            // Calculate a new width for the item, based on the screen width minus a padding.
            // This way we make sure that we always show the same amount of dp of the next item.
            val itemWidth = Utils.getDevicePixelWidth(context) - bigPadding
            val layoutParams =
                ConstraintLayout.LayoutParams(itemWidth, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            itemBinding.wholeLayoutMenuProductItem.layoutParams = layoutParams

            // Apply a big or small padding to the item, depending on its position on the list,
            // or whether the list has only one item...
            when {
                size == 1 -> itemBinding.wholeLayoutMenuProductItem.setPadding(
                    bigPadding,
                    0,
                    bigPadding,
                    0
                )
                position == 0 -> itemBinding.wholeLayoutMenuProductItem.setPadding(
                    bigPadding,
                    0,
                    smallPadding,
                    0
                )
                position == size - 1 -> itemBinding.wholeLayoutMenuProductItem.setPadding(
                    smallPadding,
                    0,
                    bigPadding,
                    0
                )
                else -> itemBinding.wholeLayoutMenuProductItem.setPadding(
                    smallPadding,
                    0,
                    smallPadding,
                    0
                )
            }

            itemBinding.root.setOnClickListener {
                itemBinding.itemImage.transitionName = product.images?.imageLg!!

                val productDetailImageTransitionName = product.images.imageLg
                val extras = FragmentNavigatorExtras(itemBinding.itemImage to productDetailImageTransitionName)
                val directions = MenuCategoryFragmentDirections.actionMenuCategoryFragmentToProductDetailFragment(product = product)
                it.findNavController().navigate(directions, extras)
            }
        }
    }
}