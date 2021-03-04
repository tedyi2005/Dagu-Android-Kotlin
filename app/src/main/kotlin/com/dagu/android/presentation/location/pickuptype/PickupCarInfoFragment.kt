package com.dagu.android.presentation.location.pickuptype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.dagu.android.databinding.FragmentCarPickupDetailBinding
import com.dagu.android.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PickupCarInfoFragment : BaseFragment() {
    private var _binding: FragmentCarPickupDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarPickupDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("color")
                ?.observe(viewLifecycleOwner, { carColor ->
                    carDetailColorEditText.setText(carColor)
                    startOrderButton.isEnabled = carDetailColorEditText.text.toString()
                        .isNotEmpty() && carDetailTypeEditText.text.toString().isNotEmpty()
                })

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("type")
                ?.observe(viewLifecycleOwner, { carType ->
                    carDetailTypeEditText.setText(carType)
                    startOrderButton.isEnabled = carDetailColorEditText.text.toString()
                        .isNotEmpty() && carDetailTypeEditText.text.toString().isNotEmpty()
                })
            carDetailColorEditText.setOnClickListener {
                findNavController().navigate(
                    PickupCarInfoFragmentDirections.carInfoDialogAction(isForCarColor = true)
                )
            }

            carDetailTypeEditText.setOnClickListener {
                findNavController().navigate(
                    PickupCarInfoFragmentDirections.carInfoDialogAction(isForCarColor = false)
                )
            }

            startOrderButton.setOnClickListener {
                // TODO: Add network call to update car info
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}