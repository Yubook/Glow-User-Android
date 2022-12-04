package com.android.fade.ui.terms_privacy

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityTermsPrivacyBinding
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.visible
import com.youbook.glow.ui.terms_privacy.TermsPrivacyViewModelFactory
import kotlinx.coroutines.launch

class TermsPrivacyActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityTermsPrivacyBinding
    private lateinit var viewModel: TermsPrivacyViewModel
    private var termsConditionData : String? = ""
    private var type : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTermsPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            TermsPrivacyViewModelFactory(
                TermsPrivacyRepository(
                    MyApi.getInstance()
                )
            )
        ).get(TermsPrivacyViewModel::class.java)

        setListener()
        type = intent.getStringExtra("Type")
        if (type.equals("Privacy")){
            binding.tvToolbarTitle.text = "Privacy Policy"
        } else {
            binding.tvToolbarTitle.text = "Terms & Condition"
        }

        getTermsPrivacyData()

        viewModel.termsPrivacyResponse.observe(this, Observer {
            binding.progressBar.visible(it is Resource.Loading)

            when (it) {
                is Resource.Success -> {
                    if (it.value.success!!) {

                        if (it.value.result != null){

                            if (it.value.result.isNotEmpty()){

                                for (data in it.value.result){
                                    if (data!!.selection!! == type){
                                        termsConditionData = data.description

                                        loadWebViewData(termsConditionData)
                                    }
                                }
                            }
                        }
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {}
            }
        })

    }

    private fun setListener() {
        binding.ivBackButton.setOnClickListener(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebViewData(data: String?) {
        binding.webView.settings.javaScriptEnabled = true;
        binding.webView.loadData(data!!, "text/html", "UTF-8")

    }

    private fun getTermsPrivacyData() {
        viewModel.viewModelScope.launch {
            viewModel.getTermsPolicy()
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBackButton -> finish()
        }

    }
}