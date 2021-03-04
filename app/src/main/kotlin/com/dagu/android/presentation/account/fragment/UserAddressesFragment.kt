package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.data.user.UserAddress
import com.dagu.android.databinding.FragmentUserAddressesBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.account.adapter.UserAddressesAdapter
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UserAddressesFragment : BaseFragment() {
    private var _binding: FragmentUserAddressesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
        }

        viewModel.addresses.observe(viewLifecycleOwner, Observer { userAddresses ->
            initRecyclerView(userAddresses)
        })
    }

    private fun initRecyclerView(userAddresses: List<UserAddress>) {
        binding.userAddressesRecyclerView.apply {
            this.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            this.itemAnimator = DefaultItemAnimator()
            this.setHasFixedSize(true)
            this.adapter = UserAddressesAdapter(requireContext(), userAddresses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
