package com.dagu.android.presentation.checkout

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.dagu.android.data.di.module.MainDispatcher
import com.dagu.android.data.location.LocationRepository
import com.dagu.android.data.order.OrderRepository
import com.dagu.android.data.order.model.Tray
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@ExperimentalCoroutinesApi
class CheckoutViewModel @ViewModelInject constructor(
    private val orderRepository: OrderRepository,
    private val locationRepository: LocationRepository,
    @MainDispatcher private val dispatcher: CoroutineDispatcher
) : ViewModel() {


    fun getCurrentTray(): StateFlow<Tray> {
        return orderRepository.tray
    }

}