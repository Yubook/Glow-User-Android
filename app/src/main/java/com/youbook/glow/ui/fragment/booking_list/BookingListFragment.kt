package com.youbook.glow.ui.fragment.booking_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fade.extension.visible
import com.youbook.glow.R
import com.youbook.glow.databinding.BookingListFragmentBinding
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.fragment.booking_list.BookingListAdapter
import com.android.fade.ui.fragment.booking_list.BookingListViewModel
import com.android.fade.ui.fragment.booking_list.DataItem
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.hide
import kotlinx.coroutines.launch
import java.util.HashMap

class BookingListFragment :
    BaseFragment<BookingListViewModel, BookingListFragmentBinding, BookingListRepository>(),
    View.OnClickListener {
    private var userId: String? = null
    private lateinit var adapter: BookingListAdapter
    private var previousOrderItem = ArrayList<DataItem>()
    private var currentOrderItem = ArrayList<DataItem>()
    private var futureOrderItem = ArrayList<DataItem>()
    override fun getViewModel() = BookingListViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setClickListener()
        userId = Prefrences.getPreferences(requireContext(), Constants.USER_ID)
        binding.bookingRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = BookingListAdapter(requireContext()){ item, type ->

            if (type == "Cancel"){
                cancelOrder(item)
            }
        }
        binding.bookingRecyclerview.adapter = adapter

        viewModel.newOrderResponse.observe(requireActivity()) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    Log.d("TAG", "onViewCreated: ".plus(it.value))

                    if (it.value.success!!) {
                        if (it.value.result != null) {
                            previousOrderItem =
                                (it.value.result.previous!!.data as ArrayList<DataItem>?)!!
                            currentOrderItem =
                                (it.value.result.today!!.data as ArrayList<DataItem>?)!!
                            futureOrderItem =
                                (it.value.result.next!!.data as ArrayList<DataItem>?)!!

                            previousOrderItem = previousOrderItem.filter { orderItem ->
                                orderItem.isOrderComplete!!.toString() == "0"
                            } as ArrayList<DataItem>

                            currentOrderItem = currentOrderItem.filter { orderItem ->
                                orderItem.isOrderComplete!!.toString() == "0"
                            } as ArrayList<DataItem>

                            futureOrderItem = futureOrderItem.filter { orderItem ->
                                orderItem.isOrderComplete!!.toString() == "0"
                            } as ArrayList<DataItem>

                            adapter.updateList(previousOrderItem, Constants.ORDER_TYPE_PREVIOUS)
                        }
                        checkListEmpty()
                    } else {
                        Utils.showErrorDialog(requireContext(), it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                    getUserOrders()
                }
            }
        }

        viewModel.cancelOrderResponse.observe(requireActivity()) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    Log.d("TAG", "onViewCreated: ".plus(it.value))
                    if (it.value.success!!) {
                        Toast.makeText(
                            context,
                            "Order Cancellation Successfully Done!",
                            Toast.LENGTH_SHORT
                        ).show()
                        getUserOrders()
                    } else {
                        Utils.showErrorDialog(requireContext(), it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }

        }
    }

    private fun cancelOrder(item: DataItem) {
        val params = HashMap<String, String>()
        params["user_id"] = userId!!
        params["barber_id"] = item.barber!!.id.toString()
        params["order_id"] = item.id.toString()
        params["cancle_by"] = "user"

        viewModel.viewModelScope.launch {
            viewModel.orderCancelByUser(params)
        }

    }

    override fun onResume() {
        super.onResume()
        getUserOrders()
    }

    private fun getUserOrders() {
        binding.rbPrevious.isChecked = true
        binding.rbCurrent.isChecked = false
        binding.rbFuture.isChecked = false
        viewModel.viewModelScope.launch {
            viewModel.getUserBookings()
        }
    }

    private fun setClickListener() {
        binding.rbCurrent.setOnClickListener(this)
        binding.rbFuture.setOnClickListener(this)
        binding.rbPrevious.setOnClickListener(this)

    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = BookingListFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = BookingListRepository(
        MyApi.getInstanceToken(
            Prefrences.getPreferences(requireContext(), Constants.API_TOKEN)!!
        )
    )

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rbPrevious -> {
                binding.rbCurrent.isChecked = false
                binding.rbFuture.isChecked = false
                adapter.updateList(previousOrderItem, Constants.ORDER_TYPE_PREVIOUS)
                checkListEmpty()
            }

            R.id.rbCurrent -> {
                binding.rbPrevious.isChecked = false
                binding.rbFuture.isChecked = false
                adapter.updateList(currentOrderItem, Constants.ORDER_TYPE_CURRENT)
                checkListEmpty()
            }
            R.id.rbFuture -> {
                binding.rbPrevious.isChecked = false
                binding.rbCurrent.isChecked = false
                adapter.updateList(futureOrderItem, Constants.ORDER_TYPE_FUTURE)
                checkListEmpty()
            }
        }
    }

    private fun checkListEmpty() {
        if (adapter.getList().isNullOrEmpty()) {
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            binding.tvNoData.visibility = View.GONE
        }
    }
}