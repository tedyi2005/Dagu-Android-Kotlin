package com.dagu.android.presentation.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentContactInfoBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment
import com.dagu.android.util.afterTextChanged
import com.dagu.android.util.hideKeyboard

class ContactInfoFragment : BaseBottomSheetDialogFragment() {
    private var _binding: FragmentContactInfoBinding? = null
    private val binding get() = _binding!!
    private val accountOverviewViewModel: AccountOverviewViewModel by activityViewModels()
    var userFirstName: String? = null
    var userLastName: String? = null
    var userPhoneNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setOnBackPressedCallback { }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (accountOverviewViewModel.userProfile.value == null) {
            getUserInfo()
        }
        accountOverviewViewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
            binding.contactInfoEmailEditText.setText(userProfile.email)
            binding.contactInfoNameEditText.setText(userProfile.firstName)
            binding.contactInfoLastNameEditText.setText(userProfile.lastName)
            binding.contactInfoPhoneNumberEditText.setText(userProfile.phoneNumber)
            userFirstName = userProfile.firstName
            userLastName = userProfile.lastName
            userPhoneNumber = userProfile.phoneNumber
        })

        binding.closeBottomSheetBtn.setOnClickListener {
            dismiss()
        }

        addTextListeners()
        binding.contactSaveButton.setOnClickListener {
            accountOverviewViewModel.updateContactInformation(
                binding.contactInfoNameEditText.text.toString(),
                binding.contactInfoLastNameEditText.text.toString(),
                binding.contactInfoPhoneNumberEditText.text.toString()
            )
        }
        observeContactInformationUpdateResult()
    }

    private fun observeContactInformationUpdateResult() {
        accountOverviewViewModel.contactInformationUpdateResult.observe(this, { result ->
            if (result is UIResult.Success) {
                if (result.data) {
                    accountOverviewViewModel.updatePhoneNumberLiveData.postValue(
                        UIResult.Success(
                            false
                        )
                    )
                    accountOverviewViewModel.updateNameLiveData.postValue(UIResult.Success(false))
                    dismiss()

                }
            } else if (result is UIResult.Error) {
                Toast.makeText(requireContext(), result.error, Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun addTextListeners() {
        binding.contactInfoNameEditText.afterTextChanged {
            binding.contactSaveButton.isEnabled = userFirstName != null && userFirstName != it
        }

        binding.contactInfoLastNameEditText.afterTextChanged {
            binding.contactSaveButton.isEnabled = userLastName != null && userLastName != it
        }

        binding.contactInfoPhoneNumberEditText.afterTextChanged {
            binding.contactSaveButton.isEnabled = userPhoneNumber != null && userPhoneNumber != it

        }

    }


    private fun getUserInfo() {
        val accountPreferences = AccountPreferencesRepository(requireContext())
        accountPreferences.authToken.asLiveData().observe(viewLifecycleOwner, Observer {
            val authToken = it ?: return@Observer
            accountOverviewViewModel.authToken = authToken
            accountOverviewViewModel.getUserData(authToken)
        })
        accountPreferences.oloAuthToken.asLiveData().observe(viewLifecycleOwner, Observer {
            val oloToken = it ?: return@Observer
            accountOverviewViewModel.oloToken = oloToken
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.root.hideKeyboard()
        _binding = null
    }

}
