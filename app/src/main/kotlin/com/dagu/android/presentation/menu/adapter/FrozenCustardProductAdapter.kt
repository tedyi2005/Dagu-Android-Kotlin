package com.dagu.android.presentation.menu.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.Option
import com.dagu.android.databinding.ItemCompactProductWithAddButtonBinding
import com.dagu.android.presentation.OneTapTrayListener
import com.dagu.android.presentation.menu.MenuCategoryFragmentDirections
import com.dagu.android.util.Utils

class FrozenCustardProductAdapter(
    private val optionList: List<Option>,
    private val oneTapTrayListener: OneTapTrayListener,
    private val context: Context
) :
    RecyclerView.Adapter<FrozenCustardProductAdapter.FrozenCustardProductViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FrozenCustardProductViewHolder {
        val itemBinding =
            ItemCompactProductWithAddButtonBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        return FrozenCustardProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: FrozenCustardProductViewHolder, position: Int) {
        holder.bind(optionList[position], oneTapTrayListener)
    }

    override fun getItemCount(): Int {
        return optionList.size
    }

    class FrozenCustardProductViewHolder(

        private val itemBinding: ItemCompactProductWithAddButtonBinding
    ) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(
            option: Option, oneTapTrayListener: OneTapTrayListener
        ) {
            itemBinding.apply {
                Utils.setTextOrHide(option.name, itemBinding.itemName)
                priceAndCalories.text =
                    String.format("$" + option.cost.toString() + "  .  " + option.baseCalories + " cals")

                allergen.visibility = View.GONE
                description.visibility = View.GONE

                root.setOnClickListener {
                    itemImage.transitionName = option.toProduct().images?.imageLg!!

                    val productDetailImageTransitionName = option.toProduct().images?.imageLg!!
                    val extras = FragmentNavigatorExtras(itemImage to productDetailImageTransitionName)
                    val directions = MenuCategoryFragmentDirections.actionMenuCategoryFragmentToProductDetailFragment(product = option.toProduct())
                    it.findNavController().navigate(directions, extras)
                }
                addItemButton.setOnClickListener {
                    oneTapTrayListener.onOptionAdded(option)
                }
            }
        }
    }
}