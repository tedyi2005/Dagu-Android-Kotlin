package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.authentication.model.Orders
import com.dagu.android.databinding.ItemOrderHistoryBinding
import com.dagu.android.presentation.account.fragment.OrderHistoryFragmentDirections
import com.dagu.android.util.Utils


class OrderHistoryListAdapter(
    private val context: Context,
    private val recentOrderList: List<Orders>
) : RecyclerView.Adapter<OrderHistoryListAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OrderHistoryViewHolder {
        val itemBinding =
            ItemOrderHistoryBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderHistoryViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        val itemResponse = recentOrderList[position]

        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return recentOrderList.size
    }

    class OrderHistoryViewHolder(private val itemBinding: ItemOrderHistoryBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(recentOrder: Orders) {
            itemBinding.apply {
                Utils.setTextOrHide(recentOrder.getOrderProductNames(), itemName)
                Utils.setTextOrHide(recentOrder.deliveryMode, deliveryMode)
                Utils.setTextOrHide(recentOrder.getOrderDate(), itemOrderDate)
                //recentOrder.image?.let { ViewUtils.loadImage(it, tray.trayFoodItem1) }

                itemBinding.root.setOnClickListener {
                    it.findNavController().navigate(
                        OrderHistoryFragmentDirections.actionOrderDetailsFragment(order = recentOrder)
                    )
                }
            }
        }
    }
}