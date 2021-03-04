package com.dagu.android.presentation.location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.location.Location
import com.dagu.android.databinding.LocationItemBinding
import com.dagu.android.databinding.PickUpTypeRowBinding

class LocationAdapter(private val context: Context, private val list: List<Location>) :
    RecyclerView.Adapter<LocationAdapter.LocationHolderView>() {
    //Display the selected position of the location
    var selectedPosition = -1
    var onItemClick: ((Location) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolderView {
        val itemBinding = LocationItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return LocationHolderView(itemBinding)
    }

    override fun onBindViewHolder(holder: LocationHolderView, position: Int) {
        val itemResponse = list[position]
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(itemResponse)
        }
        setSelectionItem(position, holder.itemView)
        holder.bind(itemResponse, position, context)
    }

    private fun setSelectionItem(position: Int, item: View) {

        if (position == selectedPosition)
            item.background =
                ContextCompat.getDrawable(context, R.drawable.item_selected_option_green)
        else
            item.background =
                ContextCompat.getDrawable(context, R.drawable.item_unselected_option_gray)

    }

    fun findLocationIndex(location: Location): Int {
        return list.indexOf(location)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class LocationHolderView(private val itemBinding: LocationItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(location: Location, position: Int, context: Context) {
            itemBinding.apply {
                count.text = (position + 1).toString()
                title.text = location.name
                address.text = location.streetAddress

                if (location.flags?.isAsapOrderingAvailable == true)
                    nextAvailable.text = context.getString(R.string.asap_ordering)
                else
                    nextAvailable.text = context.getString(R.string.non_asap_ordering)

                if (location.handoffModes?.curbside?.isAvailable == true) {
                    setUpPickUpType(
                        curbsidePickUpType, R.drawable.ic_order_curbside,
                        context.resources.getString(R.string.curbside)
                    )
                }
                if (location.handoffModes?.walkUp?.isAvailable == true) {
                    setUpPickUpType(
                        walkupWindowType, R.drawable.ic_order_walkup,
                        context.resources.getString(R.string.walk_up_window)
                    )
                }
                if (location.handoffModes?.dineIn?.isAvailable == true) {
                    setUpPickUpType(
                        indoorPickUpType, R.drawable.ic_order_indoor,
                        context.resources.getString(R.string.indoor_pick_up)
                    )
                }
            }
        }

        private fun setUpPickUpType(
            pickUpTypeView: PickUpTypeRowBinding, pickUpTypeIcon: Int, pickUpTitle: String
        ) {
            if (itemBinding.pickUpMethodsAvailableTitle.visibility == View.GONE)
                itemBinding.pickUpMethodsAvailableTitle.visibility = View.VISIBLE
            pickUpTypeView.root.visibility = View.VISIBLE
            pickUpTypeView.pickUpTypeIcon.setImageResource(pickUpTypeIcon)
            pickUpTypeView.pickUpTypeText.text = pickUpTitle
        }
    }
}