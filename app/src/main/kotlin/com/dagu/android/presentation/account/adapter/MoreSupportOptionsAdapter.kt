package com.dagu.android.presentation.account.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.databinding.SupportOptionRowBinding
import com.dagu.android.util.Utils


class MoreSupportOptionsAdapter(
    private val context: Context,
    private val supportOptionList: List<String>
) :
    RecyclerView.Adapter<MoreSupportOptionsAdapter.MoreSupportOptionsViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MoreSupportOptionsViewHolder {
        val itemBinding =
            SupportOptionRowBinding.inflate(LayoutInflater.from(context), parent, false)
        return MoreSupportOptionsViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MoreSupportOptionsViewHolder, position: Int) {
        val itemResponse = supportOptionList[position]
        holder.bind(itemResponse)
    }

    override fun getItemCount(): Int {
        return supportOptionList.size
    }

    class MoreSupportOptionsViewHolder(private val itemBinding: SupportOptionRowBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(option: String) {
            itemBinding.apply {
                Utils.setTextOrHide(option, supportOption)
                root.setOnClickListener {
                    it.findNavController().navigate(R.id.support_notes_screen_fragment_action)
                }
            }
        }
    }
}