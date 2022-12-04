package com.android.fade.ui.new_add_review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.CommonResponse
import com.android.fade.network.Resource
import com.android.fade.ui.barber_details.ISFavouriteResponseModel
import com.android.fade.ui.base.BaseViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class NewAddReviewViewModel constructor(private val repository: NewAddReviewRepository) :
    BaseViewModel(repository) {

    private val _makeBarberFav: MutableLiveData<Resource<ISFavouriteResponseModel>> = MutableLiveData()
    val makeBarberFavUnFav: LiveData<Resource<ISFavouriteResponseModel>>
        get() = _makeBarberFav

    suspend fun makeBarberFavUnFav(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _makeBarberFav.value = Resource.Loading
        _makeBarberFav.value = repository.makeBarberFavUnFav(params)
    }

    private val _addReviewResponse: MutableLiveData<Resource<CommonResponse>> = MutableLiveData()
    val addReviewResponse: LiveData<Resource<CommonResponse>>
        get() = _addReviewResponse

    suspend fun addReview(
        files: List<MultipartBody.Part>,
        params: Map<String,String>
    ) = viewModelScope.launch {
        _addReviewResponse.value = Resource.Loading
        _addReviewResponse.value = repository.addReview(files, params)
    }

    private val _driverLocation: MutableLiveData<Resource<DriverLatestLocationResponseModel>> = MutableLiveData()
    val driverLatestLocation: LiveData<Resource<DriverLatestLocationResponseModel>>
        get() = _driverLocation

    suspend fun driverLatestLocation(
        orderId: String
    ) = viewModelScope.launch {
        _driverLocation.value = Resource.Loading
        _driverLocation.value = repository.getDriverLatestLocation(orderId)
    }
}