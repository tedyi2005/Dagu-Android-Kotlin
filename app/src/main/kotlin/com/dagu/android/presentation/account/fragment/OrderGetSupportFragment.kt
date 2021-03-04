package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.dagu.android.R
import com.dagu.android.databinding.FragmentOrderGetSupportBinding
import com.dagu.android.presentation.base.BaseFragment

class OrderGetSupportFragment : BaseFragment() {
    private var _binding: FragmentOrderGetSupportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderGetSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            closeBottomSheet.setOnClickListener {
                navigationListener?.onBackPressed()
            }
            moreSupportActionButton.setOnClickListener {
                findNavController().navigate(R.id.more_support_options_fragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}