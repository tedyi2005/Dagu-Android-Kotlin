package com.dagu.android.presentation.checkout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.crashlytics.internal.common.CommonUtils
import com.dagu.android.R
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentCheckoutBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.authentication.AuthenticationViewModel
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.presentation.confirmation.ProductShowcaseFragment
import com.dagu.android.util.afterTextChanged
import com.dagu.android.util.showToast
import com.dagu.android.view.widgets.TipCurrencyTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import java.util.*


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class CheckoutFragment : BaseFragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    // 0 is curbside 1 is walk up, 2 Indoor pickup
    private var currentHandOffMode = -1

    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val accountOverviewViewModel: AccountOverviewViewModel by activityViewModels()

    // TODO: Use CheckoutViewModel
    //private val checkoutViewModel: CheckoutViewModel by viewModels()

    companion object {
        const val CHECKOUT_TIME = "CHECKOUT_TIME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).apply {
            duration = 600L
        }

        enterTransition = MaterialElevationScale(true).apply {
            duration = 600L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            // TODO set up toolbar or back button for this fragment
            // setUpToolbarFromFragment(toolbarContainer.toolbar)

            //TODO Use this to update checkout view when we start to use order repo
//            lifecycleScope.launchWhenStarted {
//                viewModel.getCurrentTray().collect { tray ->
//
//                }
//            }

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("deliverAddress")
                ?.observe(viewLifecycleOwner, { location ->
                    //Deliver address would come from Tray in Production
                    //TODO set up Delivery details
                    if (location != null) {
                        val pickUpString = "Deliver To  $location"
                        handOffTypeLocationTitleTextView.text = pickUpString
                        handOffLocationTextView.text = location
                        handOffModeGroup.visibility = View.GONE
                    }
                })

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("locationName")
                ?.observe(viewLifecycleOwner, { location ->
                    //Pickup location would come from Tray in Production
                    if (location != null) {
                        val pickUpString = "Pickup from $location"
                        handOffTypeLocationTitleTextView.text = pickUpString
                    }
                })

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("locationAddress")
                ?.observe(viewLifecycleOwner, { address ->
                    //Pickup location would come from Tray in Production
                    if (address != null) {
                        handOffLocationTextView.text = address
                    }
                })

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("handOffMode")
                ?.observe(viewLifecycleOwner, { handOffMode ->
                    currentHandOffMode = handOffMode
                    when (handOffMode) {
                        0 -> {
                            handOffModeTitle.text = getString(R.string.curbside)
                            handOffModeImageView.setImageResource(R.drawable.ic_order_curbside)
                        }
                        1 -> {
                            handOffModeTitle.text = getString(R.string.walk_up_window)
                            handOffModeImageView.setImageResource(R.drawable.ic_order_walkup)
                        }
                        else -> {
                            handOffModeTitle.text = getString(R.string.indoor_pick_up)
                            handOffModeImageView.setImageResource(R.drawable.ic_order_indoor)
                        }
                    }

                    handOffModeDescription.text = getString(R.string.hand_off_mode_description)
                    handOffModeGroup.visibility = View.VISIBLE
                })

            contactInfoSignInText.setOnClickListener {
                context?.let {
                    authenticationListener?.getTokenForAccountCreateIfNeeded(
                        accountType = ShakeShackAccountGeneral.ACCOUNT_TYPE,
                        authTokenType = ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
                    )
                }
            }

            contactInfoEditButton.setOnClickListener {
                findNavController().navigate(R.id.action_checkout_fragment_to_contactInfoFragment)
            }

            handOffModeEditButton.setOnClickListener {
                findNavController().navigate(
                    CheckoutFragmentDirections.editPickupTypeAction(
                        null,
                        currentHandOffMode
                    )
                )
            }

            pickupTimeEditButton.setOnClickListener {
                findNavController().navigate(R.id.action_checkout_fragment_to_edit_pickup_time_fragment)
            }

            handOffLocationEditButton.setOnClickListener {
                findNavController().navigate(CheckoutFragmentDirections.actionCheckoutFragmentToEditPickupLocationSelection())
            }

            // Default hide right icon
            couponCodeInputLayout.isEndIconVisible = false
            // Apply coupon call
            couponCodeInputLayout.setEndIconOnClickListener {
                couponCodeInputLayout.error = null
                // Hide key board while coupon applied
                CommonUtils.hideKeyboard(requireActivity(), it)
                val couponCode = couponCodeEditText.text.toString().trim()
                if (couponCode.isCouponValid()) {
                    applyValidCouponCode(couponCode)
                    // hide right arrow icon
                    couponCodeInputLayout.isEndIconVisible = false
                } else {
                    // show right arrow icon
                    couponCodeInputLayout.isEndIconVisible = true
                    couponCodeInputLayout.error =
                        requireActivity().getString(R.string.invalid_coupon)
                }
            }

            payFab.setOnClickListener {
                val showcaseFragment =
                    ProductShowcaseFragment.newInstance(requireActivity().getString(R.string.processing_order))
                showcaseFragment.show(childFragmentManager, ProductShowcaseFragment.TITLE)
            }

            payButton.setOnClickListener {
                findNavController().navigate(R.id.order_tracking_action)
            }

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
                CHECKOUT_TIME
            )?.observe(viewLifecycleOwner, { result ->
                pickupTimeTitle.text =
                    getString(R.string.checkout_pickup_time_result, result)
            })

            // Set chip group checked change listener
            tipChipsContainer.setOnCheckedChangeListener { tipChipGroup, checkedId: Int ->
                // Get the checked chip instance from chip group
                val chipTextValue = tipChipGroup.findViewById<Chip>(checkedId)?.text
                // if "Other" chip is selected
                if (chipTextValue == resources.getString(R.string.checkout_other)) {
                    // Visible custom tip edit text
                    tipInputLayout.visibility = View.VISIBLE
                    // Set default value i.e $0.00
                    tipEditText.setText(resources.getString(R.string.default_tip_value))
                    // Set cursor focus
                    tipEditText.requestFocus()
                } else {
                    tipInputLayout.visibility = View.GONE
                }
            }
            // Set TextWatcher on tip editText
            val watcher = TipCurrencyTextWatcher(tipEditText)
            tipEditText.addTextChangedListener(watcher)
            addTextWatcher(couponCodeEditText)

            accountOverviewViewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
                if (userProfile != null) {
                    contactInfoDescription.text = userProfile.getShortSummaryForAccountOverview()
                    contactInfoDescription.visibility = View.VISIBLE
                    contactInfoEditButton.visibility = View.VISIBLE

                    contactInfoEmailInputLayout.visibility = View.GONE
                    contactInfoSignInText.visibility = View.GONE
                }
            })

            authenticationViewModel.userData.observe(viewLifecycleOwner, { userData ->
                when (userData) {
                    is UIResult.Success -> {
                        accountOverviewViewModel.setUserProfileFromUserData(userData.data)
                    }
                    is UIResult.Loading -> {
                        // TODO add code if needed
                    }
                    is UIResult.Error -> {
                        requireContext().showToast(userData.error, Toast.LENGTH_SHORT)
                    }
                }
            })

            authenticationViewModel.oloUserContactData.observe(viewLifecycleOwner, { oloUserContactDetails ->
                when (oloUserContactDetails) {
                    is UIResult.Success -> {
                        accountOverviewViewModel.setUserProfileFromOloUserContactDetails(oloUserContactDetails.data)
                    }
                    is UIResult.Loading -> {
                        // TODO add code if needed
                    }
                    is UIResult.Error -> {
                        requireContext().showToast(oloUserContactDetails.error, Toast.LENGTH_SHORT)
                    }
                }
            })

            // Show the sign in CTA again when the user is logged out
            authenticationViewModel.authToken.observe(viewLifecycleOwner, { authToken ->
                if (authToken.isNullOrEmpty()) {
                    contactInfoDescription.visibility = View.GONE
                    contactInfoEditButton.visibility = View.GONE

                    contactInfoEmailInputLayout.visibility = View.VISIBLE
                    contactInfoSignInText.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun applyValidCouponCode(couponCode: String) {
        // Create Dynamic TextView with clear icon and background
        val textBubble = createDynamicTextView(couponCode)
        val spannableStringBuilder =
            convertViewToDrawable(couponCode, textBubble, requireActivity())
        // Add bubbled coupon view on editText
        binding.couponCodeEditText.apply {
            text = spannableStringBuilder
            setSelection(spannableStringBuilder.length)
            // Clear focus
            clearFocus()
            // Apply onTouch method on EditText
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    // Convert TextView to drawable bitmap
    private fun convertViewToDrawable(
        couponCode: String, textView: TextView, context: Context
    ): SpannableStringBuilder {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        textView.measure(measureSpec, measureSpec)
        textView.layout(0, 0, textView.measuredWidth, textView.measuredHeight)
        val bitmap = Bitmap.createBitmap(
            textView.measuredWidth, textView.measuredHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.translate((-textView.scrollX).toFloat(), (-textView.scrollY).toFloat())
        textView.draw(canvas)
        val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
        val spannableStringBuilder = SpannableStringBuilder()
        bitmapDrawable.setBounds(
            0, 0, bitmapDrawable.intrinsicWidth, bitmapDrawable.intrinsicHeight
        )
        spannableStringBuilder.append(couponCode.toUpperCase(Locale.ROOT))

        // Make Span clickable
        spannableStringBuilder.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // Clear edit text while clicking on applied coupon code
                    binding.couponCodeEditText.text?.clear()
                    // show focus
                    binding.couponCodeEditText.requestFocus()
                }
            }, spannableStringBuilder.length - couponCode.length,
            spannableStringBuilder.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        // Set image span on SpannableStringBuilder
        spannableStringBuilder.setSpan(
            ImageSpan(bitmapDrawable, ImageSpan.ALIGN_BASELINE),
            spannableStringBuilder.length - couponCode.length,
            spannableStringBuilder.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableStringBuilder
    }

    private fun createDynamicTextView(couponCode: String?): TextView {
        //creating Bubbled textView dynamically
        val textView = TextView(requireActivity())
        textView.text = couponCode
        val padding8 = requireContext().resources.getDimensionPixelSize(R.dimen.size_8dp)
        val padding12 = requireContext().resources.getDimensionPixelSize(R.dimen.size_12dp)
        textView.setPadding(padding12, padding8, padding12, padding8)
        textView.setBackgroundResource(R.drawable.coupon_applied_bubble_background)
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_close, 0)
        textView.compoundDrawablePadding = padding12
        return textView
    }

    // Static coupon value for validation
    private fun String.isCouponValid(): Boolean {
        // TODO Service call to validate coupon code and remove this fixed value
        return this != "TEST"
    }

    private fun addTextWatcher(editText: EditText) {
        binding.couponCodeInputLayout.error = null
        editText.afterTextChanged { text ->
            binding.couponCodeInputLayout.isEndIconVisible = text.isNotBlank()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}
