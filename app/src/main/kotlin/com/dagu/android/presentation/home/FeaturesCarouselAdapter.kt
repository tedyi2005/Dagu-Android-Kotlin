package com.dagu.android.presentation.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.dagu.android.R
import com.dagu.android.databinding.ItemHomeBinding


class FeaturesCarouselAdapter(private val context: Context) :
    RecyclerView.Adapter<FeaturesCarouselAdapter.FeatureItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureItemViewHolder {
        val itemHomeBinding = ItemHomeBinding.inflate(LayoutInflater.from(context), parent, false)
        return FeatureItemViewHolder(itemHomeBinding, context)
    }

    override fun onBindViewHolder(holder: FeatureItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        // for infinite scrolling
        return Int.MAX_VALUE
    }

    class FeatureItemViewHolder(
        private val itemHomeBinding: ItemHomeBinding,
        private val context: Context
    ) :
        RecyclerView.ViewHolder(itemHomeBinding.root) {
        fun bind(position: Int) {
            // For demo purposes we alternate between dark and light items:
            val pageHasDarkOverlay = position % 2 == 0
            itemHomeBinding.apply {
                if (pageHasDarkOverlay) {
                    val padding20 = context.resources.getDimensionPixelSize(
                        R.dimen.size_20dp
                    )

                    featureContainerLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.black_80_percent
                        )
                    )
                    featureContainerLayout.setPadding(padding20, padding20, padding20, padding20)

                    TextViewCompat.setTextAppearance(
                        featureTextView,
                        R.style.ShakeShackTextView_HeroMobile_White
                    )
                    TextViewCompat.setTextAppearance(
                        featureSubTextView,
                        R.style.ShakeShackTextView_BodyNormal_White
                    )

                    val whiteButton: MaterialButton = LayoutInflater.from(context).inflate(
                        R.layout.button_home_hero_white,
                        null
                    ) as MaterialButton
                    whiteButton.setOnClickListener {
                        Toast.makeText(context, "White button clicked", Toast.LENGTH_SHORT).show()
                    }

                    featureButtonContainer.removeAllViews()
                    featureButtonContainer.addView(whiteButton)
                } else {
                    featureContainerLayout.setBackgroundColor(
                        ContextCompat.getColor(
                            context,
                            R.color.transparent
                        )
                    )
                    featureContainerLayout.setPadding(0, 0, 0, 0)

                    TextViewCompat.setTextAppearance(
                        featureTextView,
                        R.style.ShakeShackTextView_HeroMobile_Black
                    )
                    TextViewCompat.setTextAppearance(
                        featureSubTextView,
                        R.style.ShakeShackTextView_BodyNormal
                    )

                    val blackButton: MaterialButton = LayoutInflater.from(context).inflate(
                        R.layout.button_home_hero_black,
                        null
                    ) as MaterialButton
                    blackButton.setOnClickListener {
                        Toast.makeText(context, "Black button clicked", Toast.LENGTH_SHORT).show()
                    }

                    featureButtonContainer.removeAllViews()
                    featureButtonContainer.addView(blackButton)
                }

                // Need this method only to navigate
                itemHomeBinding.root.setOnClickListener {
                }
            }
        }
    }
}