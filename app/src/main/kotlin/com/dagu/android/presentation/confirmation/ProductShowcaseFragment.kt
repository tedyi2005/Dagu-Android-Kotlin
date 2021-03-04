package com.dagu.android.presentation.confirmation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dagu.android.databinding.FragmentProductShowcaseBinding
import com.dagu.android.presentation.base.BaseBottomSheetDialogFragment
import com.dagu.android.presentation.confirmation.adapter.ProductShowcaseAdapter
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// This is reusable class showcases some products while performing a background operation.
// The title of the operation and the products to show are customisable
class ProductShowcaseFragment : BaseBottomSheetDialogFragment() {

    private var _binding: FragmentProductShowcaseBinding? = null
    private val binding get() = _binding!!

    companion object {
        //delay in milliseconds before task is to be executed
        const val REPEAT_MILLISECOND = 500

        // time in milliseconds between successive task executions.
        const val DELAY_MILLISECOND: Long = 5000 // 5 sec
        val TITLE: String
            get() = "ProductShowcaseFragment"

        fun newInstance(showcaseMessage: String): ProductShowcaseFragment {
            val args = Bundle()
            args.putString(Constants.ARG_SHOWCASE_MESSAGE, showcaseMessage)
            val fragment = ProductShowcaseFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        // full screen view
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // set isCancelable = false to prevent canceling
        isCancelable = true
        return BottomSheetDialog(requireContext(), theme).apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = ViewGroup.LayoutParams.MATCH_PARENT
            // disable dragging
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductShowcaseBinding.inflate(inflater, container, false)
        val showcaseMessage = arguments?.getString(Constants.ARG_SHOWCASE_MESSAGE)
        binding.showcaseMessage.text = showcaseMessage
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            // TODO: Use real products for this showcase
            val listType = object : TypeToken<List<ProductShowcase>>() {}.type
            val showcase = Utils.getJsonDataFromAsset(requireActivity(), "product_showcase.json")
            val showcaseList: ArrayList<ProductShowcase> = Gson().fromJson(showcase, listType)
            processingViewPager.adapter = ProductShowcaseAdapter(requireContext(), showcaseList)
            // Disable swapping
            processingViewPager.isUserInputEnabled = false
            // Start product show case handle
            lifecycleScope.launch {
                repeat(REPEAT_MILLISECOND) {
                    delay(DELAY_MILLISECOND)
                    val currentItem = binding.processingViewPager.currentItem
                    // Set next page/item without jumping back
                    binding.processingViewPager.setCurrentItem(
                        (currentItem % Int.MAX_VALUE) + 1, true
                    )
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class ProductShowcase(
    val id: Int? = null,
    val feature: String,
    val curiosity: String,
    val category: String,
    val image: String? = null
)
