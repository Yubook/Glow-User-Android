package com.youbook.glow.ui.fragment.booking_list

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class BookingListRepository constructor(private val api : MyApi): BaseRepository(){

    suspend fun getUserBookings(
    ) = safeApiCall {
        api.getUserBookings()
    }

    suspend fun orderCancelByUser(
        params:Map<String, String>
    ) = safeApiCall {
        api.orderCancelByUser(params)
    }

}
