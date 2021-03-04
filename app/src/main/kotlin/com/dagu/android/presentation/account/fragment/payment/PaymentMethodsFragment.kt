package com.dagu.android.presentation.account.fragment.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.data.payment.PaymentMethod
import com.dagu.android.databinding.FragmentPaymentMethodsBinding
import com.dagu.android.presentation.account.adapter.PaymentMethodsAdapter
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PaymentMethodsFragment : BaseFragment() {
    private var _binding: FragmentPaymentMethodsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
            addPaymentButton.setOnClickListener {
                findNavController().navigate(R.id.add_payment_method_action)
            }
        }

        viewModel.paymentMethods.observe(viewLifecycleOwner, Observer { paymentMethods ->
            if (paymentMethods != null) {
                initRecyclerView(paymentMethods)
            }
        })

    }

    private fun initRecyclerView(paymentMethods: List<PaymentMethod>) {
        binding.paymentMethodsRecyclerView.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.itemAnimator = DefaultItemAnimator()
            this.setHasFixedSize(true)
            this.adapter = PaymentMethodsAdapter(requireContext(), paymentMethods)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
