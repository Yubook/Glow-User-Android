package com.android.fade.ui.verify_phone_num

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityVerifyPhoneNumberBinding
import com.youbook.glow.ui.code_verify.CodeVerifyActivity

class VerifyPhoneNumberActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityVerifyPhoneNumberBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone_number)
        binding = ActivityVerifyPhoneNumberBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvRegisterOrLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvRegisterOrLogin -> goToCodeVerifyScreen()
            R.id.ivBackButton -> finish()
        }
    }

    private fun goToCodeVerifyScreen() {
        startActivity(Intent(this, CodeVerifyActivity::class.java))

    }
}