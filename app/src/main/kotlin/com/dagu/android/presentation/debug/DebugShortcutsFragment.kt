package com.dagu.android.presentation.debug

import android.accounts.AccountManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.dagu.android.R
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.databinding.FragmentDebugShortcutsBinding
import com.dagu.android.presentation.base.BaseFragment


class DebugShortcutsFragment : BaseFragment() {
    private var _binding: FragmentDebugShortcutsBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountManager: AccountManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountManager = AccountManager.get(requireContext())

        _binding = FragmentDebugShortcutsBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            allLocationsButton.setOnClickListener {
                navigationListener?.toggleBottomSheet(false)
                findNavController().navigate(R.id.locations_fragment_action, null)
            }

            getSupport.setOnClickListener {
                navigationListener?.toggleBottomSheet(false)
                findNavController().navigate(R.id.order_get_support_fragment, null)
            }
            refreshTestButton.setOnClickListener {
                // Proof of concept of token invalidation/refresh
                val accountPreferences = AccountPreferencesRepository(requireContext())

                accountPreferences.authToken.asLiveData().observe(viewLifecycleOwner, Observer {
                    val authToken = it ?: return@Observer

                    accountManager.invalidateAuthToken(
                        ShakeShackAccountGeneral.ACCOUNT_TYPE,
                        authToken
                    )

                    if (accountManager.accounts.isNotEmpty()) {
                        authenticationListener?.getTokenForAccount(
                            accountManager.accounts[0],
                            ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
                        )
                    }
                })
            }

            trayDetailButton.setOnClickListener {
                findNavController().navigate(R.id.checkout_main_action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
