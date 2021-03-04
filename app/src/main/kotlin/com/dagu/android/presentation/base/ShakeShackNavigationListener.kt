package com.dagu.android.presentation.base

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.dagu.android.data.authentication.model.Orders

/**
 * Interface for communication between Fragments and MainActivity's navigation/UI methods
 */
interface ShakeShackNavigationListener {
    /**
     * Sets the app toolbar from the fragment
     *
     * @param toolbar   The toolbar view to be set up from the Fragment
     */
    fun setUpToolbar(toolbar: Toolbar)
    fun toggleDrawerLayout(show: Boolean)
    fun toggleBottomSheet(show: Boolean)
    fun onBackPressed()
    fun expandCategoryMenu()

    /**
     *  Method for checking the current `BottomSheetBehavior.State` of the BottomSheet
     */
    fun getBottomSheetState(): Int

    /**
     * Method for showing the tray detail with animation
     *
     * @param order   If provided, this Order will be shown in the tray detail so it ce reordered.
     * @param startView   The view from which the animation will be triggered and will be transformed
     * into the tray detail. If not provided, the tray detail will appear from the bottom with a
     * regular animation.
     */
    fun showTrayDetail(order: Orders?, startView: View?)
}