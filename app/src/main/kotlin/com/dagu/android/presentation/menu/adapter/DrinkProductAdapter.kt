package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.Product
import com.dagu.android.databinding.ItemCompactProductWithAddButtonBinding
import com.dagu.android.presentation.OneTapTrayListener
import com.dagu.android.presentation.menu.MenuCategoryFragmentDirections
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils

class DrinkProductAdapter(
    private val productList: List<Product>,
    private val oneTapTrayListener: OneTapTrayListener,
    private val context: Context
) :
    RecyclerView.Adapter<DrinkProductAdapter.DrinkProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrinkProductViewHolder {
        val itemBinding =
            ItemCompactProductWithAddButtonBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        return DrinkProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DrinkProductViewHolder, position: Int) {
        holder.bind(productList[position], oneTapTrayListener)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class DrinkProductViewHolder(private val itemBinding: ItemCompactProductWithAddButtonBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            product: Product, oneTapTrayListener: OneTapTrayListener
        ) {
            itemBinding.apply {
                Utils.setTextOrHide(product.name, itemBinding.itemName)
                priceAndCalories.text =
                    String.format(product.getDisplayCost() + product.getDisplayCalories())
                description.visibility = View.GONE

                if (product.allergens != null) {
                    allergen.visibility = View.VISIBLE
                    allergen.text =
                        String.format(Constants.CONTAINS + product.allergens.replace("allergy", ""))
                }

                if (product.images?.imageLg?.isNotEmpty()!!)
                    ViewUtils.loadImage(product.images.imageLg, itemImage)

                root.setOnClickListener {
                    itemImage.transitionName = product.images.imageLg

                    val productDetailImageTransitionName = product.images.imageLg
                    val extras = FragmentNavigatorExtras(itemImage to productDetailImageTransitionName)
                    val directions = MenuCategoryFragmentDirections.actionMenuCategoryFragmentToProductDetailFragment(product = product)
                    it.findNavController().navigate(directions, extras)
                }
                addItemButton.setOnClickListener {
                    oneTapTrayListener.onProductAdded(product, itemImage, isDrink = true)
                }
            }
        }
    }
}
