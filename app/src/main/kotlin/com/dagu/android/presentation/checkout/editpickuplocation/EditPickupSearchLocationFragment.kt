package com.dagu.android.presentation.checkout.editpickuplocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.dagu.android.R
import com.dagu.android.data.location.Location
import com.dagu.android.data.location.Prediction
import com.dagu.android.data.location.Region
import com.dagu.android.data.repository.UIResult
import com.dagu.android.databinding.FarFromYouConfirmationLayoutBinding
import com.dagu.android.databinding.FragmentEditPickSearchLocationBinding
import com.dagu.android.databinding.FragmentNoNearLocationsBinding
import com.dagu.android.presentation.location.DisplayRegionOrLocation
import com.dagu.android.presentation.location.LocationAdapter
import com.dagu.android.presentation.location.LocationsViewModel
import com.dagu.android.presentation.location.OnLocationSelected
import com.dagu.android.util.PermissionStatus
import com.dagu.android.util.afterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
@AndroidEntryPoint
class EditPickupSearchLocationFragment :
    Fragment() {
    companion object {
        private const val LOCATION_REQUEST_CODE = 200

        const val LETS_TRY_PICK_UP_EVENT = 1
        const val TRY_DIFFERENT_LOCATION_EVENT = 2

        fun newInstance(
            isPickup: Boolean,
            onLocationSelected: OnLocationSelected
        ): EditPickupSearchLocationFragment {
            val fragment = EditPickupSearchLocationFragment()
            fragment.isPickup = isPickup
            fragment.onLocationSelected = onLocationSelected
            return fragment
        }


    }

    lateinit var onLocationSelected: OnLocationSelected
    var isPickup: Boolean = false

    var currentSelctedRegionOrLocation: DisplayRegionOrLocation? = null


    private var _binding: FragmentEditPickSearchLocationBinding? = null
    private val binding get() = _binding!!

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val viewModel: LocationsViewModel by activityViewModels()

    private lateinit var searchLocationAdapter: SearchLocationAdapter

    private lateinit var locationAdapter: LocationAdapter

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationPermissionStatus = PermissionStatus.CAN_ASK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPickSearchLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displaySearchRecyclerView()
        if (isPickup) {
            setUpSearchPickup()
        } else {
            setUpDeliverSearch()
        }


        binding.apply {
            addTextWatcher(pickupLocationSearchEditText)
            searchEditTextImageView.setOnClickListener {
                if (binding.pickupLocationSearchEditText.text.toString().isNotEmpty()) {
                    binding.pickupLocationSearchEditText.text?.clear()
                }
                /* //TODO Get Current Location
                else {

                }*/
            }

            searchSelectedClearImageView.setOnClickListener {
                clearSearchEditText()
            }
            addTextWatcher(binding.pickupLocationSearchEditText)
        }


        configureSearchEditTextButtonForLocationRequest()
    }


    private fun setUpDeliverSearch() {
        binding.pickupLocationSearchEditTextLayout.hint = getString(R.string.enter_address)
        observePredictions()
        if (viewModel.lastDeliverySearchQuery.value != null && viewModel.lastDeliverySearchQuery.value
            !!.isNotEmpty()
        ) {
            binding.pickupLocationSearchEditText.setText(viewModel.lastDeliverySearchQuery.value)
        }
    }

    private fun setUpSearchPickup() {
        // We observe both regions and place predictions.
        //
        // At first we request and show all regions, if the user starts typing for a search,
        // the results will be displayed, replacing the initial region list with a list of
        // predictions.
        //
        // If they clear the search field again, we should fall back to the initial region list.
        observeRegions()
        viewModel.getAllRegions()
        observeNearbyLocations()
        observeLocations()
        // TODO set up search for different locations
    }

    private fun clearSearchEditText() {
        displaySearchRecyclerView()
        binding.searchEditTextViewGroup.visibility = View.VISIBLE
        binding.selectedLocationViewGroup.visibility = View.GONE
        viewModel.updateSearchQuery("")
        when (viewModel.regions.value) {
            is UIResult.Success -> {
                searchLocationAdapter.updateDataSet(
                    (viewModel.regions.value as UIResult.Success<List<Region>>).data.toDisplayRegionItems(),
                    ""
                )
            }
            else -> {
                viewModel.getAllRegions()
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { activity ->
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        }
    }

    private fun displaySearchRecyclerView() {
        searchLocationAdapter = SearchLocationAdapter(requireContext())
        searchLocationAdapter.onItemClick = {
            if (isPickup) {
                when {
                    it.placeId != null -> {
                        viewModel.getNearbyLocations(it.placeId)
                    }
                    binding.pickupLocationSearchEditText.text.toString().isEmpty() -> {
                        it.regionId?.let { id ->
                            currentSelctedRegionOrLocation = it
                            viewModel.selectedRegionOrLocation(it, it.name)
                            invokeApi(null, null, id)
                        }
                    }
                    else -> {
                        findNavController().navigate(
                            EditPickupSearchLocationFragmentDirections.pickupTypeAction(
                                it.placeId
                            )
                        )
                    }
                }
            } else {
                viewModel.updateDeliverSearchQuery(binding.pickupLocationSearchEditText.text.toString())
                onLocationSelected.deliverAddressSelected(it)
            }
        }
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.itemAnimator = DefaultItemAnimator()
            it.setHasFixedSize(true)
            it.adapter = searchLocationAdapter
        }
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            (binding.recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
    }

    private fun displayLocationListResult(locationListResult: UIResult<List<Location>>) {
        if (viewModel.lastSearchQuery.value != null && viewModel.lastSearchQuery.value!!.isEmpty()) {
            displaySearchRecyclerView()
            return
        }
        when (locationListResult) {
            is UIResult.Error ->
                Toast.makeText(requireContext(), locationListResult.error, Toast.LENGTH_SHORT)
                    .show()

            is UIResult.Loading -> {
            }

            is UIResult.Success -> {
                displayLocationResult(locationListResult.data)
            }
        }

    }

    private fun currentLocationHasBeenSelected() {
        binding.apply {
            searchEditTextViewGroup.visibility = View.GONE
            selectedLocationViewGroup.visibility = View.VISIBLE
            searchSelectedLocationTextView.text = getString(R.string.my_current_location)
        }
    }

    private fun locationSearchedHasBeenSelected() {
        binding.apply {
            searchEditTextViewGroup.visibility = View.GONE
            selectedLocationViewGroup.visibility = View.VISIBLE
            searchSelectedLocationTextView.text = viewModel.lastSearchQuery.value
        }
    }

    private fun displayLocationResult(locations: List<Location>) {
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            (binding.recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        binding.recyclerView.removeItemDecoration(dividerItemDecoration)
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.itemAnimator = DefaultItemAnimator()
            it.setHasFixedSize(true)
            locationAdapter = LocationAdapter(requireContext(), locations)
            it.adapter = locationAdapter
        }
        locationAdapter.onItemClick = { location ->
            // Static far from you location items for testing purposes
            if (location.streetAddress.equals("266 Broadway")) {
                setUpFarFromYouNotificationBottomSheet(location)
            } else {
                pickUpLocationSelected(location)
            }
        }

        if (viewModel.selectedLocation.value != null) {
            val index = locationAdapter.findLocationIndex(viewModel.selectedLocation.value!!)
            locationAdapter.selectedPosition = index
            locationAdapter.notifyItemChanged(index)
        }

        if (currentLocationPermissionStatus == PermissionStatus.GRANTED) {
            currentLocationHasBeenSelected()
        } else {
            locationSearchedHasBeenSelected()
        }

    }

    private fun pickUpLocationSelected(location: Location) {
        viewModel.selectedLocation(location)
        onLocationSelected.pickUpLocationSelected(location)
    }


    private fun invokeApi(lat: Double?, lng: Double?, regionId: String?) {
        if (lat != null && lng != null) {
            viewModel.getNearbyLocations(lat, lng)
        } else if (regionId != null) {
            // TODO: Call getRegionLocations() when endpoint is created
            //viewModel.getRegionLocations(regionId)

            // For now fall back to all locations:
            Toast.makeText(
                context,
                "Endpoint for specific region not ready. Falling back to show all locations",
                Toast.LENGTH_LONG
            ).show()
            viewModel.getAllLocations()
        } else {
            viewModel.getAllLocations()
        }
    }

    private fun observeLocations() {
        viewModel.locations.observe(viewLifecycleOwner, { locationListResult ->
            displayLocationListResult(locationListResult)
        })
    }


    private fun observeRegions() {
        viewModel.regions.observe(viewLifecycleOwner, { regions ->
            when (regions) {
                is UIResult.Error ->
                    Toast.makeText(requireContext(), regions.error, Toast.LENGTH_SHORT)
                        .show()

                is UIResult.Loading -> {
                    // TODO add spinner
                }

                is UIResult.Success -> {
                    searchLocationAdapter.updateDataSet(
                        newItems = regions.data.toDisplayRegionItems(),
                        newSearchTerms = ""
                    )
                }
            }
        })
    }

    private fun observePredictions() {
        viewModel.placePredictions.observe(viewLifecycleOwner, { predictionsResult ->
            when (predictionsResult) {
                is UIResult.Error ->
                    Toast.makeText(
                        requireContext(),
                        predictionsResult.error,
                        Toast.LENGTH_SHORT
                    )
                        .show()

                is UIResult.Loading -> {
                    // TODO: Handle Loading state
                }

                is UIResult.Success -> {
                    searchLocationAdapter.updateDataSet(
                        newItems = predictionsResult.data.toDisplayLocationItems(),
                        newSearchTerms = viewModel.searchTermsToHighLight
                    )
                }
            }
        })
    }

    private fun observeNearbyLocations() {
        if (viewModel.lastSearchQuery.value != null && viewModel.lastSearchQuery.value!!.isEmpty()) {
            displaySearchRecyclerView()
            return
        }
        viewModel.nearbyLocations.observe(viewLifecycleOwner, { nearbyLocationsResult ->
            when (nearbyLocationsResult) {
                is UIResult.Error ->
                    Toast.makeText(
                        requireContext(),
                        nearbyLocationsResult.error,
                        Toast.LENGTH_SHORT
                    )
                        .show()

                is UIResult.Loading -> {
                    // TODO: Handle Loading state
                }

                is UIResult.Success -> {
                    if (nearbyLocationsResult.data.isEmpty()) {
                        setUpNoNearbyStoresBottomSheet()
                    } else {
                        val bundle = bundleOf(
                            "placeId" to viewModel.lastPlaceId
                        )

                        binding.searchEditTextViewGroup.visibility = View.GONE
                        binding.selectedLocationViewGroup.visibility = View.VISIBLE
                        binding.searchSelectedLocationTextView.text = "My Current Location"
//                        findNavController().navigate(R.id.nearby_locations_fragment_action, bundle)
                    }
                }
            }
        })
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun addTextWatcher(editText: EditText) {
        editText.afterTextChanged {
            if (it.isBlank()) {
                configureSearchEditTextButtonForLocationRequest()
                viewModel.getAllRegions()
            } else {
                configureSearchEditTextButtonForClearingInput()
                viewModel.searchLocations(it)
            }
        }
    }

    private fun configureSearchEditTextButtonForLocationRequest() {
        binding.searchEditTextImageView.apply {
            setOnClickListener {
                if (isPickup) {
                    requestLocationPermission()
                }
                //TODO figure out what to do with current location for Deliver
            }
            setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_pinpoint
                )
            )
        }
    }

    private fun configureSearchEditTextButtonForClearingInput() {
        binding.searchEditTextImageView.apply {
            setOnClickListener {
                binding.pickupLocationSearchEditText.text?.clear()
            }
            setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_clear_field
                )
            )

        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_REQUEST_CODE) {
            for (index in permissions.indices) {
                val permission = permissions[index]
                val newStatusInt = grantResults[index]
                val shouldShowRationale = shouldShowRequestPermissionRationale(permission)

                val newStatus = when {
                    newStatusInt == PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
                    newStatusInt == PackageManager.PERMISSION_DENIED && shouldShowRationale -> PermissionStatus.DENIED_CAN_ASK
                    newStatusInt == PackageManager.PERMISSION_DENIED && !shouldShowRationale -> PermissionStatus.DENIED_CANNOT_ASK
                    else -> PermissionStatus.CAN_ASK
                }

                when (newStatus) {
                    PermissionStatus.GRANTED -> {
                        requestDeviceLocation()
                    }
                    PermissionStatus.DENIED_CAN_ASK -> {
                        context?.let {
                            MaterialAlertDialogBuilder(it)
                                .setTitle(R.string.use_location_question)
                                .setMessage(R.string.location_permission_rationale)
                                .setPositiveButton(R.string.allow) { _, _ ->
                                    requestLocationPermission()
                                }
                                .setNegativeButton(R.string.deny) { _, _ -> }
                                .show()
                        }
                    }
                    PermissionStatus.DENIED_CANNOT_ASK -> {
                        context?.let {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.use_location_question)
                                .setMessage(R.string.location_permission_rationale_with_app_settings)
                                .setPositiveButton(R.string.allow_in_app_settings) { _, _ ->
                                    openOsAppSettings()
                                }
                                .setNegativeButton(R.string.deny) { _, _ -> }
                                .show()
                        }
                    }
                    PermissionStatus.CAN_ASK -> {
                        // Unlikely
                    }
                }

                currentLocationPermissionStatus = newStatus
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestDeviceLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: android.location.Location? ->
                context?.let { context ->
                    if (location == null) {
                        Toast.makeText(
                            context, R.string.could_not_get_your_location, Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.updateSearchQuery("My current location")
                        Toast.makeText(context, "User location: $location", Toast.LENGTH_LONG)
                            .show()
                        invokeApi(location.latitude, location.longitude, null)
                    }
                }
            }
    }

    private fun openOsAppSettings() {
        context?.let { context ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    private fun setUpNoNearbyStoresBottomSheet() {
        val noNearLocationBinding = FragmentNoNearLocationsBinding.inflate(layoutInflater)
        // Set rounded corner theme
        val bottomSheetDialog = BottomSheetDialog(
            ContextThemeWrapper(requireActivity(), R.style.SSBottomSheetDialogTheme)
        )
        bottomSheetDialog.setContentView(noNearLocationBinding.root)
        // Canceling not allowed
        bottomSheetDialog.setCancelable(false)
        val standardBottomSheetBehavior =
            BottomSheetBehavior.from(noNearLocationBinding.root.parent as View)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Set Dragging off
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetDialog.show()

        // Apply click events
        noNearLocationBinding.apply {
            closeBottomSheetBtn.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            letsTryPickUp.setOnClickListener {
                setOnClickEvents(LETS_TRY_PICK_UP_EVENT, bottomSheetDialog)
            }
            tryDifferentLocationButton.setOnClickListener {
                setOnClickEvents(TRY_DIFFERENT_LOCATION_EVENT, bottomSheetDialog)
            }

        }
    }

    private fun setUpFarFromYouNotificationBottomSheet(location: Location) {
        val farFromYouBinding = FarFromYouConfirmationLayoutBinding.inflate(layoutInflater)
        // Set rounded corner theme
        val bottomSheetDialog = BottomSheetDialog(
            ContextThemeWrapper(requireActivity(), R.style.SSBottomSheetDialogTheme)
        )
        bottomSheetDialog.setContentView(farFromYouBinding.root)
        bottomSheetDialog.setCancelable(false)
        val standardBottomSheetBehavior =
            BottomSheetBehavior.from(farFromYouBinding.root.parent as View)
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // Set Dragging off
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetDialog.show()

        // Set data and apply click events
        farFromYouBinding.apply {
            closeBottomSheetBtn.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            pickUpFromHereBtn.setOnClickListener {
                // TODO confirm location from here api call
                //onLocationSelected.pickUpLocationSelected(location)
                pickUpLocationSelected(location)
                bottomSheetDialog.dismiss()
            }
            chooseAnotherShack.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            farFromYouLocationTitle.text =
                getString(R.string.far_from_you_location_title, location.name)
            farFromYouLocationMessage.text =
                HtmlCompat.fromHtml(
                    getString(
                        R.string.far_from_you_location_message,
                        location.streetAddress
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        }
    }

    private fun setOnClickEvents(eventType: Int, bottomSheetDialog: BottomSheetDialog) {

        when (eventType) {
            LETS_TRY_PICK_UP_EVENT -> {
                // got to pickup tab
                //tabChangeListener.onTabChange()
            }
        }
        // clear search input text
        binding.pickupLocationSearchEditText.text?.clear()
        // scroll to first position
        binding.recyclerView.smoothScrollToPosition(0)
        bottomSheetDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun List<Region>.toDisplayRegionItems(): List<DisplayRegionOrLocation> {
        return map {
            DisplayRegionOrLocation(
                name = it.name ?: "No name",
                shackCount = it.count,
                regionId = it.id
            )
        }
    }

    private fun List<Prediction>.toDisplayLocationItems(): List<DisplayRegionOrLocation> {
        return map {
            DisplayRegionOrLocation(
                name = it.structuredFormatting.firstOrNull()?.mainText ?: "No name",
                locationAddress = it.structuredFormatting.firstOrNull()?.secondaryText ?: "",
                placeId = it.place_id
            )
        }


    }

    interface TabChangeListener {
        fun onTabChange()
    }
}
