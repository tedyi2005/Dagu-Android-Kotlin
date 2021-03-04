package com.dagu.android.presentation.location

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.dagu.android.R
import com.dagu.android.data.location.Location
import com.dagu.android.databinding.FarFromYouConfirmationLayoutBinding
import com.dagu.android.databinding.FragmentLocationsBinding
import com.dagu.android.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class LocationListFragment : Fragment() {
    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: LocationAdapter
    private lateinit var onLocationSelected: OnLocationSelected


    companion object {
        fun newInstance(
            locations: List<Location>,
            onLocationSelected: OnLocationSelected
        ): LocationListFragment {
            val args = Bundle()
            args.putSerializable(Constants.LOCATIONS, locations as Serializable?)
            val fragment = LocationListFragment()
            fragment.arguments = args
            fragment.onLocationSelected = onLocationSelected
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)

        val locations = arguments?.getSerializable(Constants.LOCATIONS) as List<Location>
        initRecyclerView(locations)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initRecyclerView(locations: List<Location>) {
        binding.recyclerView.also {
            it.layoutManager = LinearLayoutManager(requireContext())
            it.itemAnimator = DefaultItemAnimator()
            it.setHasFixedSize(true)
            adapter = LocationAdapter(requireContext(), locations)
            it.adapter = adapter
        }
        adapter.onItemClick = { location ->
            // Static far from you location items for testing purposes
            if (
                location.name.equals("Boca Raton") ||
                location.name.equals("Buckhead") ||
                location.name.equals("Demo Vendor")
            )
                setUpFarFromYouNotificationBottomSheet(location)
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
                onLocationSelected.pickUpLocationSelected(location)
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
}
