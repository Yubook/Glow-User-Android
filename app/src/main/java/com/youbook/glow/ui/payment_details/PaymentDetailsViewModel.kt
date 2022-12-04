package com.android.fade.ui.payment_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.fade.network.CommonResponse
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseViewModel
import kotlinx.coroutines.launch

class PaymentDetailsViewModel constructor(private val repository: PaymentDetailsRepository) :
    BaseViewModel(repository) {

    private val _paymentTokenResponse: MutableLiveData<Resource<PaymentKeyResponse>> = MutableLiveData()
    val paymentTokenResponse: LiveData<Resource<PaymentKeyResponse>>
        get() = _paymentTokenResponse

    private val _bookUserSlotByWalletResponse: MutableLiveData<Resource<CommonResponse>> = MutableLiveData()
    val bookUserSlotByWalletResponse: LiveData<Resource<CommonResponse>>
        get() = _bookUserSlotByWalletResponse

    private val _bookAppointmentResponse: MutableLiveData<Resource<CommonResponse>> = MutableLiveData()
    val bookAppointmentResponse: LiveData<Resource<CommonResponse>>
        get() = _bookAppointmentResponse

    private val _cardTokenResponse: MutableLiveData<Resource<CardTokensResponse>> = MutableLiveData()
    val cardTokenResponse: LiveData<Resource<CardTokensResponse>>
        get() = _cardTokenResponse

    private val _makePaymentResponse: MutableLiveData<Resource<StripePaymentResponse>> = MutableLiveData()
    val makePaymentResponse: LiveData<Resource<StripePaymentResponse>>
        get() = _makePaymentResponse

    suspend fun getCardToken(
    ) = viewModelScope.launch {
        _paymentTokenResponse.value = Resource.Loading
        _paymentTokenResponse.value = repository.getPaymentToken()
    }

    suspend fun getCardTokens(
        params: Map<String, String>,
        authHeader: String
    ) = viewModelScope.launch {
        _cardTokenResponse.value = Resource.Loading
        _cardTokenResponse.value = repository.getCardToken(params,authHeader)
    }

    suspend fun bookUserSlotByWallet(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _bookUserSlotByWalletResponse.value = Resource.Loading
        _bookUserSlotByWalletResponse.value = repository.bookUserSlotByWallet(params)
    }

    suspend fun bookAppointment(
        params: Map<String, String>,
        slotIdsList : ArrayList<String>,
        serviceIds : ArrayList<String>
    ) = viewModelScope.launch {
        _bookAppointmentResponse.value = Resource.Loading
        _bookAppointmentResponse.value = repository.bookAppointment(params, slotIdsList, serviceIds)
    }

    suspend fun makePayment(
        params: Map<String, String>
    ) = viewModelScope.launch {
        _makePaymentResponse.value = Resource.Loading
        _makePaymentResponse.value = repository.makePayment(params)
    }
}