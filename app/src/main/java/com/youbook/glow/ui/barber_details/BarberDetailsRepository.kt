package com.android.fade.ui.barber_details

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository
import retrofit2.http.Query

class BarberDetailsRepository constructor(private val api : MyApi) : BaseRepository(){

    suspend fun getDriverSlots(
        driver_id: String?,
        date: String?,
        total_time: String?
    ) = safeApiCall {
        api.getDriverSlots(driver_id,date, total_time)
    }

    suspend fun rescheduleOrder(
        params: Map<String, String>
    ) = safeApiCall {
        api.rescheduleOrder(params)
    }

    suspend fun getBarberDetails(
        barber_id:String?
    ) = safeApiCall {
        api.getBarberDetails(barber_id)
    }

    suspend fun makeBarberFavUnFav(
        params: Map<String,String>
    ) = safeApiCall {
        api.makeBarberFavUnFav(params)
    }
}