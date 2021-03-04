package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.data.authentication.model.Orders
import com.dagu.android.data.repository.Result
import com.dagu.android.databinding.FragmentOrderHistoryBinding
import com.dagu.android.presentation.account.adapter.OrderHistoryListAdapter
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class OrderHistoryFragment : BaseFragment() {
    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
        }

        viewModel.recentOrders.observe(viewLifecycleOwner, { result ->
            if (result is Result.Success) {
                val orders = result.data.orders
                if (orders.isNotEmpty())
                    initRecyclerView(orders)
            }
        });
    }

    private fun initRecyclerView(orders: List<Orders>) {
        binding.orderHistoryRecyclerView.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.itemAnimator = DefaultItemAnimator()
            this.setHasFixedSize(true)
            val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            itemDecoration.setDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.devider
                )!!
            )
            this.addItemDecoration(itemDecoration)
            this.adapter = OrderHistoryListAdapter(requireContext(), orders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}