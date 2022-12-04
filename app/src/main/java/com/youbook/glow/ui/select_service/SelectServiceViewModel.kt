package com.android.fade.ui.select_service

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class SelectServiceViewModel constructor(private val repository: SelectServiceRepository) :
    BaseViewModel(repository) {

    private val _serviceResponse: MutableLiveData<Resource<ServicesResponse>> = MutableLiveData()
    val serviceResponse: LiveData<Resource<ServicesResponse>>
        get() = _serviceResponse

    suspend fun getServices() = viewModelScope.launch {
        _serviceResponse.value = Resource.Loading
        _serviceResponse.value = repository.getServices()
    }
}