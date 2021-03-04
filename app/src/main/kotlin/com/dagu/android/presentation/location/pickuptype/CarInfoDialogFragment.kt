package com.dagu.android.presentation.location.pickuptype

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dagu.android.R
import com.dagu.android.databinding.FragmentCarInfoDialogBinding

class CarInfoDialogFragment : DialogFragment() {

    private var _binding: FragmentCarInfoDialogBinding? = null
    private val binding get() = _binding!!
    private val args: CarInfoDialogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (args.isForCarColor) {
            binding.apply {
                carInfoDialogTitle.text = getString(R.string.car_color)
                carColorRadioGroup.visibility = View.VISIBLE
                carTypeRadioGroup.visibility = View.GONE
                carInfoDialogCancel.setOnClickListener {
                    dismiss()
                }


                carColorRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId != -1) {
                        carInfoDialogOk.isEnabled = true
                        carInfoDialogOk.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary_green
                            )
                        )
                    }
                }



                carInfoDialogOk.setOnClickListener {
                    val currentRadioButton =
                        view.findViewById<RadioButton>(carColorRadioGroup.checkedRadioButtonId)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "color",
                        currentRadioButton.text
                    )
                    findNavController().popBackStack()

                }
            }
        } else {
            binding.apply {
                carInfoDialogTitle.text = getString(R.string.car_type)
                carColorRadioGroup.visibility = View.GONE
                carTypeRadioGroup.visibility = View.VISIBLE
                carInfoDialogCancel.setOnClickListener {
                    dismiss()
                }

                carTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId != -1) {
                        carInfoDialogOk.isEnabled = true
                        carInfoDialogOk.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.primary_green
                            )
                        )
                    }
                }

                carInfoDialogOk.setOnClickListener {
                    val currentRadioButton =
                        view.findViewById<RadioButton>(carTypeRadioGroup.checkedRadioButtonId)
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "type",
                        currentRadioButton.text
                    )
                    findNavController().popBackStack()
                }
            }
        }

    }
}