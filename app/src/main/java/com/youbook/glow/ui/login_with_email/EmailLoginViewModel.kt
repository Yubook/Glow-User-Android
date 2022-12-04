package com.youbook.glow.ui.login_with_email

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.fade.network.Resource
import kotlinx.coroutines.launch

class EmailLoginViewModel constructor(private val repository: EmailLoginRepository): ViewModel() {

    private val _userLoginResponse: MutableLiveData<Resource<UserLoginResponse>> = MutableLiveData()
    val userLoginResponse: LiveData<Resource<UserLoginResponse>>
        get() = _userLoginResponse

    suspend fun loginUser(
        email: String
    ) = viewModelScope.launch {
        _userLoginResponse.value = Resource.Loading
        _userLoginResponse.value = repository.loginUser(email)
    }



}