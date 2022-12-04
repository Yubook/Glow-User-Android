package com.youbook.glow.ui.new_add_review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.new_add_review.NewAddReviewRepository
import com.android.fade.ui.new_add_review.NewAddReviewViewModel

class NewAddReviewViewModelFactory(private val addReviewRepository: NewAddReviewRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NewAddReviewViewModel(addReviewRepository) as T
    }

}