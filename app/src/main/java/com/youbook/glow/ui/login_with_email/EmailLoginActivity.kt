package com.youbook.glow.ui.login_with_email

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar
import com.android.fade.utils.Utils.toast
import com.android.fade.utils.Utils.visible
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityEmailLoginBinding
import com.youbook.glow.ui.code_verify.CodeVerifyActivity
import com.youbook.glow.utils.Constants
import gun0912.tedimagepicker.util.ToastUtil
import kotlinx.coroutines.launch

class EmailLoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityEmailLoginBinding
    private lateinit var viewModel: EmailLoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            EmailLoginViewModelFactory(EmailLoginRepository(MyApi.getInstance()))
        ).get(EmailLoginViewModel::class.java)

        setClickListeners()

        viewModel.userLoginResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    if (it.value.success!!) {
                        if (it.value.result != null) {
                            val intent = Intent(this, CodeVerifyActivity::class.java)
                            intent.putExtra(
                                Constants.EMAIL_ID,
                                binding.edtEmailAddress.text.toString()
                            )
                            intent.putExtra(Constants.OTP, "${it.value.result.otp}")
                            intent.putExtra(Constants.IS_EMAIL_LOGIN, true)
                            startActivity(intent)

                            ToastUtil.showToast("Please check your email for OTP")
//                            ToastUtil.showToast("Your OTP is ${it.value.result.otp}")
                        }
                    } else {
                        ToastUtil.showToast(it.value.message!!)
                    }
                }
                is Resource.Failure -> {
                    binding.progressBar.visible(false)
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.relGetStarted.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.relGetStarted -> goToVerifyOtpScreen()
        }
    }

    private fun goToVerifyOtpScreen() {
        if (isValid()) {
            binding.progressBar.visible(true)
            var email = binding.edtEmailAddress.text.toString().trim()
            viewModel.viewModelScope.launch {
                viewModel.loginUser(email)
            }
        }
    }

    private fun isValid(): Boolean {
        return when {
            TextUtils.isEmpty(binding.edtEmailAddress.text.toString().trim()) -> {
                this.toast(getString(R.string.err_empty_phone_number))
                false
            }
            !Utils.isEmailValid(binding.edtEmailAddress.text.toString().trim()) -> {
                binding.root.snackbar("Please enter valid email id")
                false
            }
            else -> {
                true
            }
        }
    }
}