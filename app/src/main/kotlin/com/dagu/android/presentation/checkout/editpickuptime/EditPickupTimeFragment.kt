package com.dagu.android.presentation.checkout.editpickuptime

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.dagu.android.R
import com.dagu.android.databinding.FragmentEditPickupTimeBinding
import com.dagu.android.presentation.checkout.CheckoutFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditPickupTimeFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentEditPickupTimeBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: DeliveryTimeAdapter
    private var updateButtonGuideLinePercent = 0.65f
    private val mockTimes = listOf(
        "12:30 - 12:45pm",
        "12:45 - 1:00pm",
        "1:00 - 1:15pm",
        "1:15 - 1:30pm",
        "1:30 - 1:45pm",
        "1:45 - 2:00pm",
        "2:00 - 2:15pm",
        "2:15 - 2:30pm",
        "2:30 - 2:45pm",
        "2:45 - 3:00pm"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditPickupTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        toggleExpandedState(true)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        toggleExpandedState(false)
                    }

                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

        })
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        binding.updateTextView.bringToFront()

        binding.closeBottomSheetBtn.setOnClickListener {
            dismiss()
        }

        binding.updateTextView.setOnClickListener {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                CheckoutFragment.CHECKOUT_TIME,
                mockTimes[adapter.currentTimeSelectedPosition]
            )
            findNavController().popBackStack()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }

    fun toggleExpandedState(expanded: Boolean) {
        val set = ConstraintSet()
        set.clone(binding.editPickupTimeLayout)
        updateButtonGuideLinePercent = if (expanded) 0.98f else 0.65f
        set.setGuidelinePercent(R.id.update_button_guideline, updateButtonGuideLinePercent)
        TransitionManager.beginDelayedTransition(binding.editPickupTimeLayout)
        set.applyTo(binding.editPickupTimeLayout)
    }

    private fun initRecyclerView() {
        adapter = DeliveryTimeAdapter(requireContext(), mockTimes)
        adapter.itemOnClick = {
            adapter.notifyItemChanged(it)
            adapter.notifyItemChanged(adapter.previousSelectedItemPosition)
            binding.updateTextView.isEnabled = it != 0
            changeUpdateTextViewBackgroundColor(it)
        }
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.itemAnimator = DefaultItemAnimator()
            it.setHasFixedSize(true)
            it.adapter = adapter
        }
    }

    private fun changeUpdateTextViewBackgroundColor(position: Int) {
        if (position == 0) {
            binding.updateTextView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_grey_rounded_corner)
            binding.updateTextView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.grey_500
                )
            )
        } else {
            binding.updateTextView.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.btn_green_rounded_corner)
            binding.updateTextView.setTextColor(Color.WHITE)
        }
    }

}