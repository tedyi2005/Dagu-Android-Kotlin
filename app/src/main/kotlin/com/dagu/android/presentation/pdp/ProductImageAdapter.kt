package com.dagu.android.presentation.pdp

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.databinding.ProductImageViewBinding
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils

class ProductImageAdapter(
    private val context: Context,
    private val imageList: List<ProductImageGallery>
) :
    RecyclerView.Adapter<ProductImageAdapter.ProductImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductImageViewHolder {
        val productImageViewBinding =
            ProductImageViewBinding.inflate(LayoutInflater.from(context), parent, false)
        return ProductImageViewHolder(productImageViewBinding)
    }

    override fun onBindViewHolder(holder: ProductImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        holder.bind(context, imageUrl)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class ProductImageViewHolder(private val itemBinding: ProductImageViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(context: Context, productImageGallery: ProductImageGallery) {
            itemBinding.apply {
                if (!productImageGallery.image.isNullOrEmpty())
                    ViewUtils.loadImage(
                        productImageGallery.image,
                        productImageView,
                        squareCorners = true
                    )
                if (!productImageGallery.tag.isNullOrEmpty()) {
                    Utils.setTextOrHide(productImageGallery.tag, productTag)
                    productTag.rotation = productImageGallery.rotation!!
                    Utils.setRandomWhiteAndGreenColorScheme(context, productTag)
                }
            }
        }
    }
}

data class ProductImageGallery(
    val image: String? = null,
    val tag: String? = null,
    var rotation: Float? = null
)