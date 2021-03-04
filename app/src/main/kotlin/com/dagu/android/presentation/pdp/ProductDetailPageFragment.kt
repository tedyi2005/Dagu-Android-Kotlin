package com.dagu.android.presentation.pdp

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.MaterialContainerTransform
import com.dagu.android.R
import com.dagu.android.data.menu.Product
import com.dagu.android.data.pdp.MakeItBetterItem
import com.dagu.android.data.pdp.SingleViewItem
import com.dagu.android.databinding.FragmentProductDetailPageBinding
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils
import com.dagu.android.util.themeColor
import com.dagu.android.util.ui.ViewUtils
import com.dagu.android.view.adapters.CustomizeItemAdapter
import com.dagu.android.view.adapters.MakeItBetterAdapter
import com.dagu.android.view.adapters.SingleSelectionAdapter


class ProductDetailPageFragment : BaseFragment() {
    private var _binding: FragmentProductDetailPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var product: Product
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>

    private val args: ProductDetailPageFragmentArgs by navArgs()

    private var enterAnimationDone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.menu_nav_host_fragment
            duration = 600L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().themeColor(R.attr.colorSurface))
            addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {}

                override fun onTransitionEnd(transition: Transition) {
                    transition.removeListener(this)
                    binding.apply {
                        BottomSheetBehavior.from(detailsBottomSheet).state =
                            BottomSheetBehavior.STATE_COLLAPSED
                        back.visibility = View.VISIBLE
//                        makeFavorite.visibility = View.VISIBLE
                        enterAnimationDone = true
                    }

                }

                override fun onTransitionCancel(transition: Transition) {}
                override fun onTransitionPause(transition: Transition) {}
                override fun onTransitionResume(transition: Transition) {}
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailPageBinding.inflate(inflater, container, false)
        product = args.product
        binding.root.transitionName = product.images?.imageLg
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        binding.apply {
            back.visibility = View.GONE
//            makeFavorite.visibility = View.GONE

            setUpDetailsBottomSheet()
            setUpInitialViews()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpInitialViews() {
        setUpBasicDetails()
        setItemTypeView()
        setMakeItBetterView()
        setExpandableViews()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpBasicDetails() {
        binding.apply {
            val imageList = arrayListOf<ProductImageGallery>()

            // Add temp images
            product.images?.imageLg?.let { image ->
                imageList.add(
                    ProductImageGallery(
                        image, product.productTag, product.advanceProductTagRotation()
                    )
                )
            }
            product.images?.imageXlg?.let { image ->
                imageList.add(
                    ProductImageGallery(
                        image, product.productTag, product.advanceProductTagRotation()
                    )
                )
            }
            product.images?.imageMd?.let { image ->
                imageList.add(
                    ProductImageGallery(
                        image, product.productTag, product.advanceProductTagRotation()
                    )
                )
            }

            // if no image found, add empty url to show error image
            if (imageList.isEmpty())
                imageList.add(ProductImageGallery(""))

            if (imageList.isEmpty() || imageList.size == 1)
                productImageIndicator.visibility = View.GONE

            val productImageAdapter = ProductImageAdapter(requireContext(), imageList)
            productImageViewPager.adapter = productImageAdapter

            // Disable Indicators movements
            productImageIndicator.isClickable = false
            productImageIndicator.setOnTouchListener { _, _ -> true }
            // Image Indicators
            TabLayoutMediator(productImageIndicator, productImageViewPager) { _, _ -> }.attach()

            Utils.setTextOrHide(product.name, itemName)
            priceAndCalories.text =
                String.format(product.getDisplayCost() + product.getDisplayCalories() + product.getDisplayAttributes())
            Utils.setTextOrHide(product.description, description)
            setProductDisplayCost()

            back.setOnClickListener {
                findNavController().navigateUp()
            }
//            makeFavorite.setOnClickListener {
//                // TODO: Mark product as favorite
//            }
            greenBackArrow.setOnClickListener {
                findNavController().navigateUp()
            }

            countView.minus.setOnClickListener {
                modifyCount(increment = false)
            }
            countView.plus.setOnClickListener {
                modifyCount(increment = true)
            }
        }
    }

    private fun setUpDetailsBottomSheet() {
        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.detailsBottomSheet)

        // Set the bottom sheet to peek 32dp above the baseline of the image view pager:
        val viewPagerHeightDimen = R.dimen.size_368dp
        val peekHeight =
            Utils.getDevicePixelHeight(requireActivity()) -
                    requireActivity().resources.getDimensionPixelSize(viewPagerHeightDimen)
        standardBottomSheetBehavior.setPeekHeight(peekHeight, true)

        // Change the HIDDEN state from BottomSheetBehaviour back to COLLAPSED so it's
        // not possible to dismiss the BottomSheet from the MainActivity
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        if (enterAnimationDone)
                            standardBottomSheetBehavior.state =
                                BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // Visible green back arrow
                        binding.greenBackArrow.visibility = View.VISIBLE
                        // Add elevation on appbar
                        binding.stickyAppBarLayout.elevation =
                            resources.getDimension(R.dimen.size_10dp)
                        // Adjust Padding while stick
                        binding.stickyToolbar.setPadding(
                            resources.getDimension(R.dimen.size_24dp).toInt(), 0,
                            resources.getDimension(R.dimen.size_24dp).toInt(), 0
                        )
                    }
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_SETTLING -> {
                        // Hide green back arrow
                        binding.greenBackArrow.visibility = View.GONE
                        // remove elevation
                        binding.stickyAppBarLayout.elevation = 0f
                        // set initial padding
                        binding.stickyToolbar.setPaddingRelative(
                            resources.getDimension(R.dimen.size_28dp).toInt(), 0,
                            resources.getDimension(R.dimen.size_28dp).toInt(), 0
                        )
                    }
                    else -> {
                        // logic for handling this state isn't needed for now
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // TODO check animation for this section
            }
        }

        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun setItemTypeView() {
        val itemTypeView = binding.itemTypeView.root
        itemTypeView.choiceMode = GridView.CHOICE_MODE_SINGLE
        val flavours = product.collectFlavouredOptions()
        if (flavours.isNotEmpty()) {
            val singleViewItems = arrayListOf<SingleViewItem>()
            for (flavour in flavours) {
                singleViewItems.add(
                    SingleViewItem(
                        flavour.id, flavour.name,
                        flavour.getDisplayCost() + flavour.getDisplayCalories(), true
                    )
                )
            }
            val adapter = SingleSelectionAdapter(requireContext(), singleViewItems)
            itemTypeView.adapter = adapter
            ViewUtils.setGridViewHeightBasedOnChildren(itemTypeView, 2)
            itemTypeView.onItemClickListener =
                AdapterView.OnItemClickListener { adapterView, view, position, id ->
                    val selectedItem = adapterView.getItemAtPosition(position).toString()
                    if (adapter.getSelectedPosition() == position)
                        adapter.setSelectedPosition(-1)
                    else
                        adapter.setSelectedPosition(position)
                    adapter.notifyDataSetChanged()
                }
        } else
            itemTypeView.visibility = View.GONE

    }

    private fun setMakeItBetterView() {

        val categorizedOption = product.getCategorizedOptionForType(Constants.ADDITION)
        val additions = categorizedOption.options

        if (!additions.isNullOrEmpty()) {
            val makeItBetterItems = arrayListOf<MakeItBetterItem>()
            val customizeItems = arrayListOf<MakeItBetterItem>()
            for (addition in additions) {
                if (addition.getDisplayCost() != null && addition.cost!! > 0)
                    makeItBetterItems.add(
                        MakeItBetterItem(
                            addition.id, null,
                            addition.name, addition.getDisplayCost() + addition.getDisplayCalories()
                        )
                    )
                else {
                    var baseCalories = addition.baseCalories ?: ""
                    if (baseCalories.isNotEmpty())
                        baseCalories += " cals"
                    customizeItems.add(
                        MakeItBetterItem(
                            addition.id, null, addition.name, baseCalories
                        )
                    )
                }
            }

            if (makeItBetterItems.size > 0) {
                binding.makeItBetterText.visibility = View.VISIBLE
                binding.makeItBetterRecyclerView.visibility = View.VISIBLE
                binding.makeItBetterRecyclerView.also {
                    it.layoutManager = LinearLayoutManager(requireContext())
                    it.itemAnimator = DefaultItemAnimator()
                    it.setHasFixedSize(true)
                    it.adapter = MakeItBetterAdapter(requireContext(), makeItBetterItems)
                }
            }

            setCustomView(customizeItems)
        }
    }

    private fun setCustomView(customItems: ArrayList<MakeItBetterItem>) {
        if (customItems.isNotEmpty()) {
            binding.customize.root.visibility = View.VISIBLE
            binding.customizeDivider.visibility = View.VISIBLE
            binding.customize.customizeRecyclerView.also {
                it.layoutManager =
                    GridLayoutManager(requireActivity(), 2, LinearLayoutManager.VERTICAL, false)
                it.itemAnimator = DefaultItemAnimator()
                it.setHasFixedSize(true)
                it.adapter = CustomizeItemAdapter(requireContext(), customItems)
            }
            binding.customize.root.setOnClickListener {
                binding.customize.expand.rotation = binding.customize.expand.rotation + 180
                if (binding.customize.customizeRecyclerView.visibility == View.VISIBLE)
                    binding.customize.customizeRecyclerView.visibility = View.GONE
                else
                    binding.customize.customizeRecyclerView.visibility = View.VISIBLE
            }
        } else {
            binding.customize.root.visibility = View.GONE
            binding.customizeDivider.visibility = View.GONE
        }
    }

    private fun setExpandableViews() {
        binding.customize.itemName.text = getString(R.string.customize)
        binding.nutrition.itemName.text = getString(R.string.nutrition_info)

        // Handle empty or null Ingredients and Allergens value
        if (product.getIngredientsAndAllergens().isNullOrEmpty()) {
            binding.ingredientsDivider.visibility = View.GONE
            binding.ingredientsLayout.visibility = View.GONE
        } else {
            binding.ingredientsLayout.setText(
                getString(R.string.ingredients_and_allergens), product.getIngredientsAndAllergens()
            )
        }
    }

    private fun modifyCount(increment: Boolean) {
        val count = binding.countView.count
        val quantity = count.text.toString().toInt()
        val modification = if (increment) 1 else -1
        count.text = maxOf((quantity + modification), 1).toString()
        setProductDisplayCost()
    }

    private fun setProductDisplayCost() {
        val addItems = binding.addItems
        val quantity = binding.countView.count.text.toString().toBigDecimal()
        val productCost = product.getBestCostForDisplay().times(quantity)
        addItems.text = String.format("Add \$%.2f", productCost)
    }
}
