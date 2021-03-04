package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.data.user.UserAddress
import com.dagu.android.databinding.ItemUserAddressesScreenBinding


class UserAddressesAdapter(
    private val context: Context,
    private val list: List<UserAddress>
) :
    RecyclerView.Adapter<UserAddressesAdapter.UserAddressViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserAddressViewHolder {
        val itemBinding =
            ItemUserAddressesScreenBinding.inflate(LayoutInflater.from(context), parent, false)
        return UserAddressViewHolder(
            itemBinding, context
        )
    }

    override fun onBindViewHolder(holder: UserAddressViewHolder, position: Int) {
        val itemResponse = list[position]
        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class UserAddressViewHolder(
        private val itemBinding: ItemUserAddressesScreenBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(userAddress: UserAddress) {
            itemBinding.addressNameTextView.text = userAddress.name
            itemBinding.addressFirstLineTextView.text = userAddress.firstLine

            if (userAddress.secondLine.isNullOrBlank()) {
                itemBinding.addressSecondLineTextView.visibility = View.GONE
            } else {
                itemBinding.addressFirstLineTextView.text = userAddress.firstLine

                itemBinding.addressSecondLineTextView.text =
                    appendInstructionsToSecondLineIfPossible(
                        userAddress.secondLine,
                        userAddress.instructions
                    )
                itemBinding.addressSecondLineTextView.visibility = View.VISIBLE
            }
        }

        private fun appendInstructionsToSecondLineIfPossible(
            secondLine: String,
            instructions: String?
        ): String {
            return if (instructions.isNullOrBlank())
                secondLine
            else {
                context.getString(
                    R.string.address_second_line_with_instructions,
                    secondLine,
                    instructions
                )
            }
        }
    }
}