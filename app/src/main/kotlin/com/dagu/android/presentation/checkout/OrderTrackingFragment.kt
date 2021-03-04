package com.dagu.android.presentation.checkout

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.maps.android.ui.IconGenerator
import com.dagu.android.R
import com.dagu.android.data.location.Location
import com.dagu.android.databinding.FragmentOrderTrackingBinding
import com.dagu.android.presentation.base.BaseFragment
import com.dagu.android.util.Utils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class OrderTrackingFragment : BaseFragment() {
    private var _binding: FragmentOrderTrackingBinding? = null
    private val binding get() = _binding!!
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var googleMap: GoogleMap

    //For now this is hardcoded, can be either an argument passed to the fragment or could get it from the Tray
    private var isPickup = true

    //Used to test motionlayout transitions
    private var testStatusNumber = 0

    private val regularPeekHeight: Int by lazy {
        (Utils.getDevicePixelHeight(requireContext()) * 0.65).toInt()
    }

    companion object {
        private const val zoomLevel = 16.0f
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.closeOrderButton.setOnClickListener {
            findNavController().popBackStack()
        }
        setTextViews()

        // Set Map height according bottom sheet
        val params = binding.mapFragment.layoutParams
        params.height = (Utils.getDevicePixelHeight(requireContext()) * 0.50).toInt()
        binding.mapFragment.layoutParams = params

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync {
            googleMap = it
            addMarker()
            showCloseButtonWithZoomInAnimation()
        }
        setUpLocationListBottomSheet()

        binding.pickUpInfoView.pickUpInfoTitleTimeInfo.setOnClickListener {
            when (testStatusNumber) {
                //second status
                0 -> {
                    transitionToSecondStatus()

                }
                // third status
                1 -> {
                    transitionToThirdStatus()
                }
                //Last status
                2 -> {
                    transitionToFourthStatus()
                }
            }
        }

    }

    private fun setTextViews() {
        binding.pickUpInfoView.apply {
            // Hard coding time stamp for now
            pickUpInfoFirstStepTimeStamp.text = "2:30 PM"
            pickUpInfoSecondStepTimeStamp.text = "2:45 PM"
            pickUpInfoThirdStepTimeStamp.text = "3:00 PM"
            pickUpInfoFourthStepTimeStamp.text = "3:45 PM"
            if (isPickup) {
                pickUpInfoSecondStep.text = getString(R.string.see_you_soon)
                pickUpInfoSecondStepDetail.text = getString(R.string.we_putting_together_your_order)
                pickUpInfoThirdStep.text = getString(R.string.order_ready_for_pick_up)
                pickUpInfoThirdDetail.text = getString(R.string.order_is_waiting_for_pick_up)
            } else {
                pickUpInfoSecondStep.text = getString(R.string.order_has_been_picked_up)
                pickUpInfoSecondStepDetail.text =
                    getString(R.string.your_driver_is_grabbing_your_order)
                pickUpInfoThirdStep.text = getString(R.string.driver_is_on_his_way)
                pickUpInfoThirdDetail.text = getString(R.string.order_en_route)
            }
        }

    }

    private fun transitionToSecondStatus() {
        binding.pickUpInfoView.apply {
            pickUpInfoSecondStep.setTypeface(
                pickUpInfoFirstStep.typeface,
                Typeface.BOLD
            )
            motionLayout.setTransition(R.id.pick_up_info_preparing_order_transition)
            motionLayout.transitionToEnd()
            pickUpInfoFirstToSecondLine.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary_green
                )
            )
            testStatusNumber += 1

        }
    }

    private fun transitionToThirdStatus() {
        binding.pickUpInfoView.apply {
            pickUpInfoThirdStep.apply {
                this.setTypeface(pickUpInfoFirstStep.typeface, Typeface.BOLD)
            }
            pickUpSecondToThirdLine.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary_green
                )
            )
            pickUpInfoSecondGreenCircle.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.green_circle)
            motionLayout.setTransition(R.id.pick_up_info_ready_for_pick_up_transition)
            motionLayout.transitionToEnd()
            testStatusNumber += 1
        }
    }

    private fun transitionToFourthStatus() {
        binding.pickUpInfoView.apply {
            pickUpInfoFourthStep.apply {
                this.setTypeface(
                    binding.pickUpInfoView.pickUpInfoFirstStep.typeface,
                    Typeface.BOLD
                )
            }
            pickUpInfoThirdCircle.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.green_circle)
            pickUpInfoThirdToFourthLine.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.primary_green
                )
            )
            motionLayout.setTransition(R.id.pick_up_info_picked_up_transition)
            motionLayout.transitionToEnd()
            testStatusNumber += 1
        }
    }

    private fun addMarker(location: Location? = null) {
        val iconGenerator = IconGenerator(requireContext())
        //TODO update with store location in future
        iconGenerator.setBackground(
            ContextCompat.getDrawable(requireActivity(), R.drawable.green_circle)
        )
        val latLng = LatLng(41.881649, -87.625023)
        val markerOptions = MarkerOptions().icon(
            BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon())
        ).position(latLng).anchor(iconGenerator.anchorU, iconGenerator.anchorV)
        googleMap.addMarker(markerOptions)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        googleMap.setOnCameraMoveStartedListener {
            binding.searchThisAreaButton.visibility = View.VISIBLE
        }
    }


    private fun showCloseButtonWithZoomInAnimation() {
        val zoomInAnimation = ObjectAnimator.ofPropertyValuesHolder(
            binding.closeOrderButton,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f),
        )
        zoomInAnimation.startDelay = 700
        zoomInAnimation.duration = 200
        zoomInAnimation.start()
    }

    private fun setUpLocationListBottomSheet() {
        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.pickUpInfoBottomSheet)

        // Set the regular peek height to be used from now on, whenever the sheet gets collapsed:
        standardBottomSheetBehavior.setPeekHeight(regularPeekHeight, true)

        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {

                    BottomSheetBehavior.STATE_HIDDEN -> {
                        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_COLLAPSED,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_EXPANDED,
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset < 0.50)
                    standardBottomSheetBehavior.setPeekHeight(regularPeekHeight, true)

                if (slideOffset > 0.85)
                    binding.searchThisAreaButton.visibility = View.GONE
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
    }


}