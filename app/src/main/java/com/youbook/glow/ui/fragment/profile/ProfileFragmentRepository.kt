package com.android.fade.ui.fragment.profile

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class ProfileFragmentRepository constructor(private val api : MyApi): BaseRepository() {

    suspend fun getDashBoardData(
        params: Map<String,String>
    ) = safeApiCall {
        api.getDashBoardData(params)
    }

    suspend fun logoutUser(
    ) = safeApiCall {
        api.logout()
    }
}

