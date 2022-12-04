package com.android.fade.ui.code_verify

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository
import okhttp3.MultipartBody

class CodeVerifyRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun login(
        mobile: String
    ) = safeApiCall {
        api.login(mobile)
    }

    suspend fun loginUserOtpVerify(
        email: String,
        otp: Int
    ) = safeApiCall {
        api.loginUserOtpVerify(email,otp)
    }

    /*suspend fun updateProfileWithoutPhoto(
        uid: String,
        params: Map<String,String>
    ) = safeApiCall {
        api.updateProfileWithoutPhoto1(uid,params)
    }

    suspend fun updateContactDetail(
        uid: String,
        mobile_no: String,
        address:String
    ) = safeApiCall {
        api.updateContactDetail(uid,mobile_no,address)
    }*/

}
