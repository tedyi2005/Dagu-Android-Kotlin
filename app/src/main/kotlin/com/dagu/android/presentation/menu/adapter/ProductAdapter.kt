package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.Product
import com.dagu.android.databinding.ItemMenuProductBinding
import com.dagu.android.presentation.OneTapTrayListener
import com.dagu.android.presentation.menu.MenuCategoryFragmentDirections
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils

class ProductAdapter(
    private val productList: List<Product>,
    private val oneTapTrayListener: OneTapTrayListener,
    private val context: Context
) : RecyclerView.Adapter<ProductAdapter.ProductMenuHolderView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductMenuHolderView {
        val itemBinding =
            ItemMenuProductBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductMenuHolderView(context, itemBinding)
    }

    override fun onBindViewHolder(holder: ProductMenuHolderView, position: Int) {
        val itemResponse = productList[position]
        holder.bind(itemResponse, oneTapTrayListener)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class ProductMenuHolderView(
        private val context: Context,
        private val itemBinding: ItemMenuProductBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            product: Product,
            oneTapTrayListener: OneTapTrayListener
        ) {
            itemBinding.apply {
                Utils.setTextOrHide(product.name, itemName)

                priceAndCalories.text =
                    String.format(product.getDisplayCost() + product.getDisplayCalories() + product.getDisplayAttributes())

                Utils.setTextOrHide(product.description, description)
                Utils.setTextOrHide(product.productTag, usualLabel)
                if (!product.productTag.isNullOrEmpty()) {
                    usualLabel.rotation = product.advanceProductTagRotation()
                    Utils.setRandomWhiteAndGreenColorScheme(context, usualLabel)
                }

                if (product.allergens != null) {
                    allergen.visibility = View.VISIBLE
                    allergen.text =
                        String.format(
                            Constants.CONTAINS + product.allergens.replace("allergy", "")
                        )
                }

                if (product.images?.imageLg?.isNotBlank() == true)
                    ViewUtils.loadImage(product.images.imageLg, itemImage)

                root.setOnClickListener {
                    itemImage.transitionName = product.images?.imageLg!!

                    val productDetailImageTransitionName = product.images.imageLg
                    val extras = FragmentNavigatorExtras(itemImage to productDetailImageTransitionName)
                    val directions = MenuCategoryFragmentDirections.actionMenuCategoryFragmentToProductDetailFragment(product = product)
                    it.findNavController().navigate(directions, extras)
                }

                addItemButton.setOnClickListener {
                    oneTapTrayListener.onProductAdded(product, itemImage, isDrink = false)
                }
            }
        }
    }
}
