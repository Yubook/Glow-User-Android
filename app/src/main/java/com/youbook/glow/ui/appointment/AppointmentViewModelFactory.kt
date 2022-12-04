package com.youbook.glow.ui.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.appointment.AppointmentViewModel
import com.android.fade.ui.appointment.AppointmentsRepository

class AppointmentViewModelFactory(private val appointmentsRepository: AppointmentsRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppointmentViewModel(appointmentsRepository) as T
    }
}
