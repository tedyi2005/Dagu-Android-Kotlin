package com.dagu.android.presentation.account.dialog

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dagu.android.data.authentication.model.Orders
import com.dagu.android.databinding.FragmentReorderDialogBinding
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReorderDialogFragment : BaseBottomSheetDialogFragment() {
    private var _binding: FragmentReorderDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReorderDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val order: Orders = arguments?.getSerializable("order") as Orders

        binding.apply {
            reorderButton.setOnClickListener {
                dismiss()
                navigationListener?.showTrayDetail(order, startView = null)
            }

            cancelReorderLinkTextview.apply {
                paintFlags = Paint.UNDERLINE_TEXT_FLAG

                setOnClickListener {
                    dismiss()
                }
            }

            closeReorderDialogButton.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}