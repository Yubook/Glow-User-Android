package com.android.fade.ui.barber_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.CommonResponse
import com.android.fade.network.Resource
import com.android.fade.ui.appointment.DriverSlotsResponse
import com.android.fade.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class BarberDetailsViewModel constructor(private val repository: BarberDetailsRepository) : BaseViewModel(repository){

    private val _driverSlotsResponse: MutableLiveData<Resource<BarberAvailableSlotsResponse>> = MutableLiveData()
    val driverSlotsResponse: LiveData<Resource<BarberAvailableSlotsResponse>>
        get() = _driverSlotsResponse

    private val _rescheduleOrderResponse: MutableLiveData<Resource<CommonResponse>> = MutableLiveData()
    val rescheduleOrderResponse: LiveData<Resource<CommonResponse>>
        get() = _rescheduleOrderResponse

    private val _barberDetailsResponse: MutableLiveData<Resource<BarberDetailsResponseModel>> = MutableLiveData()
    val barberDetailsResponse: LiveData<Resource<BarberDetailsResponseModel>>
        get() = _barberDetailsResponse

    private val _makeBarberFav: MutableLiveData<Resource<ISFavouriteResponseModel>> = MutableLiveData()
    val makeBarberFavUnFav: LiveData<Resource<ISFavouriteResponseModel>>
        get() = _makeBarberFav


    suspend fun getDriverSlots(
        driver_id: String?,
        date: String?,
        total_time: String?,
    ) = viewModelScope.launch {
        _driverSlotsResponse.value = Resource.Loading
        _driverSlotsResponse.value = repository.getDriverSlots(driver_id, date, total_time)
    }

    suspend fun rescheduleOrder(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _rescheduleOrderResponse.value = Resource.Loading
        _rescheduleOrderResponse.value = repository.rescheduleOrder(params)
    }

    suspend fun getBarberDetails(
        barber_id: String?,
    ) = viewModelScope.launch {
        _barberDetailsResponse.value = Resource.Loading
        _barberDetailsResponse.value = repository.getBarberDetails(barber_id)
    }

    suspend fun makeBarberFavUnFav(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _makeBarberFav.value = Resource.Loading
        _makeBarberFav.value = repository.makeBarberFavUnFav(params)
    }
}
