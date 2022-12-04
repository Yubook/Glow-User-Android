package com.android.fade.ui.payment_history

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository
import okhttp3.MultipartBody

class PaymentHistoryRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun getUserPaymentHistory(
    ) = safeApiCall {
        api.getUserPaymentHistory()
    }
}
