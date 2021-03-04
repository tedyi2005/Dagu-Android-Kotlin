package com.dagu.android.view.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.view.widgets.MultiSelectorView

class MultiSelectionAdapter(private val context: Context, private val list: List<SingleViewItem>) :
    BaseAdapter() {
    private var selectedPositions: MutableList<Int> = mutableListOf()
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val multiSelectorView: MultiSelectorView? = if (convertView == null) {
            MultiSelectorView(context)
        } else {
            convertView as MultiSelectorView?
        }
        multiSelectorView?.display(list[position], selectedPositions.contains(position))

        multiSelectorView?.setOnClickListener {
            if (!selectedPositions.contains(position))
                selectedPositions.add(position)
            else
                selectedPositions.remove(position)
            multiSelectorView.display(list[position], selectedPositions.contains(position))
        }

        return multiSelectorView as MultiSelectorView
    }

}