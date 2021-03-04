package com.dagu.android.presentation.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.GridView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.R
import com.dagu.android.data.authentication.AccountPreferencesRepository
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.FragmentProfileBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.JsonExamples
import com.dagu.android.util.hideKeyboard
import com.dagu.android.util.ui.ViewUtils
import com.dagu.android.view.adapters.MultiSelectionAdapter
import com.dagu.android.view.adapters.SingleSelectionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AccountOverviewViewModel by activityViewModels()

    private val birthdayDatePicker = MaterialDatePicker.Builder.datePicker().build()

    private lateinit var genderOptionList: ArrayList<SingleViewItem>
    private lateinit var kidsOptionList: ArrayList<SingleViewItem>
    private lateinit var petsOptionList: ArrayList<SingleViewItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        initOptionLists()
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbarFromFragment(binding.toolbarContainer.toolbar)

        // Prepare view model with a token to perform Olo requests,
        // such as "update contact details" (phone):
        val accountPreferences = AccountPreferencesRepository(requireContext())
        accountPreferences.oloAuthToken.asLiveData().observe(viewLifecycleOwner, Observer {
            val oloToken = it ?: return@Observer
            viewModel.oloToken = oloToken
        })

        viewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
            binding.apply {
                firstNameEditText.setText(userProfile.firstName)
                lastNameEditText.setText(userProfile.lastName)
                emailEditText.setText(userProfile.email)
                phoneNumberEditText.setText(userProfile.phoneNumber)

                messagingSmsCheckBox.isChecked = userProfile.receiveSmsFromShakeShack == true
                messagingEmailsCheckBox.isChecked = userProfile.receiveMarketingEmails == true

                saveProfileButton.isEnabled = true // Disabled while no profile is emitted
                saveProfileButton.setOnClickListener {
                    it.hideKeyboard()
                    viewModel.updateContactInformation(
                        firstNameEditText.text.toString(),
                        lastNameEditText.text.toString(),
                        phoneNumberEditText.text.toString()
                    )
                }
            }

            genderOptionList.forEachIndexed { index, genderOption ->
                if (userProfile.genderSelection == genderOption) {
                    binding.genderView.root.setItemChecked(index, true)
                }
            }
            kidsOptionList.forEachIndexed { index, kidsOption ->
                if (userProfile.kidsSelection == kidsOption) {
                    binding.kidsView.root.setItemChecked(index, true)
                }
            }
            petsOptionList.forEachIndexed { index, petsOption ->
                if (userProfile.petsSelection?.contains(petsOption) == true) {
                    binding.petsView.root.setItemChecked(index, true)
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initOptionLists() {
        val listType = object : TypeToken<List<SingleViewItem>>() {}.type

        val genderData = JsonExamples.GENDER
        val kidsData = JsonExamples.KIDS
        val petsData = JsonExamples.PETS
        genderOptionList = Gson().fromJson(genderData, listType)
        kidsOptionList = Gson().fromJson(kidsData, listType)
        petsOptionList = Gson().fromJson(petsData, listType)
    }

    private fun initViews() {
        // Change Password view action
        binding.changePassword.setOnClickListener {
            findNavController().navigate(R.id.fragment_change_password_dialog)
        }
        // Birthday view actions
        birthdayDatePicker.addOnPositiveButtonClickListener { epochValue ->
            binding.birthdayEditText.setText(birthdayDatePicker.headerText)
            viewModel.onBirthdayEpochValueSelected(epochValue)
        }
        binding.birthdayEditText.setOnClickListener {
            birthdayDatePicker.show(parentFragmentManager, null)
        }
        binding.birthdayEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                birthdayDatePicker.show(parentFragmentManager, null)
            }
        }
        // Set views
        setUpGenderView()
        setUpKidsView()
        setUpPetsView()
    }

    private fun setUpGenderView() {
        val genderView = binding.genderView.root
        genderView.choiceMode = GridView.CHOICE_MODE_SINGLE
        val adapter = SingleSelectionAdapter(requireContext(), genderOptionList)
        genderView.adapter = adapter
        ViewUtils.setGridViewHeightBasedOnChildren(genderView, 2)
        genderView.onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                // Get the selected item
                val selectedItem = adapterView.getItemAtPosition(position).toString()
                if (adapter.getSelectedPosition() == position)
                    adapter.setSelectedPosition(-1)
                else
                    adapter.setSelectedPosition(position)
                adapter.notifyDataSetChanged()
            }
    }

    private fun setUpKidsView() {
        val kidsView = binding.kidsView.root
        kidsView.choiceMode = GridView.CHOICE_MODE_SINGLE
        val adapter = SingleSelectionAdapter(requireContext(), kidsOptionList)
        kidsView.adapter = adapter
        ViewUtils.setGridViewHeightBasedOnChildren(kidsView, 2)
        kidsView.onItemClickListener =
            OnItemClickListener { adapterView, view, position, id ->
                // Get the selected item
                val selectedItem = adapterView.getItemAtPosition(position).toString()
                if (adapter.getSelectedPosition() == position)
                    adapter.setSelectedPosition(-1)
                else
                    adapter.setSelectedPosition(position)
                adapter.notifyDataSetChanged()
            }
    }

    private fun setUpPetsView() {
        val petsView = binding.petsView.root
        val adapter = MultiSelectionAdapter(requireContext(), petsOptionList)
        petsView.adapter = adapter
        ViewUtils.setGridViewHeightBasedOnChildren(petsView, 2)
    }
}
