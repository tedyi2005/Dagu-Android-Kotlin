package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.payment.PaymentMethod
import com.dagu.android.databinding.ItemPaymentMethodsScreenBinding


class PaymentMethodsAdapter(
    private val context: Context,
    private val list: List<PaymentMethod>
) :
    RecyclerView.Adapter<PaymentMethodsAdapter.PaymentMethodViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PaymentMethodViewHolder {
        val itemBinding =
            ItemPaymentMethodsScreenBinding.inflate(LayoutInflater.from(context), parent, false)
        return PaymentMethodViewHolder(
            itemBinding, context
        )
    }

    override fun onBindViewHolder(holder: PaymentMethodViewHolder, position: Int) {
        val itemResponse = list[position]
        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class PaymentMethodViewHolder(
        private val itemBinding: ItemPaymentMethodsScreenBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(paymentMethod: PaymentMethod) {
            itemBinding.paymentMethodNameTextView.text = paymentMethod.name

            var separator = ""
            var status: Int? = null

            if (paymentMethod.expired || paymentMethod.default) {
                separator = " · "

                if (paymentMethod.expired) {
                    status = R.string.expired
                    itemBinding.paymentMethodStatusTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.design_default_color_error
                        )
                    )
                } else {
                    status = R.string.default_payment_method
                    itemBinding.paymentMethodStatusTextView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.primary_green
                        )
                    )
                }
            }

            itemBinding.paymentMethodDescriptionTextView.text = context.getString(
                R.string.payment_method_last_four_with_separator,
                paymentMethod.lastFour,
                separator
            )
            status?.let { itemBinding.paymentMethodStatusTextView.text = context.getString(status) }
        }
    }
}