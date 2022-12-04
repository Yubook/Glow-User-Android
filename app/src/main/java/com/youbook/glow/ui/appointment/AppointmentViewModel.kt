package com.android.fade.ui.appointment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.CommonResponse
import com.android.fade.network.Resource
import com.android.fade.ui.barber_details.BarberAvailableSlotsResponse
import com.android.fade.ui.base.BaseViewModel
import com.android.fade.ui.profile.ProfileRepository
import com.android.fade.ui.profile.ProfileResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AppointmentViewModel constructor(private val repository: AppointmentsRepository) : BaseViewModel(repository){

    private val _driverSlotsResponse: MutableLiveData<Resource<BarberAvailableSlotsResponse>> = MutableLiveData()
    val driverSlotsResponse: LiveData<Resource<BarberAvailableSlotsResponse>>
        get() = _driverSlotsResponse

    private val _rescheduleOrderResponse: MutableLiveData<Resource<CommonResponse>> = MutableLiveData()
    val rescheduleOrderResponse: LiveData<Resource<CommonResponse>>
        get() = _rescheduleOrderResponse

    suspend fun getDriverSlots(
        driver_id: String?,
        date: String?,
        total_time: String?
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
}
