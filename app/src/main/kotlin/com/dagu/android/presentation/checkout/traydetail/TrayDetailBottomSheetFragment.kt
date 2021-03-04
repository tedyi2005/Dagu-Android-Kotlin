package com.dagu.android.presentation.checkout.traydetail

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.R
import com.dagu.android.data.authentication.model.OrderProducts
import com.dagu.android.data.manager.account.ShakeShackAccountGeneral
import com.dagu.android.data.menu.MenuCategory
import com.dagu.android.data.menu.Product
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentTrayDetailBottomSheetBinding
import com.dagu.android.presentation.account.viewmodel.AccountOverviewViewModel
import com.dagu.android.presentation.authentication.AuthenticationViewModel
import com.dagu.android.presentation.base.AuthenticationCallbackListener
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment
import com.dagu.android.util.Utils
import com.dagu.android.util.showToast
import com.dagu.android.util.themeColor
import com.dagu.android.util.toDisplayCost
import com.dagu.android.util.ui.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.LazyThreadSafetyMode.NONE

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class TrayDetailBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private lateinit var binding: FragmentTrayDetailBottomSheetBinding

    private val authenticationViewModel: AuthenticationViewModel by activityViewModels()
    private val accountOverviewViewModel: AccountOverviewViewModel by activityViewModels()

    private var checkoutButtonExpanded = false
    private lateinit var trayDetailAdapter: TrayDetailAdapter
    private var firstSuggestionItemAdded = false
    private var secondSuggestionItemAdded = false

    private var mockProducts = mutableListOf<Product>()

    private lateinit var checkoutListener: OnCheckoutButtonPressed
    private lateinit var authenticationCallbackListener: AuthenticationCallbackListener

    // For reorder cases, we'll use the STATE_COLLAPSED state, but we need to ignore some logic
    // associated to such state the first time the bottom sheet is created. This flag is for that.
    private var reorderDetailOpened = false

    private val behavior: BottomSheetBehavior<FrameLayout> by lazy(NONE) {
        BottomSheetBehavior.from(binding.backgroundContainer)
    }

    private val bottomSheetCallback = TrayDetailBottomSheetCallback()

    private val backgroundShapeDrawable: MaterialShapeDrawable by lazy(NONE) {
        val backgroundContext = binding.backgroundContainer.context
        MaterialShapeDrawable(
            backgroundContext,
            null,
            R.attr.bottomSheetStyle,
            0
        ).apply {
            fillColor = ColorStateList.valueOf(
                backgroundContext.themeColor(R.attr.colorPrimarySurfaceVariant)
            )
            elevation = resources.getDimension(R.dimen.size_8dp)
            initializeElevationOverlay(requireContext())
        }
    }

    private val closeDrawerOnBackPressed = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            close()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCheckoutButtonPressed) {
            checkoutListener = context
        }
        if (context is AuthenticationCallbackListener) {
            authenticationCallbackListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, closeDrawerOnBackPressed)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrayDetailBottomSheetBinding.inflate(inflater, container, false)
        binding.backgroundContainer.setOnApplyWindowInsetsListener { view, windowInsets ->
            // Record the window's top inset so it can be applied when the bottom sheet is slide up
            // to meet the top edge of the screen.
            view.setTag(
                R.id.tag_system_window_inset_top,
                windowInsets.systemWindowInsetTop
            )
            windowInsets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSignInBinding()

        binding.run {
            backgroundContainer.background = backgroundShapeDrawable
            backgroundContainer.visibility = View.GONE
            scrimView.visibility = View.GONE
            scrimView.alpha = 1F

            scrimView.setOnClickListener { close() }

            bottomSheetCallback.apply {
                // Scrim view transforms
                addOnSlideAction(AlphaSlideAction(scrimView))

                addOnStateChangedAction(object : OnStateChangedAction {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            binding.backgroundContainer.visibility = View.GONE
                            scrimView.visibility = View.GONE
                            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
                        }
                    }
                })

                // move checkout button
                addOnStateChangedAction(object : OnStateChangedAction {
                    override fun onStateChanged(sheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            moveCheckoutButtonGuideLine(true)
                        } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            if (!reorderDetailOpened)
                                reorderDetailOpened = true // Don't move the checkout button the first time
                            else
                                moveCheckoutButtonGuideLine(false)
                        }
                    }
                })
            }

            behavior.addBottomSheetCallback(bottomSheetCallback)
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

            binding.apply {

                checkoutButton.setOnClickListener {
                    checkoutListener.navigateToCheckoutScreen(true)
                }

                // Load these two images to match mock image being added to the list
                ViewUtils.loadImage(
                    "https://ssapp-stage.s3.amazonaws.com/Breakfast_SausageEgg_App_crop_lg1557257154.jpeg",
                    firstSuggestionItemImage
                )

                ViewUtils.loadImage(
                    "https://ssapp-stage.s3.amazonaws.com/Breakfast_SausageEgg_App_crop_lg1557257154.jpeg",
                    secondSuggestionItemImage
                )

                closeTrayDetailBottomSheetButton.setOnClickListener {
                    close()
                }

                addSecondSuggestionItemButton.setOnClickListener {
                    trayRootView.setTransition(R.id.remove_second_add_button_transition)
                    trayRootView.transitionToEnd()
                    secondSuggestionItemAdded = true
                }

                addFirstSuggestionItemsButton.setOnClickListener {
                    trayRootView.setTransition(R.id.remove_first_add_button_transition)
                    trayRootView.transitionToEnd()
                    firstSuggestionItemAdded = true
                }
                trayDetailScrollView.viewTreeObserver.addOnScrollChangedListener {
                    val scrollViewHeight: Int =
                        trayDetailScrollView.getChildAt(0).bottom - trayDetailScrollView.height
                    val getScrollY: Int = trayDetailScrollView.scrollY
                    val scrollPosition = getScrollY.toDouble() / scrollViewHeight.toDouble() * 100.0
                    // If scroll position is over 95 expand the checkout button
                    if (scrollPosition >= 95.00) {
                        expandCheckoutButton(true)
                    } else {
                        expandCheckoutButton(false)
                    }
                }

                trayRootView.setTransitionListener(object :
                    MotionLayout.TransitionListener {
                    override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {}

                    override fun onTransitionChange(
                        p0: MotionLayout?,
                        p1: Int,
                        p2: Int,
                        p3: Float
                    ) {
                    }

                    override fun onTransitionCompleted(p0: MotionLayout?, currentId: Int) {
                        when (currentId) {
                            R.id.second_add_item_removed_end -> {
                                binding.trayRootView.setTransition(R.id.add_second_item_to_list_transition)
                                binding.trayRootView.transitionToEnd()
                                trayDetailAdapter.onProductAdded(mockProducts[0])
                                if (firstSuggestionItemAdded) {
                                    binding.firstSuggestionItemImage.alpha = 0f

                                }
                            }
                            R.id.move_second_image_to_list_end -> {
                                if (firstSuggestionItemAdded) {
                                    removeSuggestionItemsViews()
                                }
                            }

                            R.id.first_add_item_removed_end -> {
                                binding.trayRootView.setTransition(R.id.add_first_item_to_list_transition)
                                binding.trayRootView.transitionToEnd()

                                trayDetailAdapter.onProductAdded(mockProducts[0])
                                if (secondSuggestionItemAdded) {
                                    binding.secondSuggestionItemImage.alpha = 0f
                                }
                            }
                            R.id.move_first_image_to_list_end -> {
                                binding.firstSuggestionItemImage.alpha = 0f
                                if (secondSuggestionItemAdded) {
                                    binding.secondSuggestionItemImage.alpha = 0f
                                    removeSuggestionItemsViews()
                                }
                            }
                        }
                    }

                    override fun onTransitionTrigger(
                        p0: MotionLayout?,
                        p1: Int,
                        p2: Boolean,
                        p3: Float
                    ) {
                    }
                })
            }

        }

        setUpProductsInTrayDetail()
    }

    @ExperimentalCoroutinesApi
    private fun setUpSignInBinding() {
        binding.apply {
            trayDetailSignInText.setOnClickListener {
                context?.let {
                    authenticationCallbackListener.getTokenForAccountCreateIfNeeded(
                        accountType = ShakeShackAccountGeneral.ACCOUNT_TYPE,
                        authTokenType = ShakeShackAccountGeneral.AUTH_TOKEN_TYPE_FULL_ACCESS
                    )
                }
            }

            accountOverviewViewModel.userProfile.observe(viewLifecycleOwner, { userProfile ->
                if (userProfile != null) {
                    trayDetailUserInitialsTextView.text =
                        Utils.getInitials(userProfile.firstName, userProfile.lastName)
                    trayDetailUserInitialsTextView.visibility = View.VISIBLE

                    trayDetailSignInText.visibility = View.GONE
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

            authenticationViewModel.oloUserContactData.observe(
                viewLifecycleOwner,
                { oloUserContactDetails ->
                    when (oloUserContactDetails) {
                        is UIResult.Success -> {
                            accountOverviewViewModel.setUserProfileFromOloUserContactDetails(
                                oloUserContactDetails.data
                            )
                        }
                        is UIResult.Loading -> {
                            // TODO add code if needed
                        }
                        is UIResult.Error -> {
                            requireContext().showToast(
                                oloUserContactDetails.error,
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                })

            // Show the sign in CTA again when the user is logged out
            authenticationViewModel.authToken.observe(viewLifecycleOwner, { authToken ->
                if (authToken.isNullOrEmpty()) {
                    trayDetailUserInitialsTextView.visibility = View.GONE

                    trayDetailSignInText.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun setUpProductsInTrayDetail() {
        // Show the default order in tray:
        showOrderProductsInTrayDetail(emptyList())
        binding.subtotalAmount.text = "$21.30"

        // Observe the currentOrder in view model to update tray detail:
        accountOverviewViewModel.currentOrder.observe(viewLifecycleOwner, { currentOrder ->
            if (currentOrder != null) {
                showOrderProductsInTrayDetail(currentOrder.products.toTrayDetailProducts())
                binding.subtotalAmount.text = currentOrder.getSubtotalPlusTaxes().toDisplayCost()
            }
        })
    }

    private fun showOrderProductsInTrayDetail(products: List<Product>) {

        val productsToShow: MutableList<Product> = if (products.isEmpty()) {
            // If no order provided, show an example order in tray detail...
            val data: String? =
                Utils.getJsonDataFromAsset(requireContext(), "demo_vendor_menu.json")
            val listType = object : TypeToken<List<MenuCategory>>() {}.type
            val menuProductItem: List<MenuCategory> = Gson().fromJson(data, listType)

            // Add a mock product for upsell examples:
            mockProducts.add(menuProductItem[0].products!![5])

            menuProductItem[0].products?.subList(0, 4)!!.toMutableList()
        } else {
            products.toMutableList()
        }

        trayDetailAdapter =
            TrayDetailAdapter(productsToShow, requireContext())
        binding.itemDetailRecyclerView.apply {
            adapter = trayDetailAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    fun openForCustomAnimation() {
        binding.scrimView.visibility = View.VISIBLE
        // STATE_HALF_EXPANDED renders no animation when the bottom sheet is opened for the first
        // time. In this case we want to do this since we'll rely on a MotionLayout for a custom
        // animation from the floating tray instead.
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun openWithRegularAnimation() {
        binding.scrimView.visibility = View.VISIBLE

        // Using post() to avoid a bug that skipped the open animation the first time the sheet was
        // opened with this state change:
        binding.backgroundContainer.post {
            // STATE_COLLAPSED renders the regular animation (i.e: Fade in + Scroll from the bottom)
            // when the bottom sheet is opened for the first time. We want this for reorders, since
            // there's no floating tray to start a custom animation:
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun expandCheckoutButton(expanded: Boolean) {
        binding.apply {
            if (expanded) {
                if (trayDetailLayout.currentState == R.id.move_checkout_button_down) {
                    trayDetailLayout.setTransition(R.id.expand_checkout_button_transition)
                    trayDetailLayout.transitionToEnd()
                }
            } else {
                if (trayDetailLayout.currentState == R.id.checkout_button_expand) {
                    trayDetailLayout.transitionToStart()
                    checkoutButtonExpanded = false
                }
            }
        }
    }

    fun moveCheckoutButtonGuideLine(bottomSheetExpandedToFullScreen: Boolean) {
        binding.apply {
            if (firstSuggestionItemAdded && secondSuggestionItemAdded && bottomSheetExpandedToFullScreen) {
                // If both item are added and scrollview is at the bottom expand checkout button
                trayDetailLayout.setTransition(R.id.expand_checkout_button_transition)
                trayDetailLayout.transitionToEnd()
            } else if ((!firstSuggestionItemAdded || !secondSuggestionItemAdded) && bottomSheetExpandedToFullScreen) {
                // Moved down checkout button once bottom sheet has been expanded to full screen
                trayDetailLayout.setTransition(R.id.move_checkout_button_transition)
                trayDetailLayout.transitionToEnd()
            } else {
                // Move checkout button back up once bottom sheet has moved up
                trayDetailLayout.transitionToState(R.id.move_checkout_button_down)
                trayDetailLayout.setTransition(R.id.move_checkout_button_transition)
                trayDetailLayout.transitionToStart()
            }
        }

    }

    private fun removeSuggestionItemsViews() {
        binding.apply {
            trayRootView.removeView(firstSuggestionItemImage)
            trayRootView.removeView(firstSuggestionItemInfo)
            trayRootView.removeView(firstSuggestionItemTitle)
            trayRootView.removeView(firstSuggestionItemReason)
            trayRootView.removeView(addFirstSuggestionItemsButton)
            trayRootView.removeView(secondSuggestionItemImage)
            trayRootView.removeView(secondSuggestionItemInfo)
            trayRootView.removeView(secondSuggestionItemTitle)
            trayRootView.removeView(secondSuggestionItemReason)
            trayRootView.removeView(addSecondSuggestionItemButton)
            almostThereTitle.visibility = View.INVISIBLE
            almostThereTitle.alpha = 0f
            trayRootView.invalidate()
            trayRootView.requestLayout()
            trayDetailScrollView.invalidate()
            trayDetailScrollView.requestLayout()
            trayDetailLayout.setTransition(R.id.expand_checkout_button_transition)
            trayDetailLayout.transitionToEnd()
        }
    }

    interface OnCheckoutButtonPressed {
        fun navigateToCheckoutScreen(isFromProductMenu: Boolean)
    }

    private fun List<OrderProducts>.toTrayDetailProducts(): MutableList<Product> {
        return map {
            Product(
                name = it.name,
                description = it.name,
                basePrice = it.totalCost,
            )
        }.toMutableList()
    }
}