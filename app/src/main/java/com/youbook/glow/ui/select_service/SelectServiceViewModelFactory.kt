package com.youbook.glow.ui.select_service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.select_service.SelectServiceRepository
import com.android.fade.ui.select_service.SelectServiceViewModel

class SelectServiceViewModelFactory(private val selectServiceRepository: SelectServiceRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SelectServiceViewModel(selectServiceRepository) as T
    }
}
