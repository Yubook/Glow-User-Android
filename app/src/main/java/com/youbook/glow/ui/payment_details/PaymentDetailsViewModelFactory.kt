package com.youbook.glow.ui.payment_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.payment_details.PaymentDetailsRepository
import com.android.fade.ui.payment_details.PaymentDetailsViewModel

class PaymentDetailsViewModelFactory(private val paymentDetailsRepository: PaymentDetailsRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PaymentDetailsViewModel(paymentDetailsRepository) as T
    }
}