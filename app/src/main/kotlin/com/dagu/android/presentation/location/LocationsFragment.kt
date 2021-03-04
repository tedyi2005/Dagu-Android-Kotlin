package com.dagu.android.presentation.location

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.google.maps.android.ui.IconGenerator
import com.dagu.android.R
import com.dagu.android.data.location.Location
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FragmentAllLocationsBinding
import com.dagu.android.presentation.checkout.editpickuplocation.EditPickupSearchLocationFragment
import com.dagu.android.presentation.checkout.editpickuplocation.EditPickupSearchLocationFragmentDirections
import com.dagu.android.util.Constants
import com.dagu.android.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LocationsFragment : Fragment(), OnLocationSelected {

    private var _binding: FragmentAllLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>
    private var googleMap: GoogleMap? = null


    private val regularPeekHeight: Int by lazy {
        (Utils.getDevicePixelHeight(requireContext()) * .55).toInt()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val viewModel: LocationsViewModel by activityViewModels()

    @FlowPreview
    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpLocationBottomSheetTabLayout()
        setUpLocationListBottomSheet()

        observeLocations()
        observeNearbyLocations()

        // Set Map height according bottom sheet
        val params = binding.mapFragment.layoutParams
        params.height = (Utils.getDevicePixelHeight(requireContext()) * 0.50).toInt()
        binding.mapFragment.layoutParams = params
        binding.closeLocationButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.closeBottomSheetBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        viewModel.lastSearchQuery.observe(viewLifecycleOwner, {
            if (it.isEmpty()) {
                displaySearchFullScreen()
            }
        })

        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true /* enabled by default */) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.checkout_fragment, false)
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }


    private fun displayMapFragment(locations: List<Location>) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment

        mapFragment.getMapAsync {
            googleMap = it
            addMarkers(locations)
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            standardBottomSheetBehavior.isDraggable = true
            binding.closeBottomSheetBtn.visibility = View.GONE
            showCloseButtonWithZoomInAnimation()
        }

    }

    private fun addMarkers(locations: List<Location>) {
        // To generate customise icon
        val iconGenerator = IconGenerator(requireContext())
        iconGenerator.setBackground(
            ContextCompat.getDrawable(requireActivity(), R.drawable.green_circle)
        )
        iconGenerator.setTextAppearance(R.style.ShakeShackTextView_GreenRounded)

        for ((index, location) in locations.withIndex()) {
            if (location.latitude != null && location.longitude != null) {
                // Collect longitude and latitude from locations
                val latLng = LatLng(location.latitude, location.longitude)
                // Put marker options
                val markerOptions = MarkerOptions().icon(
                    BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((index + 1).toString()))
                ).position(latLng).anchor(iconGenerator.anchorU, iconGenerator.anchorV)
                googleMap?.addMarker(markerOptions)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLng(latLng))
            }
        }
        // On touch map listener
        googleMap?.setOnCameraMoveStartedListener {
            binding.searchThisAreaButton.visibility = View.VISIBLE
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun observeLocations() {
        viewModel.locations.observe(viewLifecycleOwner, { locationListResult ->
            displayLocationListResult(locationListResult)
        })
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun observeNearbyLocations() {
        viewModel.nearbyLocations.observe(viewLifecycleOwner, { nearbyResults ->
            displayLocationListResult(nearbyResults)
        })
    }

    private fun displayLocationListResult(locationListResult: UIResult<List<Location>>?) {
        if (viewModel.lastSearchQuery.value != null && viewModel.lastSearchQuery.value!!.isEmpty()) {
            setUpLocationListBottomSheet()
            return
        }
        when (locationListResult) {
            is UIResult.Error ->
                Toast.makeText(requireContext(), locationListResult.error, Toast.LENGTH_SHORT)
                    .show()

            is UIResult.Loading -> {
                if (locationListResult.loading)
                    binding.spinner.visibility = View.VISIBLE
                else
                    binding.spinner.visibility = View.GONE
            }

            is UIResult.Success -> {
                // Add markers on Map
                displayMapFragment(locationListResult.data)

            }
        }
    }

    private fun showCloseButtonWithZoomInAnimation() {
        val zoomInAnimation = ObjectAnimator.ofPropertyValuesHolder(
            binding.closeLocationButton,
            PropertyValuesHolder.ofFloat("scaleX", 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f),
        )
        zoomInAnimation.startDelay = 700
        zoomInAnimation.duration = 200
        zoomInAnimation.start()
    }


    private fun setUpLocationBottomSheetTabLayout() {
        val adapter = LocationTabAdapter(requireActivity(), this)
        binding.locationViewPager.adapter = adapter

        TabLayoutMediator(binding.tabs, binding.locationViewPager) { tab, position ->
            tab.text = MenuTabs.values()[position].title
        }.attach()

        binding.locationViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                displaySearchFullScreen()
            }
        })

        // Disable swapping
        binding.locationViewPager.isUserInputEnabled = false
    }

    private fun setUpLocationListBottomSheet() {
        standardBottomSheetBehavior = BottomSheetBehavior.from(binding.locationBottomSheet)

        // Hack to make it appear as expanded by default...
        val fakeExpandedPeekHeight = Utils.getDevicePixelHeight(requireContext())
        standardBottomSheetBehavior.setPeekHeight(fakeExpandedPeekHeight, false)
        displaySearchFullScreen()

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

    private fun displaySearchFullScreen() {
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        standardBottomSheetBehavior.isDraggable = false
        binding.closeBottomSheetBtn.visibility = View.VISIBLE
    }


    class LocationTabAdapter(
        activity: FragmentActivity,
        private val onLocationSelected: OnLocationSelected
    ) :
        FragmentStateAdapter(activity) {


        override fun getItemCount(): Int {
            return MenuTabs.values().size
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                MenuTabs.PICKUP.ordinal -> EditPickupSearchLocationFragment.newInstance(
                    true,
                    onLocationSelected
                )
                MenuTabs.DELIVERY.ordinal -> EditPickupSearchLocationFragment.newInstance(
                    false,
                    onLocationSelected
                )
                else -> EditPickupSearchLocationFragment()
            }
        }
    }

    enum class MenuTabs(val title: String) {
        PICKUP(Constants.PICKUP),
        DELIVERY(Constants.DELIVERY),
    }

    override fun pickUpLocationSelected(location: Location) {
        val navOptions = NavOptions.Builder().setPopUpTo(R.id.checkout_fragment, false).build()
        findNavController().navigate(
            EditPickupSearchLocationFragmentDirections.pickupTypeAction(location),
            navOptions
        )
    }

    override fun deliverAddressSelected(displayRegionOrLocation: DisplayRegionOrLocation) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            "deliverAddress",
            displayRegionOrLocation.name
        )
        findNavController().popBackStack()
    }

}

interface OnLocationSelected {
    fun pickUpLocationSelected(location: Location)
    fun deliverAddressSelected(displayRegionOrLocation: DisplayRegionOrLocation)
}
