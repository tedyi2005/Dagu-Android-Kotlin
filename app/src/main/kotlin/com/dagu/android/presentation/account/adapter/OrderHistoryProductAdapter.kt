package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.authentication.model.OrderProducts
import com.dagu.android.databinding.OrderProductItemBinding
import com.dagu.android.util.Utils
import com.dagu.android.util.appendDollarSign

class OrderHistoryProductAdapter(
    private val context: Context,
    private val orderProductList: List<OrderProducts>
) :
    RecyclerView.Adapter<OrderHistoryProductAdapter.OrderHistoryProductViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): OrderHistoryProductViewHolder {
        val itemBinding =
            OrderProductItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return OrderHistoryProductViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: OrderHistoryProductViewHolder, position: Int) {
        val itemResponse = orderProductList[position]

        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return orderProductList.size
    }

    class OrderHistoryProductViewHolder(private val itemBinding: OrderProductItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(orderProducts: OrderProducts) {
            itemBinding.apply {
                Utils.setTextOrHide(orderProducts.name, orderProductTitle)
                Utils.setTextOrHide(orderProducts.quantity.toString(), orderProductQuantity)
                Utils.setTextOrHide(orderProducts.getExtras(), orderProductDescription)
                Utils.setTextOrHide(
                    orderProducts.totalCost.toString().appendDollarSign(), orderProductPrice
                )
            }
        }
    }
}