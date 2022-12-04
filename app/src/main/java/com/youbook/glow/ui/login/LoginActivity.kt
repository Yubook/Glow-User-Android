package com.youbook.glow.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.login.*
import com.android.fade.utils.Prefrences
import com.youbook.glow.R
import com.youbook.glow.ui.code_verify.CodeVerifyActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils.toast
import com.youbook.glow.databinding.ActivityLoginBinding
import gun0912.tedimagepicker.util.ToastUtil
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    private lateinit var countrySpinnerAdapter: CountrySpinnerAdapter
    private var countryId: String? = null
    var countryList: ArrayList<ResultItem?> = ArrayList()
    var selectedCountry: String = "Select Country"
    var selectedCountryID: String = ""
    var isFirstTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setClickListeners()

        spinnerListener()


        viewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(LoginRepository(MyApi.getInstance()))
        ).get(LoginViewModel::class.java)

        getCountryCode()
        //Country response handler
        viewModel.countryCodeResponse.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    if (it.value.success!!) {
                        countryList.addAll(it.value.result!!)
                        countrySpinnerAdapter = CountrySpinnerAdapter(ToastUtil.context, countryList)
                        var indexOfFirst = 1
                        /*if (isFirstTime.equals("YES")) {
                            binding.spnCity.setSelection(0)
                            binding.spnCity.adapter = countrySpinnerAdapter
                        } else {
                            indexOfFirst = countryList.indexOfFirst { resultItem ->
                                resultItem!!.id.toString() == countryId
                            }
                            binding.spnCity.adapter = countrySpinnerAdapter
                            binding.spnCity.setSelection(indexOfFirst)
                        }*/

                        indexOfFirst = countryList.indexOfFirst { resultItem ->
                            resultItem!!.id.toString() == "232"
                        }
                        binding.spnCity.adapter = countrySpinnerAdapter
                        binding.spnCity.setSelection(indexOfFirst)

                    } else {

                    }
                }
                is Resource.Failure -> {
                    // Utils.handleApiError(binding.root, it)
                }
            }
        })
    }

    private fun setClickListeners() {
        binding.relGetStarted.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.relGetStarted -> goToVerifyPhoneNumScreen()
        }
    }

    private fun goToVerifyPhoneNumScreen() {
        if (isValid()) {
            val intent = Intent(this, CodeVerifyActivity::class.java)
            intent.putExtra(Constants.USER_MOBILE_NO, "" + binding.edtPhoneNumber.text)
            startActivity(intent)
        }

    }

    private fun isValid(): Boolean {
        return when {
            TextUtils.isEmpty(binding.edtPhoneNumber.text.toString()) -> {
                this.toast(getString(R.string.err_empty_phone_number))
                false
            }
            binding.edtPhoneNumber.text.toString().length < 10 -> {
                this.toast(getString(R.string.err_valid_phone_number))
                false
            }
            else -> {
                true
            }
        }
    }
    private fun getCountryCode() {
        viewModel.viewModelScope.launch {
            viewModel.countryCode()
        }
    }

    private fun spinnerListener() {
        binding.spnCity.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                println("Nothing Selected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedCountry = countryList[position]!!.phonecode!!
//                selectedCountryID = "${countryList[position]!!.id!!}"

                Prefrences.savePreferencesString(this@LoginActivity, Constants.SELECTED_COUNTRY_CODE, selectedCountry)
            }

        }
    }
}