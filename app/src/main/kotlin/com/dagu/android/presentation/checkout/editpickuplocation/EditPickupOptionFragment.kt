package com.dagu.android.presentation.checkout.editpickuplocation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.dagu.android.R
import com.dagu.android.databinding.FragmentEditPickupOptionBinding
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment

class EditPickupOptionFragment : BaseBottomSheetDialogFragment() {
    private var _binding: FragmentEditPickupOptionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPickupOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBottomSheetBtn.setOnClickListener {
            dismiss()
        }
        val adapter = EditPickupOptionAdapter(this, binding.tabLayout)
        binding.pager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            if (position == 0) {
                tab.text = requireContext().getString(R.string.pick_up)
            } else {
                tab.text = requireContext().getString(R.string.delivery)
            }
        }.attach()
    }

    class EditPickupOptionAdapter(fragment: Fragment, private val tabLayout: TabLayout) :
        FragmentStateAdapter(fragment), EditPickupSearchLocationFragment.TabChangeListener {

        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {
            // Return a NEW fragment instance in createFragment(int)
            return EditPickupSearchLocationFragment()
        }

        override fun onTabChange() {
            // Move first tab
            tabLayout.getTabAt(0)?.select()
        }
    }

}