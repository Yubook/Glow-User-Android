package com.youbook.glow.ui.login_with_email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class EmailLoginViewModelFactory(private val repository: EmailLoginRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EmailLoginViewModel(repository) as T
    }
}