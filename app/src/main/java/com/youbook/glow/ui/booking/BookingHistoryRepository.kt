package com.android.fade.ui.booking

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class BookingHistoryRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun getUserBookings(
    ) = safeApiCall {
        api.getUserBookings()
    }
}