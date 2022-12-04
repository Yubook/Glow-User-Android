package com.android.fade.ui.fragment.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class HomeViewModel(val repository: HomeRepository) : BaseViewModel(repository) {

    private val _getOfferResponse: MutableLiveData<Resource<HomeOfferResponse>> = MutableLiveData()
    val getOfferResponse: LiveData<Resource<HomeOfferResponse>>
        get() = _getOfferResponse

    private val _getDriversResponse: MutableLiveData<Resource<DriversResponse>> = MutableLiveData()
    val getDriversResponse: LiveData<Resource<DriversResponse>>
        get() = _getDriversResponse

   /* suspend fun getOffer(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _getOfferResponse.value = Resource.Loading
        _getOfferResponse.value = repository.getOffer(params)
    }*/

    suspend fun getNearestDriver(
    ) = viewModelScope.launch {
        _getDriversResponse.value = Resource.Loading
        _getDriversResponse.value = repository.getNearestDrivers()
    }

    suspend fun searchDriver(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _getDriversResponse.value = Resource.Loading
        _getDriversResponse.value = repository.searchDriver(params)
    }


}