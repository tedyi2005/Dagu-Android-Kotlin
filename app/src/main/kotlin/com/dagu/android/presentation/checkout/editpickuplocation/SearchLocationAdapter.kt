package com.dagu.android.presentation.checkout.editpickuplocation

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dagu.android.R
import com.dagu.android.databinding.ItemSearchLocationBinding
import com.dagu.android.presentation.location.DisplayRegionOrLocation


class SearchLocationAdapter(val context: Context) :
    RecyclerView.Adapter<SearchLocationAdapter.SearchLocationHolderView>() {


    var onItemClick: ((DisplayRegionOrLocation) -> Unit)? = null


    private var regionOrLocationList: MutableList<DisplayRegionOrLocation> =
        emptyList<DisplayRegionOrLocation>().toMutableList()
    private var searchTermsToHighlight = ""


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchLocationHolderView {
        val itemBinding =
            ItemSearchLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchLocationHolderView(itemBinding = itemBinding)
    }

    override fun onBindViewHolder(holder: SearchLocationHolderView, position: Int) {
        val regionOrLocation = regionOrLocationList[position]
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(regionOrLocation)
        }
        holder.bind(
            context,
            locationTitle = regionOrLocation.name,
            locationSubTitle = regionOrLocation.shackCount.toDisplayShackCount()
                ?: regionOrLocation.locationAddress
                ?: "",
            searchTerms = searchTermsToHighlight
        )
    }

    override fun getItemCount(): Int {
        return regionOrLocationList.size
    }

    fun updateDataSet(newItems: List<DisplayRegionOrLocation>, newSearchTerms: String) {
        regionOrLocationList.clear()
        searchTermsToHighlight = newSearchTerms
        regionOrLocationList = newItems.toMutableList()
        notifyDataSetChanged()
    }

    class SearchLocationHolderView(private val itemBinding: ItemSearchLocationBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            context: Context,
            locationTitle: String,
            locationSubTitle: String,
            searchTerms: String
        ) {
            itemBinding.searchLocationSubTitle.text = locationSubTitle
            if (searchTerms.isNotBlank()) {
                itemBinding.searchLocationTitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.grey_500
                    )
                )
                val highlightedSpannableString =
                    highlightSearchTermsInTitle(locationTitle, searchTerms)
                itemBinding.searchLocationTitle.setText(
                    highlightedSpannableString,
                    TextView.BufferType.SPANNABLE
                )
            } else {
                itemBinding.searchLocationTitle.setTextColor(Color.BLACK)
                itemBinding.searchLocationTitle.text = locationTitle
            }

        }

        private fun highlightSearchTermsInTitle(
            locationTitle: String,
            searchTerms: String
        ): SpannableString {
            val spannable = SpannableString(locationTitle)
            val firstOccurrence = indexesOf(locationTitle, searchTerms).firstOrNull()
            firstOccurrence?.let {
                spannable.setSpan(
                    ForegroundColorSpan(Color.BLACK),
                    firstOccurrence.first,
                    firstOccurrence.last + 1,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
            return spannable
        }

        private fun ignoreCaseOpt(ignoreCase: Boolean) =
            if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()

        private fun indexesOf(
            start: String,
            pat: String,
            ignoreCase: Boolean = true
        ): List<IntRange> =
            pat.toRegex(ignoreCaseOpt(ignoreCase))
                .findAll(start)
                .map { it.range }
                .toList()
    }
}

private fun Int?.toDisplayShackCount(): String? {
    return when (this) {
        null -> null
        1 -> "$this Shack"
        else -> "$this Shacks"
    }
}

