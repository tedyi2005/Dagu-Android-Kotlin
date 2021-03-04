package com.dagu.android.util.ui

import android.widget.GridView
import android.widget.ImageView
import com.dagu.android.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ViewUtils {

    companion object {
        fun loadImage(imageUrl: String, imageView: ImageView, squareCorners: Boolean? = false) {
            if (squareCorners == true)
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_error_image)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                        }
                    })
            else
                Picasso.get()
                    .load(imageUrl)
                    .transform(RoundedCornersTransform())
                    .placeholder(R.drawable.ic_error_image)
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                        }

                        override fun onError(e: Exception?) {
                        }
                    })
        }

        fun loadCircularImage(imageUrl: String, imageView: ImageView) {
            Picasso.get()
                .load(imageUrl)
                .transform(CircleTransform())
                .placeholder(R.drawable.ic_error_image)
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                    }

                    override fun onError(e: Exception?) {
                    }
                })
        }

        fun setGridViewHeightBasedOnChildren(gridView: GridView, noOfColumns: Int) {
            val gridViewAdapter = gridView.adapter ?: // adapter is not set yet
            return
            var totalHeight: Int //total height to set on grid view
            val items = gridViewAdapter.count //no. of items in the grid
            val rows: Int //no. of rows in grid
            val listItem = gridViewAdapter.getView(0, null, gridView)
            listItem.measure(0, 0)
            totalHeight = listItem.measuredHeight
            val x: Float
            if (items > noOfColumns) {
                x = items / noOfColumns.toFloat()
                //Check if exact no. of rows of rows are available, if not adding 1 extra row
                rows = if (items % noOfColumns != 0)
                    (x + 1).toInt()
                else
                    x.toInt()

                totalHeight *= rows
                //Adding any vertical space set on grid view
                totalHeight += gridView.verticalSpacing * rows
            }

            //Setting height on grid view
            val params = gridView.layoutParams
            params.height = totalHeight
            gridView.layoutParams = params
        }
    }
}