package com.android.fade.ui.fragment.new_dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.CommonResponse
import com.android.fade.network.Resource
import com.android.fade.ui.barber_details.ISFavouriteResponseModel
import com.android.fade.ui.base.BaseViewModel
import com.android.fade.ui.fragment.dashboard.HomeOfferResponse
import com.android.fade.ui.fragment.dashboard.HomeRepository
import kotlinx.coroutines.launch

class NewDashboardViewModel(val repository: NewDashboardRepository) : BaseViewModel(repository)  {

    private val _getDashboardResponse: MutableLiveData<Resource<NewDashBoardResponseModel>> = MutableLiveData()
    val getDashboardResponse: LiveData<Resource<NewDashBoardResponseModel>>
        get() = _getDashboardResponse

    private val _makeBarberFav: MutableLiveData<Resource<ISFavouriteResponseModel>> = MutableLiveData()
    val makeBarberFavUnFav: LiveData<Resource<ISFavouriteResponseModel>>
        get() = _makeBarberFav

    private val _getServiceResponse: MutableLiveData<Resource<ServiceResponseModel>> = MutableLiveData()
    val getServiceResponse: LiveData<Resource<ServiceResponseModel>>
        get() = _getServiceResponse

    suspend fun getDashBoardData(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _getDashboardResponse.value = Resource.Loading
        _getDashboardResponse.value = repository.getDashBoardData(params)
    }

    suspend fun getDashBoardDataWithOtherLatLon(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _getDashboardResponse.value = Resource.Loading
        _getDashboardResponse.value = repository.getDashBoardDataWithOtherLatLon(params)
    }

    suspend fun makeBarberFavUnFav(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _makeBarberFav.value = Resource.Loading
        _makeBarberFav.value = repository.makeBarberFavUnFav(params)
    }

    suspend fun getServices(
    ) = viewModelScope.launch {
        _getServiceResponse.value = Resource.Loading
        _getServiceResponse.value = repository.getServices()
    }
}