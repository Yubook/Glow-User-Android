package com.youbook.glow.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.fade.ui.review.ReviewListViewModel
import com.android.fade.ui.review.ReviewRepository

class ReviewModelFactory(private val reviewRepository: ReviewRepository) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReviewListViewModel(reviewRepository) as T
    }

}
