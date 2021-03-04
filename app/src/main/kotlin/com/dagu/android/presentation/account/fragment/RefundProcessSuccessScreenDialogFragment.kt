package com.dagu.android.presentation.account.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.dagu.android.R
import com.dagu.android.databinding.FragmentRefundProcessSuccessScreenBinding
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment

class RefundProcessSuccessScreenDialogFragment : BaseBottomSheetDialogFragment() {

    private var _binding: FragmentRefundProcessSuccessScreenBinding? = null
    private val binding get() = _binding!!

    private val args: RefundProcessSuccessScreenDialogFragmentArgs by navArgs()

    override fun onStart() {
        super.onStart()
        // full screen view
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // set isCancelable = false to prevent canceling
        isCancelable = true
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = ViewGroup.LayoutParams.MATCH_PARENT
            // disable dragging
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRefundProcessSuccessScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            closeBottomSheet.setOnClickListener{
                dismiss()
            }
            if (args.isRefunded) {
                somethingWrongWithOrderTitle.text =
                    resources.getString(R.string.something_wrong_with_order_title)
                refundSuccessInfoText.visibility = View.VISIBLE
                refundSuccessSectionSeparator.visibility = View.VISIBLE
            } else {
                somethingWrongWithOrderTitle.text =
                    resources.getString(R.string.not_refund_thanks_title)
                refundSuccessInfoText.visibility = View.GONE
                refundSuccessSectionSeparator.visibility = View.GONE
            }
            startNewOrder.setOnClickListener{
                findNavController().navigate(R.id.home_fragment_action)
                navigationListener?.expandCategoryMenu()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
