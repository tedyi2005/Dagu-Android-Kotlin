package com.dagu.android.presentation.checkout.editpickuptime

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.databinding.ItemDeliveryTimeBinding

class DeliveryTimeAdapter(private val context: Context, private val list: List<String>) :
    RecyclerView.Adapter<DeliveryTimeAdapter.DeliveryTimeHolderView>() {
    var currentTimeSelectedPosition = 0
    var previousSelectedItemPosition = 0
    var itemOnClick: ((itemPosition:Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryTimeHolderView {
        val itemBinding =
            ItemDeliveryTimeBinding.inflate(LayoutInflater.from(context), parent, false)
        return DeliveryTimeHolderView(itemBinding)
    }

    override fun onBindViewHolder(holder: DeliveryTimeHolderView, position: Int) {
        val itemResponse = list[position]
        //using this logic for ASAP ordering for now
        holder.bind(
            itemResponse,
            position == 0,
            currentTimeSelectedPosition == holder.adapterPosition,
            context
        )
        holder.itemView.setOnClickListener {
            if (currentTimeSelectedPosition != previousSelectedItemPosition) {
                previousSelectedItemPosition = currentTimeSelectedPosition
            }
            currentTimeSelectedPosition = holder.adapterPosition
            itemOnClick?.invoke(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class DeliveryTimeHolderView(private val itemBinding: ItemDeliveryTimeBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(time: String, asapOrdering: Boolean, currentSelected: Boolean, context: Context) {
            itemBinding.deliveryTimeText.text = time
            if (currentSelected) {
                itemView.background =
                    ContextCompat.getDrawable(context, R.drawable.btn_round_corner_green_shape)
            } else {
                itemView.background =
                    ContextCompat.getDrawable(context, R.drawable.btn_round_corner_shape)
            }
            if (asapOrdering)
                itemBinding.asapTextView.visibility = View.VISIBLE
            else
                itemBinding.asapTextView.visibility = View.GONE


        }
    }
}