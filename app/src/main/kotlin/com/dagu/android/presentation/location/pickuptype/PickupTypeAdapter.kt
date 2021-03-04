package com.dagu.android.presentation.location.pickuptype

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.databinding.ItemPickupTypeBinding

class PickupTypeAdapter(val context: Context) :
    RecyclerView.Adapter<PickupTypeAdapter.PickupTypeViewHolder>() {
    var itemOnClick: ((itemPosition: Int) -> Unit)? = null
    var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickupTypeViewHolder {
        val itemBinding = ItemPickupTypeBinding.inflate(LayoutInflater.from(context), parent, false)
        return PickupTypeViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: PickupTypeViewHolder, position: Int) {
        holder.bind(position)
        setSelectionItem(position, holder.itemView)
        holder.itemView.setOnClickListener {
            selectedPosition = position
            itemOnClick?.invoke(position)

        }
    }

    private fun setSelectionItem(position: Int, item: View) {
        if (position == selectedPosition)
            item.background =
                ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
        else
            item.background =
                ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)

    }

    override fun getItemCount(): Int {
        return 3
    }

    class PickupTypeViewHolder(private val itemViewBinding: ItemPickupTypeBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
        fun bind(position: Int) {
            val context = itemViewBinding.root.context
            when (position) {
                0 -> {
                    itemViewBinding.pickupTypeItemTitle.text = context.getString(
                        R.string.curbside
                    )
                    itemViewBinding.pickupTypeItemSubtitle.text = context.getString(
                        R.string.curbisde_detail
                    )
                    itemViewBinding.pickupTypeImageView.setImageResource(R.drawable.ic_order_curbside)
                }
                1 -> {
                    itemViewBinding.pickupTypeItemTitle.text = context.getString(
                        R.string.walk_up_window
                    )
                    itemViewBinding.pickupTypeItemSubtitle.text = context.getString(
                        R.string.walk_up_window_detail
                    )
                    itemViewBinding.pickupTypeImageView.setImageResource(R.drawable.ic_order_walkup)
                }

                else -> {
                    itemViewBinding.pickupTypeItemTitle.text = context.getString(
                        R.string.indoor_pick_up
                    )
                    itemViewBinding.pickupTypeItemSubtitle.text = context.getString(
                        R.string.indoor_pick_up_detail
                    )
                    itemViewBinding.shackTrackTextview.visibility = View.GONE
                    itemViewBinding.pickupTypeImageView.setImageResource(R.drawable.ic_order_indoor)
                }
            }
        }
    }
}