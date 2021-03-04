package com.dagu.android.presentation.authentication

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.dagu.android.R
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.ForgottenPasswordSuccessScreenBinding
import com.dagu.android.databinding.FragmentForgotPasswordBinding
import com.dagu.android.presentation.base.AccountAuthenticatorActivityListener
import com.dagu.android.util.Utils
import com.dagu.android.util.afterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthenticationViewModel by viewModels()

    private lateinit var accountAuthenticatorActivityListener: AccountAuthenticatorActivityListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is AccountAuthenticatorActivityListener) {
            accountAuthenticatorActivityListener = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            addTextWatcher(emailEditText)
            closeButton.setOnClickListener {
                accountAuthenticatorActivityListener.closeBottomSheet()
            }
            sendButton.setOnClickListener {
                authViewModel.requestPasswordRecovery(binding.emailEditText.text.toString())
            }
        }

        authViewModel.forgottenPasswordResult.observe(
            this,
            { forgottenPasswordResult ->
                when (forgottenPasswordResult) {
                    is UIResult.Success -> {
                        binding.sendButton.isEnabled = true
                        setUpSuccessScreenBottomSheet()
                    }
                    is UIResult.Loading -> {
                        // TODO update with animation loader on the send button
                        binding.sendButton.isEnabled = false
                    }
                    is UIResult.Error -> {
                        binding.sendButton.isEnabled = true
                        showErrorToast(forgottenPasswordResult.error)
                    }
                }
            })
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun addTextWatcher(editText: EditText) {
        editText.afterTextChanged {
            val isEmailValid = Utils.isValidEmail(it)
            binding.sendButton.isEnabled = isEmailValid
            if (isEmailValid || it.isEmpty())
                binding.emailAddressInputLayout.error = null
            else
                binding.emailAddressInputLayout.error = resources.getString(R.string.invalid_email)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    @ExperimentalCoroutinesApi
    private fun setUpSuccessScreenBottomSheet() {
        val successScreenBinding = ForgottenPasswordSuccessScreenBinding.inflate(layoutInflater)
        val bottomSheetDialog = BottomSheetDialog(
            ContextThemeWrapper(requireActivity(), R.style.SSBottomSheetDialogTheme)
        )
        bottomSheetDialog.setContentView(successScreenBinding.root)
        // Canceling not allowed
        bottomSheetDialog.setCancelable(false)
        val standardBottomSheetBehavior =
            BottomSheetBehavior.from(successScreenBinding.root.parent as View)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Set Dragging off
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetDialog.show()

        // Apply click events
        successScreenBinding.apply {
            close.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            closeButton.setOnClickListener {
                // TODO redirect to login screen
                bottomSheetDialog.dismiss()
                accountAuthenticatorActivityListener.closeBottomSheet()
            }
            sendItAgain.setOnClickListener {
                authViewModel.requestPasswordRecovery(binding.emailEditText.text.toString())
                bottomSheetDialog.dismiss()
            }
        }
    }

    private fun showErrorToast(errorString: String) {
        Toast.makeText(requireActivity(), errorString, Toast.LENGTH_SHORT).show()
    }

}