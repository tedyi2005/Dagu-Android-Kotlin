package com.dagu.android.presentation.account.fragment.payment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.FragmentAddNewCardBinding
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.Utils
import com.dagu.android.util.ui.ViewUtils
import com.dagu.android.view.adapters.SingleSelectionAdapter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NewCardPaymentFragment : BaseFragment() {
    private var _binding: FragmentAddNewCardBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardNumberOptionList: ArrayList<SingleViewItem>
    private var previousLength = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewCardBinding.inflate(inflater, container, false)
        initCardNameList()
        setUpCardName()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setUpToolbarFromFragment(toolbarContainer.toolbar)
            newCardExpDateEditText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable) {}

                override fun beforeTextChanged(
                    chars: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    previousLength = before
                }

                override fun onTextChanged(
                    char: CharSequence,
                    start: Int,
                    removed: Int,
                    added: Int
                ) {
                    // month and date exp date validation, once user enter 2 numbers add the slash for them
                    val length: Int = newCardExpDateEditText.text.toString().trim().length
                    if (previousLength <= length && length < 3) {
                        val month: Int = newCardExpDateEditText.text.toString().toInt()
                        if (length == 1 && month >= 2) {
                            val autoFixStr = "0$month/"
                            newCardExpDateEditText.setText(autoFixStr)
                            newCardExpDateEditText.setSelection(3)
                        } else if (length == 2 && month <= 12) {
                            val autoFixStr: String =
                                newCardExpDateEditText.text.toString() + "/"
                            newCardExpDateEditText.setText(autoFixStr)
                            newCardExpDateEditText.setSelection(3)
                        } else if (length == 2 && month > 12) {
                            newCardExpDateEditText.setText("1")
                            newCardExpDateEditText.setSelection(1)
                        }
                    } else if (length == 5) {
                        newCardCvvEditText.requestFocus() // auto move to next edittext
                    }
                }
            })

            newCardExpDateEditText.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_DEL && newCardExpDateEditText.text.toString().length == 3) {
                    newCardExpDateEditText.setText(newCardExpDateEditText.text?.get(0).toString())
                    newCardExpDateEditText.setSelection(1)
                }
                return@setOnKeyListener false
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initCardNameList() {
        val listType = object : TypeToken<List<SingleViewItem>>() {}.type
        val cardNameList: String? =
            Utils.getJsonDataFromAsset(requireContext(), "example_card_name.json")
        cardNumberOptionList = Gson().fromJson(cardNameList, listType)
    }

    private fun setUpCardName() {
        val cardNumber = binding.cardNumberView.root
        cardNumber.choiceMode = GridView.CHOICE_MODE_SINGLE
        val adapter = SingleSelectionAdapter(requireContext(), cardNumberOptionList)
        cardNumber.adapter = adapter
        ViewUtils.setGridViewHeightBasedOnChildren(cardNumber, 2)
        cardNumber.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, position, _ ->
                // Get the selected item
                val selectedItem = adapterView.getItemAtPosition(position).toString()
                if (adapter.getSelectedPosition() == position)
                    adapter.setSelectedPosition(-1)
                else
                    adapter.setSelectedPosition(position)
                adapter.notifyDataSetChanged()
            }
    }


}