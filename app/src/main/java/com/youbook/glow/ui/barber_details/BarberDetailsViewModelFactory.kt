package com.youbook.glow.ui.barber_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.barber_details.BarberDetailsRepository
import com.android.fade.ui.barber_details.BarberDetailsViewModel

class BarberDetailsViewModelFactory(private val repository: BarberDetailsRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BarberDetailsViewModel(repository) as T
    }
}
