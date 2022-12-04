package com.youbook.glow.ui.select_service

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivitySelectServiceBinding
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.payment_details.PaymentDetailsActivity
import com.android.fade.ui.select_service.*
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar
import com.android.fade.utils.Utils.visible
import kotlinx.coroutines.launch

class SelectServiceActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivitySelectServiceBinding
    private lateinit var viewModel: SelectServiceViewModel
    private lateinit var adapter: ServiceAdapter
    private var driverId: String? = null
    private var driverName: String? = null
    private var driverRating: String? = null
    private var driverTotalReview: String? = null
    private var driverImage: String? = null
    private var selectedDate: String? = null
    private var driverServices: String? = null
    private var booked_date: String? = null
    private var orderSlotId: String? = null
    private var orderLat: String? = null
    private var orderLon: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            SelectServiceViewModelFactory(
                SelectServiceRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(
                            this,
                            Constants.API_TOKEN
                        )!!
                    )
                )
            )
        ).get(SelectServiceViewModel::class.java)

        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ServiceAdapter(this)
        binding.serviceRecyclerView.adapter = adapter

        driverId = intent.getStringExtra(Constants.DRIVER_ID)
        driverName = intent.getStringExtra(Constants.DRIVER_NAME)
        driverImage = intent.getStringExtra(Constants.DRIVER_IMAGE)
        driverRating = intent.getStringExtra(Constants.DRIVER_RATING)
        driverTotalReview = intent.getStringExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT)
        driverServices = intent.getStringExtra(Constants.DRIVER_SERVICE)
        booked_date = intent.getStringExtra(Constants.BOOKED_DATE)
        orderSlotId = intent.getStringExtra(Constants.ORDER_SLOT_ID)
        orderLat = intent.getStringExtra(Constants.ORDER_LAT)
        orderLon = intent.getStringExtra(Constants.ORDER_LON)

        getServices()
        setClickListener()
        binding.tvConfirmBookings.isEnabled = false

        viewModel.serviceResponse.observe(this, Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    if (it.value.result != null) {
                        adapter.updateList(it.value.result as List<ResultItem>)
                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                    binding.tvConfirmBookings.isEnabled = it.value.result!!.isNotEmpty()
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    getServices()
                }
            }
        })
    }

    private fun getServices() {
        viewModel.viewModelScope.launch {
            viewModel.getServices()
        }
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvConfirmBookings.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.tvConfirmBookings -> goToPaymentScreen()
        }
    }

    private fun goToPaymentScreen() {
        val intent = Intent(this, PaymentDetailsActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, driverId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)
        intent.putExtra(Constants.DRIVER_RATING, driverRating)
        intent.putExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT, driverTotalReview)
        intent.putExtra(Constants.BOOKED_SERVICE, adapter.getServiceName())
        intent.putExtra(Constants.BOOKED_SERVICE_ID, adapter.getServiceId())
        intent.putExtra(Constants.DRIVER_SERVICE, driverServices)
        intent.putExtra(Constants.DRIVER_SERVICE_PRICE, adapter.getServicePrice())
        intent.putExtra(Constants.BOOKED_DATE, booked_date)
        intent.putExtra(Constants.ORDER_SLOT_ID, orderSlotId)
        intent.putExtra(Constants.ORDER_LAT, orderLat)
        intent.putExtra(Constants.ORDER_LON, orderLon)
        startActivity(intent)
    }
}