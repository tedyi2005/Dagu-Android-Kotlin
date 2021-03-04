package com.dagu.android.presentation.menu

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.text.style.TextAppearanceSpan
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.transition.MaterialElevationScale
import com.dagu.android.R
import com.dagu.android.application.MainActivity
import com.dagu.android.data.menu.CategorizedOption
import com.dagu.android.data.menu.MenuCategory
import com.dagu.android.data.menu.Option
import com.dagu.android.data.menu.Product
import com.dagu.android.data.repository.Result
import com.dagu.android.databinding.FragmentMenuCategoryBinding
import com.dagu.android.presentation.OneTapTrayListener
import com.dagu.android.presentation.base.ShakeShackNavigationListener
import com.dagu.android.presentation.menu.adapter.MenuCategoryAdapter
import com.dagu.android.util.Constants
import com.dagu.android.util.toDisplayCost
import com.dagu.android.util.ui.CustomTypefaceSpan
import com.dagu.android.util.ui.ViewUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MenuCategoryFragment : Fragment(), OneTapTrayListener,
    MainActivity.MenuCategoryListener {
    companion object {
        const val TRAY_ROTATION_WHEN_ADDED = -15f
        const val TRAY_ROTATION_WHEN_REMOVED = 15f
        const val REMOVE_ITEM_TRANSITION_MILLISECONDS = 1800L
    }

    private var _binding: FragmentMenuCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MenuCategoryViewModel by activityViewModels()
    private var tabIcons = ArrayList<Int>()
    private var isScrolling = false
    private var sortedList: ArrayList<MenuCategory>? = null
    private var trayIsShowing = false

    //Temp array to store product added to Tray
    private var itemsInTray = mutableListOf<Product>()

    var recyclerViewState: Parcelable? = null

    private lateinit var oneTapTrayListener: OneTapTrayListener
    private lateinit var navigationListener: ShakeShackNavigationListener

    // TODO: Replace this demo with proper implementation of tray options
    private var itemAmount = 1

    //Get the current Product Information for the animation of removing the item from Tray
    private var currentItemGuideLinePercent: Float = 0f
    private var currentImageView: ImageView? = null
    private var currentItemIsDrink = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitTransition = MaterialElevationScale(false).apply {
            duration = 600L
        }

        reenterTransition = MaterialElevationScale(true).apply {
            duration = 600L
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        oneTapTrayListener = this
        _binding = FragmentMenuCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.menuCategory.observe(viewLifecycleOwner, { result ->
            if (result is Result.Success) {
                if (result.data.isNotEmpty()) {
                    initMenuCategoryView(result.data)
                    // Restore RecyclerView state to previously scrolled position
                    binding.recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
                }
            }

        })

        viewModel.serverError.observe(viewLifecycleOwner, { errorString ->
            if (errorString.isNotEmpty()) {
                Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
            }
        })

        setUpTrayOptions()
        displayTrayItems()
        binding.apply {
            tray.root.setOnClickListener { rootView ->
                navigationListener.showTrayDetail(order = null, startView = rootView)
            }
            motionLayout.setTransitionListener(object :
                MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    when (endId) {
                        R.id.all_food_items_removed,
                        R.id.all_drink_items_removed -> {
                            binding.productSelectedImageView.visibility = View.VISIBLE
                            binding.firstItemImageView.visibility = View.INVISIBLE
                        }
                    }
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {

                    when (currentId) {
                        R.id.adding_drink_start -> {
                            startTransition(R.id.drink_to_tray_transition)
                        }

                        R.id.item_lands_in_tray -> {
                            binding.productSelectedImageView.visibility = View.INVISIBLE
                            binding.firstItemImageView.visibility = View.VISIBLE
                            startTransition(R.id.display_expanded_tray_transition)
                        }

                        R.id.tray_option_start -> {
                            binding.motionLayout.setTransition(R.id.drink_to_tray_transition)
                            binding.motionLayout.transitionToStart()
                        }

                        R.id.all_food_items_removed,
                        R.id.all_drink_items_removed,
                        R.id.remove_tray_on_scroll -> {
                            displayTrayItems()
                            binding.productSelectedImageView.alpha = 0f
                        }

                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                }
            })
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OneTapTrayListener) {
            oneTapTrayListener = context
        }
        if (context is ShakeShackNavigationListener) {
            navigationListener = context
        }
    }

    private fun initMenuCategoryView(categories: List<MenuCategory>) {
        sortedList = sortCategory(categories)
        val adapter =
            MenuCategoryAdapter(
                categoryList = sortedList!!,
                oneTapTrayListener = oneTapTrayListener,
                navController = findNavController(),
                context = requireContext()
            )
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.itemAnimator = DefaultItemAnimator()
            it.setHasFixedSize(false)
            it.adapter = adapter
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (navigationListener.getBottomSheetState() != BottomSheetBehavior.STATE_EXPANDED) {
                    binding.recyclerView.stopScroll()
                }
                val position: Int = (binding.recyclerView.layoutManager as LinearLayoutManager)
                    .findFirstVisibleItemPosition()
                isScrolling = true
                binding.categoryTabs.getTabAt(position)?.select()

                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    isScrolling = false
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isScrolling = false
                if (trayIsShowing) {
                    clearSelectedTrayItem()
                    trayIsShowing = false
                }
            }
        })

        // Set the icons for each tab and actions
        setUpTabsFunctionality()
    }

    private fun setUpTabsFunctionality() {

        for (tabIcon in tabIcons) {
            binding.categoryTabs.addTab(binding.categoryTabs.newTab().setIcon(tabIcon))
        }

        val smoothScroller: RecyclerView.SmoothScroller =
            object : LinearSmoothScroller(context) {
                override fun calculateTimeForScrolling(dx: Int): Int {
                    // Try to cap scroll times to this amount of milliseconds, so longer distances
                    // are scrolled at a higher speed.
                    // Some things are computed on-the-fly by the OS, so things like the amount of
                    // items in between affect the actual time the animation takes, but this
                    // approach produces results that are good enough.
                    return 100
                }

                override fun getVerticalSnapPreference(): Int {
                    // Always snap to the top of the target item when the smooth scroll ends.
                    return SNAP_TO_START
                }
            }

        binding.categoryTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                if (isScrolling)
                    isScrolling = false
                else {
                    smoothScroller.targetPosition = position
                    binding.recyclerView.layoutManager?.startSmoothScroll(smoothScroller)

                    binding.categoryTabs.getTabAt(position)?.select()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun sortCategory(menuCategories: List<MenuCategory>): ArrayList<MenuCategory> {

        val sortedCategories = ArrayList<MenuCategory>()
        val favoriteProducts = ArrayList<Product>()
        for (menuCategory in menuCategories) {
            if (menuCategory.platform?.contains(Constants.ANDROID) == true) {
                when (menuCategory.name) {

                    Constants.CATEGORY_NAME_BREAKFAST ->
                        if (!tabIcons.contains(R.drawable.ic_breakfast))
                            tabIcons.add(R.drawable.ic_breakfast)
                    Constants.CATEGORY_NAME_BURGERS ->
                        if (!tabIcons.contains(R.drawable.ic_burgers))
                            tabIcons.add(R.drawable.ic_burgers)
                    Constants.CATEGORY_NAME_CHICKEN ->
                        if (!tabIcons.contains(R.drawable.ic_chickens))
                            tabIcons.add(R.drawable.ic_chickens)
                    Constants.CATEGORY_NAME_RETAIL ->
                        if (!tabIcons.contains(R.drawable.ic_retail))
                            tabIcons.add(R.drawable.ic_retail)
                    Constants.CATEGORY_NAME_SHAKES_FROZEN_CUSTARD ->
                        if (!tabIcons.contains(R.drawable.ic_custards))
                            tabIcons.add(R.drawable.ic_custards)
                    Constants.CATEGORY_NAME_DRINKS ->
                        if (!tabIcons.contains(R.drawable.ic_drinks))
                            tabIcons.add(R.drawable.ic_drinks)
                    else -> {
                        // Edge case: Avoid adding a menuCategory if it doesn't match our tab name
                        // constants.
                        continue
                    }
                }
                menuCategory.products?.filter { it.favorite }?.let { favoriteProducts.addAll(it) }
                sortedCategories.add(menuCategory)

            }
        }

        // Add favorite products
        if (favoriteProducts.isNotEmpty()) {
            val favoriteCategory = MenuCategory()
            favoriteCategory.name = Constants.CATEGORY_NAME_YOUR_FAVORITE
            favoriteCategory.products = favoriteProducts
            if (!tabIcons.contains(R.drawable.ic_favorites))
                tabIcons.add(0, R.drawable.ic_favorites)
            sortedCategories.add(0, favoriteCategory)
        }
        return sortedCategories
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Save RecyclerView state to restore scrolled position
        recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()

        _binding = null
    }

    private fun displayPDPWhenCustomizeIsTapped() {
        itemsInTray[itemsInTray.size - 1].images?.imageLg?.let {
            val productDetailImageTransitionName = it
            binding.firstItemImageView.transitionName = it
            val extras =
                FragmentNavigatorExtras(binding.firstItemImageView to productDetailImageTransitionName)
            val directions =
                MenuCategoryFragmentDirections.actionMenuCategoryFragmentToProductDetailFragment(
                    product = itemsInTray[itemsInTray.size - 1]
                )
            findNavController().navigate(directions, extras)
        }

    }

    // Tray related methods
    private fun setUpTrayOptions() {
        setUpTransitionListenersForTrayOptions()

        binding.trayOptions.apply {
            trayUnclassifiedSingletonButton.setOnClickListener {
                // After animation, though hidden, the button might end up in front of another
                // widget, so we disable clicks until the button is reset:
                trayUnclassifiedSingletonButton.isClickable = false

                binding.trayOptions.root.apply {
                    setTransition(R.id.transition_tray_unclassified_singleton_button_clicked)
                    transitionToEnd()
                }
            }

            trayAdditionButton.setOnClickListener {
                // After animation, though hidden, the button might end up in front of another
                // widget, so we disable clicks until the button is reset:
                trayAdditionButton.isClickable = false

                binding.trayOptions.root.apply {
                    setTransition(R.id.transition_tray_addition_button_clicked)
                    transitionToEnd()
                }
            }

            trayCustomizeButton.setOnClickListener {
                displayPDPWhenCustomizeIsTapped()
            }

            lessButtonTrayOption.setOnClickListener {
                if (itemAmount > 0) {
                    setItemAmountInSpinner(itemAmount - 1)
                    itemRemoveAnimation()
                }
            }

            moreButtonTrayOption.setOnClickListener {
                setItemAmountInSpinner(itemAmount + 1)
                itemAddedAnimation()
            }
        }
    }

    private fun setUpTransitionListenersForTrayOptions() {
        binding.apply {
            trayOptions.root.setTransitionListener(
                object : MotionLayout.TransitionListener {
                    override fun onTransitionCompleted(
                        motionLayout: MotionLayout?,
                        currentId: Int
                    ) {
                        when (currentId) {
                            R.id.constraint_set_tray_addition_button_clicked,
                            R.id.transition_tray_addition_button_clicked
                            -> {
                                startBounceAnimation(
                                    productSelectedImageView,
                                    secondItemImageView,
                                    thirdItemImageView,
                                    fourthItemImageView,
                                )
                            }
                        }
                    }

                    //region Unused functions
                    override fun onTransitionStarted(
                        motionLayout: MotionLayout?,
                        startId: Int,
                        endId: Int
                    ) {
                    }

                    override fun onTransitionChange(
                        motionLayout: MotionLayout?,
                        startId: Int,
                        endId: Int,
                        progress: Float
                    ) {
                    }

                    override fun onTransitionTrigger(
                        motionLayout: MotionLayout?,
                        triggerId: Int,
                        positive: Boolean,
                        progress: Float
                    ) {
                    }
                    //endregion
                }
            )
        }
    }

    private fun setItemAmountInSpinner(newAmount: Int) {
        itemAmount = newAmount
        binding.trayOptions.itemAmountTrayOption.text = newAmount.toString()
    }

    private fun itemAddedAnimation() {
        when (itemAmount) {
            2 -> {
                startTransition(R.id.add_second_item_transition)
            }

            3 -> {
                startTransition(R.id.add_third_item_transition)
            }

            4 -> {
                startTransition(R.id.add_fourth_item_transition)
            }
        }
    }

    private fun itemRemoveAnimation() {
        when (itemAmount) {
            0 -> {
                if (currentItemIsDrink) {
                    removeDrinkTransition()
                } else {
                    removeFoodTransition()
                }
            }
            1 -> {
                startTransition(R.id.remove_second_item_transition)
            }
            2 -> {
                startTransition(R.id.remove_third_item_transition)
            }

            3 -> {
                startTransition(R.id.remove_fourth_item_transition)
            }
        }
    }

    private fun removeDrinkTransition() {
        binding.motionLayout.getConstraintSet(R.id.all_drink_items_removed)
            .setGuidelinePercent(R.id.item_selected_guideline, currentItemGuideLinePercent)
        startTransition(R.id.removed_drink_tray_transition)
        removingItemFromTray()
        trayIsShowing = false
        currentImageView?.alpha = 0f
        currentImageView?.animate()?.alpha(1f)?.setDuration(REMOVE_ITEM_TRANSITION_MILLISECONDS)
            ?.start()
    }

    private fun removeFoodTransition() {
        binding.motionLayout.getConstraintSet(R.id.all_food_items_removed)
            .setGuidelinePercent(R.id.item_selected_guideline, currentItemGuideLinePercent)
        startTransition(R.id.removed_food_tray_transition)
        removingItemFromTray()
        trayIsShowing = false
        currentImageView?.alpha = 0f
        currentImageView?.animate()?.alpha(1f)?.setDuration(REMOVE_ITEM_TRANSITION_MILLISECONDS)
            ?.start()
    }

    private fun clearSelectedTrayItem() {
        binding.motionLayout.getConstraintSet(R.id.all_drink_items_removed)
            .setGuidelinePercent(R.id.item_selected_guideline, currentItemGuideLinePercent)
        binding.productSelectedImageView.visibility = View.INVISIBLE
        startTransition(R.id.remove_tray)
        removingItemFromTray()
    }

    private fun displayTrayItems() {
        binding.apply {
            when {
                itemsInTray.isEmpty() -> {
                    return
                }
                itemsInTray.size == 1 -> {
                    ViewUtils.loadCircularImage(
                        itemsInTray[0].images!!.imageLg!!,
                        itemNumberOneImageView
                    )
                    itemNumberOneImageView.visibility = View.VISIBLE

                }
                else -> {
                    itemNumberOneImageView.visibility = View.GONE
                    twoItemLayout.root.visibility = View.VISIBLE
                    // Unsafely unwrapping these for test purpose
                    ViewUtils.loadCircularImage(
                        itemsInTray[0].images!!.imageLg!!,
                        twoItemLayout.firstItemImageView
                    )
                    ViewUtils.loadCircularImage(
                        itemsInTray[1].images!!.imageLg!!,
                        twoItemLayout.secondItemImageView
                    )
                }
            }
        }
    }

    private fun startTransition(id: Int) {
        binding.motionLayout.setTransition(id)
        binding.motionLayout.transitionToEnd()
    }

    // Start the animation for adding a new item to Tray and get the current position of the item
    // image selected to know where to start the animation
    private fun animateProductIntoTrayAndTrayOptionsIntoScreen(
        productImageView: ImageView,
        product: Product,
        isDrink: Boolean
    ) {
        if (product.images?.imageLg.isNullOrEmpty()) {
            // TODO: Figure out what to do when there is no image
            return
        }
        trayIsShowing = true
        currentImageView = productImageView
        currentItemIsDrink = isDrink
        val imageViewXYPosition = IntArray(2)
        productImageView.getLocationOnScreen(imageViewXYPosition)
        binding.productSelectedImageView.x = imageViewXYPosition[0].toFloat()
        binding.productSelectedImageView.layoutParams =
            ConstraintLayout.LayoutParams(productImageView.width, productImageView.height)
        binding.productSelectedImageView.alpha = 1f
        productImageView.alpha = 0f
        binding.productSelectedImageView.visibility = View.VISIBLE
        if (isDrink) {
            setDrinkTransition(imageViewXYPosition)
        } else
            startTransition(R.id.food_to_tray_transition)
        rotateTray()
        loadProductImageToTrayImageViews(product)
        productImageView.animate().setDuration(2000).alpha(1f).start()
        startBounceAnimation(binding.tray.wholeLayout)
    }

    private fun startBounceAnimation(vararg viewsToBounce: View) {
        for (currentView in viewsToBounce) {
            if (currentView.isVisible) {
                val trayBounceAnimation = ObjectAnimator.ofPropertyValuesHolder(
                    currentView,
                    PropertyValuesHolder.ofFloat("scaleX", 0.8f),
                    PropertyValuesHolder.ofFloat("scaleY", 0.8f),
                )
                trayBounceAnimation.duration = 300
                trayBounceAnimation.startDelay = 500
                trayBounceAnimation.repeatMode = ValueAnimator.REVERSE
                trayBounceAnimation.repeatCount = 1
                trayBounceAnimation.start()
            }
        }
    }

    private fun loadProductImageToTrayImageViews(product: Product) {
        product.images?.imageLg?.let { imageURL ->
            ViewUtils.loadImage(imageURL, binding.productSelectedImageView)
            ViewUtils.loadCircularImage(imageURL, binding.firstItemImageView)
            ViewUtils.loadCircularImage(imageURL, binding.secondItemImageView)
            ViewUtils.loadCircularImage(imageURL, binding.thirdItemImageView)
            ViewUtils.loadCircularImage(imageURL, binding.fourthItemImageView)
        }
    }

    private fun setDrinkTransition(screen: IntArray) {
        binding.motionLayout.getConstraintSet(R.id.adding_drink_start)?.let {
            val percent = getGuideLinePercent(screen[1])
            currentItemGuideLinePercent = percent - 0.05f
            it.setGuidelinePercent(R.id.item_selected_guideline, percent)
        }
        startTransition(R.id.drink_to_tray_transition)
    }

    private fun getGuideLinePercent(yPosition: Int): Float {
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        return (yPosition).toFloat() / height.toFloat() - 0.05f
    }

    private fun removingItemFromTray() {
        rotateTray(false)
    }

    private fun rotateTray(addingItem: Boolean = true) {
        val trayRotationDegrees = if (addingItem) {
            TRAY_ROTATION_WHEN_ADDED
        } else {
            TRAY_ROTATION_WHEN_REMOVED
        }
        binding.tray.wholeLayout.animate().rotation(trayRotationDegrees).setStartDelay(400)
            .setDuration(500).start()
    }

    override fun onProductAdded(product: Product, imageView: ImageView, isDrink: Boolean) {
        itemsInTray.add(product)
        prepareTrayOptionsLayoutForProduct(product)
        animateProductIntoTrayAndTrayOptionsIntoScreen(imageView, product, isDrink)
        binding.itemNumberOneImageView.visibility = View.GONE
        binding.twoItemLayout.root.visibility = View.GONE
    }

    private fun prepareTrayOptionsLayoutForProduct(product: Product) {
        resetTrayOptionsLayout()

        product.categorizedOptions?.let { categorizedOptions ->
            if (categorizedOptions.isNotEmpty()) {
                binding.trayOptions.trayCustomizeButton.visibility = View.VISIBLE
            }
            for (categorizedOption in categorizedOptions) {
                when (categorizedOption.type) {
                    CategorizedOption.TYPE_SIZE,
                    CategorizedOption.TYPE_FLAVOR,
                    CategorizedOption.TYPE_SIZE_FLAVOR -> {
                        addOptionsToPicker(
                            categorizedOption.options,
                            binding.trayOptions.traySizePicker,
                            binding.trayOptions.traySizePickerRadioGroup
                        )
                    }
                    CategorizedOption.TYPE_UNCLASSIFIED -> {
                        showOptionsForUnclassified(categorizedOption)
                    }
                    CategorizedOption.TYPE_ADDITION -> {
                        showFirstAddition(categorizedOption)
                    }
                }
            }
        }
    }

    override fun onOptionAdded(option: Option) {
        resetTrayOptionsLayout()
    }

    private fun resetTrayOptionsLayout() {
        binding.trayOptions.apply {
            // Undo previous animations:
            root.transitionToState(R.id.constraint_set_base_tray_options_visibility)
            trayUnclassifiedSingletonButton.isClickable = true
            trayAdditionButton.isClickable = true

            setItemAmountInSpinner(1)

            trayAmountSpinner.visibility = View.VISIBLE
            traySizePicker.visibility = View.GONE
            trayUnclassifiedPicker.visibility = View.GONE
            trayUnclassifiedSingletonButton.visibility = View.GONE
            trayAdditionButton.visibility = View.GONE
            trayCustomizeButton.visibility = View.GONE
        }
    }

    private fun addOptionsToPicker(
        options: List<Option>?,
        picker: ConstraintLayout,
        radioGroup: RadioGroup
    ) {
        options?.let {
            radioGroup.removeAllViews()
            options.forEachIndexed { index, option ->
                addVariantToRadioGroup(
                    option.name,
                    option.cost,
                    addAsChecked = index == 0, // First option selected by default
                    radioGroup
                )
            }
            picker.visibility = View.VISIBLE
        }
    }

    private fun addVariantToRadioGroup(
        variantName: String?,
        variantCost: Double?,
        addAsChecked: Boolean,
        radioGroup: RadioGroup
    ) {
        val whitespaces = "  "
        val displayVariantCost = "$$variantCost"

        val nameTypefaceSpan =
            CustomTypefaceSpan(
                ResourcesCompat.getFont(
                    requireContext(),
                    R.font.futura_lt_pro_bold
                )!!, true
            )
        val priceTypefaceSpan =
            CustomTypefaceSpan(
                ResourcesCompat.getFont(
                    requireContext(),
                    R.font.futura_lt_pro_medium
                )!!, false
            )
        val spannable =
            SpannableStringBuilder().append("$variantName$whitespaces$displayVariantCost")
        spannable.setSpan(
            TextAppearanceSpan(requireContext(), R.style.ShakeShackRadioButtonText_Bold),
            0,
            "$variantName$whitespaces".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            nameTypefaceSpan,
            0,
            "$variantName$whitespaces".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            "$variantName$whitespaces".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            TextAppearanceSpan(requireContext(), R.style.ShakeShackRadioButtonText),
            "$variantName$whitespaces".length,
            "$variantName$whitespaces$displayVariantCost".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            priceTypefaceSpan,
            "$variantName$whitespaces".length,
            "$variantName$whitespaces$displayVariantCost".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val radioButton = RadioButton(requireContext())
        radioButton.background =
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.selector_radio_button_underline_pressed_black
            )
        radioButton.layoutDirection = View.LAYOUT_DIRECTION_RTL
        radioButton.setText(spannable, TextView.BufferType.SPANNABLE)
        radioButton.buttonTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green))

        radioGroup.addView(radioButton)
        radioButton.isChecked = addAsChecked
    }

    // If this UNCLASSIFIED categorized option has a populated  array of (sub)"options",
    // add them to a picker with a RadioGroup.
    // Example: ("Honey Mustard", "BBQ", "Shack Sauce", "Cheese Sauce (+$1.00)")
    //
    // Otherwise show a single bubble with the name of this UNCLASSIFIED categorized option.
    // Example: "Gluten Free Bun?"
    private fun showOptionsForUnclassified(categorizedOption: CategorizedOption) {
        categorizedOption.options?.let { options ->
            if (options.isNotEmpty()) {
                addOptionsToPicker(
                    categorizedOption.options,
                    binding.trayOptions.trayUnclassifiedPicker,
                    binding.trayOptions.trayUnclassifiedPickerRadioGroup
                )
            } else {
                binding.trayOptions.trayUnclassifiedSingletonName.text =
                    categorizedOption.description
                binding.trayOptions.trayUnclassifiedSingletonButton.visibility =
                    View.VISIBLE
            }
        }
    }

    private fun showFirstAddition(categorizedOption: CategorizedOption) {
        categorizedOption.options?.let { options ->
            for (option in options) {
                binding.trayOptions.trayAdditionName.text = option.name
                showAdditionCostIfNeeded(option)
                binding.trayOptions.trayAdditionButton.visibility = View.VISIBLE
                break
            }
        }
    }

    private fun showAdditionCostIfNeeded(option: Option) {
        binding.trayOptions.apply {
            if (option.cost == null) {
                trayAdditionPrice.visibility = View.GONE
            } else {
                val costWithTwoDecimals = option.cost.toDisplayCost()
                trayAdditionPrice.text =
                    getString(R.string.additional_cost, costWithTwoDecimals)
                trayAdditionPrice.visibility = View.VISIBLE
            }
        }
    }

    override fun onCategoryClicked(value: String) {
        sortedList?.forEachIndexed { position, category ->
            if (value.equals(category.name, ignoreCase = true)) {
                binding.categoryTabs.getTabAt(position)?.select()
                binding.recyclerView.scrollToPosition(position)
                return@forEachIndexed
            }
        }
    }

    override fun scrollToTop() {
        binding.apply {
            recyclerView.scrollToPosition(0)
            categoryTabs.getTabAt(0)?.select()
        }
    }
}
