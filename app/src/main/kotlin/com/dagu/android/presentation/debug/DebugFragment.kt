package com.dagu.android.presentation.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.dagu.android.R
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment

class DebugFragment : BaseBottomSheetDialogFragment() {
    private lateinit var debugPageAdapter: DebugPageAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_debug, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        debugPageAdapter = DebugPageAdapter(this)
        viewPager = view.findViewById(R.id.debug_view_pager)
        viewPager.adapter = debugPageAdapter

        val tabNames = listOf("Shortcuts", "Network", "Environment")
        val tabLayout: TabLayout = view.findViewById(R.id.debug_tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabNames[position]
        }.attach()
    }
}

class DebugPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DebugShortcutsFragment()
            1 -> DebugNetworkFragment()
            2 -> DebugEnvironmentFragment()
            else -> DebugShortcutsFragment()
        }
    }
}
