package com.android.fade.ui.get_started
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityGetStartedBinding
import com.android.fade.ui.terms_privacy.TermsPrivacyActivity
import com.youbook.glow.ui.login_with_email.EmailLoginActivity

class GetStartedActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityGetStartedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetStartedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setClickListeners()
    }

    private fun setClickListeners() {
        binding.relGetStarted.setOnClickListener(this)
        binding.tvTerms.setOnClickListener(this)
        binding.tvPrivacy.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.relGetStarted -> goToLoginScreen()
            R.id.tvPrivacy -> goToPrivacyPolicy("Privacy")
            R.id.tvTerms -> goToPrivacyPolicy("Terms")
        }
    }

    private fun goToPrivacyPolicy(type: String) {
        val intent = Intent(this, TermsPrivacyActivity::class.java)
        intent.putExtra("Type", type)
        startActivity(intent)
    }

    private fun goToLoginScreen() {
        finish()
        startActivity(Intent(this, EmailLoginActivity::class.java))
    }
}