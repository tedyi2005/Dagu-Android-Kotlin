package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.data.authentication.model.Orders
import com.dagu.android.databinding.FragmentOrderDetailsBinding
import com.dagu.android.presentation.account.adapter.OrderHistoryProductAdapter
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.Utils
import com.dagu.android.util.appendDollarSign

class OrderDetailsFragment : BaseFragment() {
    private var _binding: FragmentOrderDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var order: Orders

    private val args: OrderDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailsBinding.inflate(inflater, container, false)
        order = args.order
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        binding.apply {
            setUpToolbarFromFragment(toolbar)
            orderPlacedDate.text = Utils.formatOrderDate(order.timePlaced)
            orderNumber.text = String.format("#%s", order.oloid)
            deliveryOrPickupDescription.text =
                String.format("%s at %s", order.deliveryMode, order.vendorName)
            subtotalText.text = order.subtotal.toString().appendDollarSign()
            taxesText.text = order.getAggregatedTaxes().toString().appendDollarSign()
            discountsText.text = order.discount.toString().appendDollarSign()
            tipText.text = order.tip.toString().appendDollarSign()
            totalText.text = order.total.toString().appendDollarSign()

            // Hide line separator if no order products
            if (order.products.isEmpty())
                productSectionSeparator.visibility = View.GONE

            orderedProductRecyclerView.apply {
                this.adapter = OrderHistoryProductAdapter(requireContext(), order.products)
                this.layoutManager = LinearLayoutManager(requireContext())
            }

            backOrderDetailsFab.setOnClickListener {
                navigationListener?.onBackPressed()
            }

            reorderFab.setOnClickListener {
                val orderBundle = bundleOf(
                    "order" to order
                )

                findNavController().navigate(R.id.reorder_fragment_action, orderBundle)
            }

            scrollView.viewTreeObserver.addOnScrollChangedListener {
                val scrollViewHeight = scrollView.getChildAt(0).bottom - scrollView.height
                val getScrollY = scrollView.scrollY
                val scrollPosition = getScrollY.toDouble() / scrollViewHeight.toDouble() * 100.0
                // If scroll position is over 25 show floating back button
                if (scrollPosition >= 25) {
                    backOrderDetailsFab.visibility = View.VISIBLE
                } else {
                    backOrderDetailsFab.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}