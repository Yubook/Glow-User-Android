package com.android.fade.ui.payment_history

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityPaymentHistoryBinding
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.visible
import com.youbook.glow.ui.payment_history.PaymentHistoryModelFactory
import kotlinx.coroutines.launch

class PaymentHistoryActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPaymentHistoryBinding
    private lateinit var viewModel: PaymentHistoryViewModel
    private lateinit var adapter: PaymentHistoryAdapter
    private var userId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            PaymentHistoryModelFactory(
                PaymentHistoryRepository(
                    MyApi.getInstanceToken(Prefrences.getPreferences(this, Constants.API_TOKEN)!!)
                )
            )
        ).get(PaymentHistoryViewModel::class.java)

        setClickListener()
        binding.paymentHistoryRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = PaymentHistoryAdapter(this)
        binding.paymentHistoryRecyclerview.adapter = adapter

        userId = Prefrences.getPreferences(this, Constants.USER_ID)!!.toString()
        getUserOrders()

        viewModel.userPaymentHistoryResponse.observe(this, Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
//                    binding.progressBar.hide()
                    if (it.value.success!!) {
                        binding.tvTotalExpense.text = this.getText(R.string.pound_sign).toString().plus(" ")
                            .plus(it.value.result!!.totalExpense.toString())
                        if (it.value.result.order != null) {
                            adapter.updateList(it.value.result.order.data!!)
                        }

                        if (adapter.getList().isEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, it.value.message!!, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
//                    binding.progressBar.hide()
                    getUserOrders()
                }
            }
        })

    }

    private fun getUserOrders() {
        viewModel.viewModelScope.launch {
            viewModel.getUserPaymentHistory()
        }
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
        }
    }
}