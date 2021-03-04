package com.dagu.android.presentation.checkout.traydetail

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.data.menu.Product
import com.dagu.android.databinding.ItemTrayDetailBinding
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils


class TrayDetailAdapter(
    private val productList: MutableList<Product>,
    private val context: Context
) : RecyclerView.Adapter<TrayDetailAdapter.TrayDetailHolderView>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TrayDetailHolderView {
        val itemBinding =
            ItemTrayDetailBinding.inflate(LayoutInflater.from(context), parent, false)
        return TrayDetailHolderView(itemBinding)
    }

    override fun onBindViewHolder(holder: TrayDetailHolderView, position: Int) {
        val itemResponse = productList[position]
        holder.bind(itemResponse)
        setFadeAnimation(holder.itemView)
    }

    fun onProductAdded(product: Product) {
        productList.add(product)
        notifyItemChanged(productList.size - 1)
    }

    private fun setFadeAnimation(view: View) {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 500
        view.startAnimation(anim)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class TrayDetailHolderView(private val itemBinding: ItemTrayDetailBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            product: Product,
        ) {
            itemBinding.apply {
                Utils.setTextOrHide(product.name, trayItemTitle)
                Utils.setTextOrHide(product.description, trayItemDescription)
                Utils.setTextOrHide(product.basePrice.toString(), trayItemPrice)

                if (product.images?.imageLg?.isNotBlank() == true)
                    ViewUtils.loadImage(product.images.imageLg, trayItemImage)
            }
        }
    }
}