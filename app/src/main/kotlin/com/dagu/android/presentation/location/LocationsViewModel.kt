package com.dagu.android.presentation.location

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dagu.android.data.di.module.MainDispatcher
import com.dagu.android.data.location.Location
import com.dagu.android.data.location.LocationRepository
import com.dagu.android.data.location.Prediction
import com.dagu.android.data.location.Region
import com.dagu.android.data.repository.Result
import com.dagu.android.data.repository.UIResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect

@FlowPreview
@ExperimentalCoroutinesApi
class LocationsViewModel @ViewModelInject constructor(
    private val locationRepository: LocationRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _regions = MutableLiveData<UIResult<List<Region>>>()
    val regions: LiveData<UIResult<List<Region>>> = _regions

    private val _locations = MutableLiveData<UIResult<List<Location>>>()
    val locations: LiveData<UIResult<List<Location>>> = _locations

    private val _placePredictions = MutableLiveData<UIResult<List<Prediction>>>()
    val placePredictions: LiveData<UIResult<List<Prediction>>> = _placePredictions

    private val _nearbyLocations = MutableLiveData<UIResult<List<Location>>>()
    val nearbyLocations: LiveData<UIResult<List<Location>>> = _nearbyLocations

    private var _lastPlaceId: String? = null
    val lastPlaceId: String?
        get() = _lastPlaceId

    private val _selectedLocation = MutableLiveData<Location>()
    val selectedLocation: LiveData<Location> = _selectedLocation

    private val _selectedRegionOrLocation = MutableLiveData<DisplayRegionOrLocation>()
    val selectedRegionOrLocation: LiveData<DisplayRegionOrLocation> = _selectedRegionOrLocation

    private val _searchQuery = MutableLiveData<String>()
    val lastSearchQuery: LiveData<String> = _searchQuery

    private val _deliverSearchQuery = MutableLiveData<String>()
    val lastDeliverySearchQuery: LiveData<String> = _deliverSearchQuery

    // Store last coroutine so we can cancel it in favor a new one.
    // This is basically to avoid clashing or redundant coroutines.
    private var lastJob: Job? = null


    private var _searchTermsToHighLight: String = ""
    val searchTermsToHighLight: String
        get() = _searchTermsToHighLight

    fun selectedLocation(location: Location) {
        _selectedLocation.value = location
    }

    fun selectedRegionOrLocation(
        displayRegionOrLocation: DisplayRegionOrLocation,
        searchQuery: String
    ) {
        _selectedRegionOrLocation.value = displayRegionOrLocation
        _searchQuery.value = searchQuery
    }

    fun updateSearchQuery(searchQuery: String) {
        _searchQuery.postValue(searchQuery)
    }

    fun updateDeliverSearchQuery(searchQuery: String) {
        _deliverSearchQuery.postValue(searchQuery)
    }

    fun getAllRegions() {
        lastJob?.cancel()
        lastJob = viewModelScope.launch(dispatcher) {
            locationRepository.getRegions().catch { exception ->
                _regions.value =
                    UIResult.Error("Error fetching regions: " + exception.message)
            }.collect { result ->
                when (result) {
                    is Result.Success -> {
                        val list: List<Region> = result.data.regions
                        _regions.value = UIResult.Success(list)
                    }
                    is Result.Loading -> {
                        _regions.value = UIResult.Loading(result.loading)
                    }
                    is Result.Error -> {
                        _regions.value =
                            UIResult.Error("Error fetching regions: " + result.exception.message)
                    }
                }
            }
        }
    }

    fun getAllLocations() {
        getLocations()
    }

    fun getNearbyLocations(lat: Double, lng: Double) {
        getLocations(lat, lng)
    }

    fun getNearbyLocations(placeId: String) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch(dispatcher) {
            _lastPlaceId = placeId
            locationRepository.getLocations(placeId)
                .catch { exception ->
                    _nearbyLocations.value =
                        UIResult.Error("Error fetching nearby locations: " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val list: List<Location> = result.data
                            _nearbyLocations.value = UIResult.Success(list)
                        }
                        is Result.Loading -> {
                            _nearbyLocations.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _nearbyLocations.value =
                                UIResult.Error("Error fetching nearby locations: " + result.exception.message)
                        }
                    }
                }
        }
    }

    fun getRegionLocations(regionId: String) {
        //TODO update location from the regionID enpoint when it is created
    }

    private fun getLocations(lat: Double? = null, lng: Double? = null) {
        lastJob?.cancel()
        lastJob = viewModelScope.launch(dispatcher) {
            locationRepository.getLocations(lat, lng)
                .catch { exception ->
                    _locations.value =
                        UIResult.Error("Error fetching locations: " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val list: List<Location> = result.data
                            _locations.value = UIResult.Success(list)
                        }
                        is Result.Loading -> {
                            _locations.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _locations.value =
                                UIResult.Error("Error fetching locations: " + result.exception.message)
                        }
                    }
                }
        }
    }

    fun searchLocations(searchTerms: String) {
        _searchTermsToHighLight = searchTerms
        lastJob?.cancel()
        lastJob = viewModelScope.launch(dispatcher) {
            locationRepository.searchLocation(searchTerms)
                .catch { exception ->
                    _placePredictions.value =
                        UIResult.Error("Error searching locations: " + exception.message)
                }.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            val list: List<Prediction> =
                                result.data.result.first().predictionsContainer.predictions
                            _placePredictions.value = UIResult.Success(list)
                        }
                        is Result.Loading -> {
                            _placePredictions.value = UIResult.Loading(result.loading)
                        }
                        is Result.Error -> {
                            _placePredictions.value =
                                UIResult.Error("Error searching locations: " + result.exception.message)
                        }
                    }
                }
        }
    }
}
