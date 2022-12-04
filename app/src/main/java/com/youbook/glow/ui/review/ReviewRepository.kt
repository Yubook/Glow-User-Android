package com.android.fade.ui.review

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository
import okhttp3.MultipartBody

class ReviewRepository constructor(private val api : MyApi) : BaseRepository() {

    suspend fun getReview(
    ) = safeApiCall {
        api.getReview()
    }
}
