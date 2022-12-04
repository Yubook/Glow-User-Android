package com.youbook.glow.ui.add_review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.add_review.AddReviewRepository
import com.android.fade.ui.add_review.AddReviewViewModel

class AddReviewViewModelFactory(private val addReviewRepository: AddReviewRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddReviewViewModel(addReviewRepository) as T
    }

}