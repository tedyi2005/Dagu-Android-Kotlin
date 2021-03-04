package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.ItemAllergensScreenBinding


class UserAllergensAdapter(
    private val userSelectedAllergens: List<SingleViewItem>,
    private val allAllergens: List<SingleViewItem>,
    private val context: Context
) :
    RecyclerView.Adapter<UserAllergensAdapter.AllergenViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllergenViewHolder {
        val itemBinding =
            ItemAllergensScreenBinding.inflate(LayoutInflater.from(context), parent, false)
        return AllergenViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AllergenViewHolder, position: Int) {
        val currentAllergen = allAllergens[position]
        val isSelected = userSelectedAllergens.contains(currentAllergen)
        holder.bind(currentAllergen, isSelected)
    }

    override fun getItemCount(): Int {
        return allAllergens.size
    }

    class AllergenViewHolder(private val itemBinding: ItemAllergensScreenBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(allergen: SingleViewItem, isSelected: Boolean) {
            itemBinding.allergenName.text = allergen.itemName
            itemBinding.allergenCheckBox.isChecked = isSelected
        }
    }
}