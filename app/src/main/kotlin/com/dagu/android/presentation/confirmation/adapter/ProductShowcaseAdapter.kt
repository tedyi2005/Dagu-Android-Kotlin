package com.dagu.android.presentation.confirmation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.databinding.ProductShowcaseItemBinding
import com.dagu.android.presentation.confirmation.ProductShowcase
import com.dagu.android.util.Utils

class ProductShowcaseAdapter(
    private val context: Context,
    private val productShowcaseList: List<ProductShowcase>
) :
    RecyclerView.Adapter<ProductShowcaseAdapter.ProcessingConfirmationViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ProcessingConfirmationViewHolder {
        val itemHomeBinding =
            ProductShowcaseItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProcessingConfirmationViewHolder(itemHomeBinding)
    }

    override fun onBindViewHolder(holder: ProcessingConfirmationViewHolder, position: Int) {
        // Circulate showcase list items to show/display view
        val item = productShowcaseList[position % productShowcaseList.size]
        holder.bind(item, context)
    }

    override fun getItemCount(): Int {
        // for infinite scrolling
        return Int.MAX_VALUE
    }

    class ProcessingConfirmationViewHolder(private val itemBinding: ProductShowcaseItemBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(productShowcase: ProductShowcase, context: Context) {
            itemBinding.apply {
                feature.text = productShowcase.feature
                curiosity.text = productShowcase.curiosity
                productShowcase.image?.let {
                    itemImage.setImageDrawable(Utils.getDrawableFromFileName(it, context))
                }
            }
        }
    }
}