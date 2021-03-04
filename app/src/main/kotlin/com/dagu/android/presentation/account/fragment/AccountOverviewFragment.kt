package com.dagu.android.presentation.account.fragment

import android.accounts.AccountManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dagu.android.R
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.repository.Result
import com.dagu.android.databinding.FragmentAccountOverviewBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AccountOverviewFragment : BaseFragment() {
    private var _binding: FragmentAccountOverviewBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    private lateinit var accountManager: AccountManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountManager = AccountManager.get(requireContext())

        _binding = FragmentAccountOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val accountPreferences = AccountPreferencesRepository(requireContext())
        accountPreferences.authToken.asLiveData().observe(viewLifecycleOwner, Observer {
            val authToken = it ?: return@Observer
            viewModel.getUserData(authToken)
        })

        accountPreferences.oloAuthToken.asLiveData().observe(viewLifecycleOwner, Observer {
            val oloToken = it ?: return@Observer
            viewModel.oloToken = oloToken
            viewModel.getOloUserRecentOrders(oloToken)
        })

        viewModel.serverError.observe(viewLifecycleOwner, { errorString ->
            if (errorString.isNotEmpty()) {
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
            }
        })

        binding.apply {

            setUpToolbarFromFragment(toolbarContainer.toolbar)
            viewModel.recentOrders.observe(viewLifecycleOwner, { result ->
                if (result is Result.Success) {
                    val orders = result.data.orders
                    if (orders.isEmpty()) {
                        orderHistoryOverviewContainer.wholeLayout.visibility = View.GONE
                        orderHistoryOverviewTitle.visibility = View.GONE
                        orderHistoryOverviewViewAllLink.visibility = View.GONE
                    } else {
                        orders.first().let { lastOrder ->

                            Utils.setTextOrHide(
                                lastOrder.getOrderProductNames(),
                                orderHistoryOverviewContainer.itemName
                            )
                            Utils.setTextOrHide(
                                lastOrder.getOrderDate(),
                                orderHistoryOverviewContainer.itemOrderDate
                            )
                            Utils.setTextOrHide(
                                lastOrder.deliveryMode,
                                orderHistoryOverviewContainer.deliveryMode
                            )
                            // view Order details page
                            orderHistoryOverviewContainer.wholeLayout.setOnClickListener {
                                it.findNavController().navigate(
                                    OrderHistoryFragmentDirections.actionOrderDetailsFragment(order = lastOrder)
                                )
                            }
                        }
                    }
                }
            })

            profileContainer.propertyTitle.text = getString(R.string.profile)
            viewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
                profileContainer.propertyDescription.text =
                    userProfile.getShortSummaryForAccountOverview()
            })

            paymentMethodsContainer.propertyTitle.text = getString(R.string.payment_methods)
            viewModel.paymentMethods.observe(viewLifecycleOwner, { paymentMethods ->
                if (paymentMethods == null || paymentMethods.isEmpty()) {
                    paymentMethodsContainer.propertyDescription.text = getString(R.string.none)
                } else {
                    paymentMethodsContainer.propertyDescription.text = paymentMethods.joinToString {
                        getString(
                            R.string.generic_title_and_description,
                            it.name,
                            it.lastFour
                        )
                    }

                    paymentMethodsContainer.propertyDescription
                        .setCompoundDrawablesRelativeWithIntrinsicBounds(
                            R.drawable.ic_vector_mastercard, 0, 0, 0
                        )
                }
            })

            addressesContainer.propertyTitle.text = getString(R.string.addresses)
            viewModel.addresses.observe(viewLifecycleOwner, { addresses ->
                addressesContainer.propertyDescription.text =
                    if (addresses == null || addresses.isEmpty()) {
                        getString(R.string.none)
                    } else {
                        addresses.joinToString {
                            getString(
                                R.string.generic_title_and_description,
                                it.name,
                                it.firstLine
                            )
                        }
                    }
            })

            allergensContainer.propertyTitle.text = getString(R.string.allergens)
            viewModel.allergens.observe(viewLifecycleOwner, { selectedAllergens ->
                allergensContainer.propertyDescription.text =
                    if (selectedAllergens == null || selectedAllergens.isEmpty()) {
                        getString(R.string.none_selected)
                    } else {
                        selectedAllergens
                            .filter { it.itemName != null }
                            .joinToString { it.itemName!! }
                    }
            })

            profileContainer.topSeparator.apply {
                layoutParams =
                    ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        requireContext().resources.getDimensionPixelSize(R.dimen.size_4dp)
                    )
                visibility = View.VISIBLE
            }

            orderHistoryOverviewViewAllLink.setOnClickListener {
                findNavController().navigate(R.id.order_history_action)
            }

            profileContainer.wholeLayout.setOnClickListener {
                findNavController().navigate(R.id.profile_action)
            }

            paymentMethodsContainer.wholeLayout.setOnClickListener {
                findNavController().navigate(R.id.payment_methods_action)
            }

            addressesContainer.wholeLayout.setOnClickListener {
                findNavController().navigate(R.id.user_addresses_action)
            }

            allergensContainer.wholeLayout.setOnClickListener {
                findNavController().navigate(R.id.user_allergens_action)
            }

            signOutLink.setOnClickListener {
                val availableAccounts =
                    accountManager.getAccountsByType(ShakeShackAccountGeneral.ACCOUNT_TYPE)

                if (availableAccounts.isNotEmpty()) {
                    authenticationListener?.removeAccount(availableAccounts[0])
                }
                findNavController().popBackStack()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
