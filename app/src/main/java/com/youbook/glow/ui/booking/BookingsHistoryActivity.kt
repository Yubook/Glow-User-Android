package com.android.fade.ui.booking

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.youbook.glow.R
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.fragment.booking_list.BookingListAdapter
import com.android.fade.ui.fragment.booking_list.DataItem
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.hide
import com.android.fade.utils.Utils.visible
import com.youbook.glow.databinding.ActivityBookingsBinding
import com.youbook.glow.ui.booking.BookingHistoryViewModelFactory
import kotlinx.coroutines.launch

class BookingsHistoryActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityBookingsBinding
    private lateinit var viewModel: BookingHistoryViewModel
    private var user_id: String? = null
    private val cancelledOrderItem = ArrayList<DataItem>()
    private val completedOrderItem = ArrayList<DataItem>()
    private var allOrderItem = ArrayList<DataItem>()
    private lateinit var adapter: BookingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            BookingHistoryViewModelFactory(
                BookingHistoryRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(this, Constants.API_TOKEN)!!
                    )
                )
            )
        ).get(BookingHistoryViewModel::class.java)

        setClickListener()

        user_id = Prefrences.getPreferences(this, Constants.USER_ID)
        binding.bookingRecyclerview.layoutManager = LinearLayoutManager(this)
        adapter = BookingListAdapter(this)
        binding.bookingRecyclerview.adapter = adapter

        viewModel.userOrderResponse.observe(this, Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    if (it.value.success!!) {
                        if (it.value.result != null) {
                            cancelledOrderItem.clear()
                            completedOrderItem.clear()

                            allOrderItem = concatenate(it.value.result.previous!!.data!!,
                                it.value.result.next!!.data!!,it.value.result.today!!.data!!) as ArrayList<DataItem>

                            cancelledOrderItem.addAll(allOrderItem.filter {
                                    orderItem ->
                                orderItem.isOrderComplete!!.toString() == "2"
                            })

                            completedOrderItem.addAll(allOrderItem.filter {
                                    orderItem ->
                                orderItem.isOrderComplete!!.toString() == "1"
                            })

                            adapter.updateList(cancelledOrderItem, Constants.ORDER_TYPE_PREVIOUS)
                        }
                        if (adapter.getList().isNullOrEmpty()) {
                            binding.tvNoData.visibility = View.VISIBLE
                        } else {
                            binding.tvNoData.visibility = View.GONE
                        }
                    } else {
                        Toast.makeText(this, it.value.message!!, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                    getUserOrders()
                }
            }
        })
    }

    private fun <T> concatenate(vararg lists: List<T>): List<T> {
        return listOf(*lists).flatten()
    }

    override fun onResume() {
        super.onResume()
        getUserOrders()
        binding.rbCancelled.isChecked = true
        binding.rbCompleted.isChecked = false
    }

    private fun getUserOrders() {
        viewModel.viewModelScope.launch {
            viewModel.getUserOrder()
        }
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.rbCancelled.setOnClickListener(this)
        binding.rbCompleted.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.rbCompleted ->{
                binding.rbCancelled.isChecked = false
                loadItemsToAdapter(completedOrderItem)
            }
            R.id.rbCancelled ->{
                binding.rbCompleted.isChecked = false
                loadItemsToAdapter(cancelledOrderItem)
            }
        }
    }

    private fun loadItemsToAdapter(orderItem: java.util.ArrayList<DataItem>) {
        adapter.updateList(orderItem, Constants.ORDER_TYPE_PREVIOUS)
        if (adapter.getList().isNullOrEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }
    }
}

