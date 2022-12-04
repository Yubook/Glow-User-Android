package com.android.fade.ui.terms_privacy

import com.android.fade.network.MyApi
import com.android.fade.repository.BaseRepository

class TermsPrivacyRepository(private val api: MyApi) : BaseRepository() {

    // 3 = User
    suspend fun getTermsPolicy(
    ) = safeApiCall {
        api.getTermsPolicy("3")
    }

}