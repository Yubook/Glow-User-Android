package com.youbook.glow.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.notification.NotificationListRepository
import com.android.fade.ui.notification.NotificationListViewModel

class NotificationListViewModelFactory(private val paymentDetailsRepository: NotificationListRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationListViewModel(paymentDetailsRepository) as T
    }
}