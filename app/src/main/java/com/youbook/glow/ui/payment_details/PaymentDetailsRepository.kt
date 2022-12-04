package com.android.fade.ui.payment_details

import com.android.fade.network.MyApi
import com.android.fade.network.MyStripeApi
import com.android.fade.repository.BaseRepository
import java.util.HashMap

class PaymentDetailsRepository(private val api: MyApi, private val stripeApi: MyStripeApi) : BaseRepository() {

    suspend fun getPaymentToken(
    ) = safeApiCall {
        api.getPaymentToken()
    }

    suspend fun bookUserSlotByWallet(
        params: Map<String, String>
    ) = safeApiCall {
        api.bookUserSlotByWallet(params)
    }

    suspend fun bookAppointment(
        params: Map<String, String>,
        slotIdsList : ArrayList<String>,
        serviceIds : ArrayList<String>,
    ) = safeApiCall {
        api.bookAppointment(params, slotIdsList, serviceIds)
    }

    suspend fun makePayment(
        params: Map<String, String>
    ) = safeApiCall {
        api.makePayment(params)
    }

    suspend fun getCardToken(
        params: Map<String, String>,
        authHeader: String
    ) = safeApiCall {
        stripeApi.getCardTokens(params, authHeader)
    }

}