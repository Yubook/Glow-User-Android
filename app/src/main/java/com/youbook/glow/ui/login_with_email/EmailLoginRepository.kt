package com.youbook.glow.ui.login_with_email

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class EmailLoginRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun getCountryCode(
    ) = safeApiCall {
        api.getCountryCode()
    }

    suspend fun loginUser(
        email: String
    ) = safeApiCall {
        api.loginUser(email)
    }
}