package com.youbook.glow.ui.code_verify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.code_verify.CodeVerifyRepository
import com.android.fade.ui.code_verify.CodeVerifyViewModel

class CodeVerifyViewModelFactory(private val profileRepository: CodeVerifyRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CodeVerifyViewModel(profileRepository) as T
    }
}
