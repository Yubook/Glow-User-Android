package com.android.fade.ui.fragment.favourite_list

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class FavouriteRepository constructor(private val api : MyApi): BaseRepository() {

    suspend fun getFavouriteBarber(
    ) = safeApiCall {
        api.getFavouriteBarbers()
    }

    suspend fun makeBarberFavUnFav(
        params: Map<String,String>
    ) = safeApiCall {
        api.makeBarberFavUnFav(params)
    }

}