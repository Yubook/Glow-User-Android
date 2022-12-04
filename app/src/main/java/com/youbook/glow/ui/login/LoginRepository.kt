package com.youbook.glow.ui.login

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class LoginRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun getCountryCode(
    ) = safeApiCall {
        api.getCountryCode()
    }

}