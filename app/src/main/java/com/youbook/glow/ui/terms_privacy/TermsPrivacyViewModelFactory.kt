package com.youbook.glow.ui.terms_privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.terms_privacy.TermsPrivacyRepository
import com.android.fade.ui.terms_privacy.TermsPrivacyViewModel

class TermsPrivacyViewModelFactory(private val repository: TermsPrivacyRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TermsPrivacyViewModel(repository) as T
    }
}