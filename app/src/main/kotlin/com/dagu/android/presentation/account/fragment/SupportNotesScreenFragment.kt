package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.dagu.android.databinding.FragmentSupportNotesScreenBinding
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.afterTextChanged

class SupportNotesScreenFragment : BaseFragment() {
    private var _binding: FragmentSupportNotesScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSupportNotesScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
            additionIssueDetailsEditText.apply {
                afterTextChanged {
                    submitIssueButton.isEnabled = it.isNotEmpty()
                }
            }
            submitIssueButton.setOnClickListener {
                it.findNavController().navigate(
                    // for testing only : if inout length is 1 goto refund success screen else non refund success screen
                    SupportNotesScreenFragmentDirections.refundProcessSuccessScreenDialogAction(
                        isRefunded = (additionIssueDetailsEditText.text?.length == 1)
                    )
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}