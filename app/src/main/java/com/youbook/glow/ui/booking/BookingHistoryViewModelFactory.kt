package com.youbook.glow.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.booking.BookingHistoryRepository
import com.android.fade.ui.booking.BookingHistoryViewModel

class BookingHistoryViewModelFactory(private val bookingHistoryRepository: BookingHistoryRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookingHistoryViewModel(bookingHistoryRepository) as T
    }

}