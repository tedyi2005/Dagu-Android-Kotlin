package com.dagu.android.presentation.location.pickuptype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dagu.android.R
import com.dagu.android.databinding.FragmentPickupTypeBinding
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PickupTypeFragment : BaseFragment() {
    private var _binding: FragmentPickupTypeBinding? = null
    private val binding get() = _binding!!
    val args: PickupTypeFragmentArgs by navArgs()
    lateinit var pickupTypeAdapter
            : PickupTypeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickupTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        binding.pickupChangeLocationButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.closeBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        if (args.pickupType != -1) {
            pickupTypeAdapter.selectedPosition = args.pickupType
            pickupTypeAdapter.notifyDataSetChanged()
        }
    }

    private fun initRecyclerView() {
        pickupTypeAdapter = PickupTypeAdapter(requireContext())
        pickupTypeAdapter.itemOnClick = { itemPosition ->
            // TODO Make network call to update pickup type and go to menu
            pickupTypeAdapter.notifyDataSetChanged()
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "handOffMode",
                itemPosition
            )
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "locationAddress",
                args.location?.let { location ->
                    "${location.streetAddress}, ${location.city}"
                }
            )

            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "locationName",
                args.location?.name
            )
            // If Curbside or drive up order go to Car detail info
            if (itemPosition == 0 || itemPosition == 1) {
                val navOptions =
                    NavOptions.Builder().setPopUpTo(R.id.checkout_fragment, false).build()
                findNavController().navigate(R.id.pickup_car_action, null, navOptions)
            } else {
                findNavController().popBackStack()
            }
        }
        binding.pickupTypeRecyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.setHasFixedSize(true)
            it.adapter = pickupTypeAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}