package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.databinding.FragmentMoreSupportOptionsBinding
import com.dagu.android.presentation.account.adapter.MoreSupportOptionsAdapter
import com.dagu.android.presentation.base.BaseFragment

class MoreSupportOptionsFragment : BaseFragment() {
    private var _binding: FragmentMoreSupportOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreSupportOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            closeBottomSheet.setOnClickListener {
                navigationListener?.onBackPressed()
            }

            // static data
            val supportOptions =
                arrayListOf("Accuracy", "Food quality", "Other")
            issueTypeRecyclerView.apply {
                this.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                this.itemAnimator = DefaultItemAnimator()
                this.setHasFixedSize(true)
                val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                itemDecoration.setDrawable(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.devider)!!
                )
                this.addItemDecoration(itemDecoration)
                this.adapter = MoreSupportOptionsAdapter(requireContext(), supportOptions)
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}