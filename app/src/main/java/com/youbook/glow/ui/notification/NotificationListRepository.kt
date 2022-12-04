package com.android.fade.ui.notification

import com.android.fade.network.MyApi
import com.android.fade.network.MyStripeApi
import com.android.fade.repository.BaseRepository
import java.util.HashMap

class NotificationListRepository(private val api: MyApi) : BaseRepository() {

    suspend fun getNotification(
    ) = safeApiCall {
        api.getNotification()
    }

    suspend fun readNotification(
    ) = safeApiCall {
        api.readNotification()
    }

}