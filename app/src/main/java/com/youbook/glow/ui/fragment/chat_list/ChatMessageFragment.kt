package com.youbook.glow.ui.fragment.chat_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fade.chat_models.BarbersItem
import com.android.fade.chat_models.HeaderInbox
import com.android.fade.chat_models.InboxResponse
import com.youbook.glow.databinding.ChatMessageFragmentBinding
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.fragment.chat_list.ChatMessageRepository
import com.android.fade.ui.fragment.chat_list.ChatMessageViewModel
import com.android.fade.ui.fragment.chat_list.InboxHeaderAdapter
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.SocketConnector
import com.github.nkzawa.emitter.Emitter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ChatMessageFragment :
    BaseFragment<ChatMessageViewModel, ChatMessageFragmentBinding, ChatMessageRepository>(),
    View.OnClickListener {

    private var headerList: ArrayList<HeaderInbox>? = ArrayList()
    private var adapter: InboxHeaderAdapter? = null
    private var firstTime: Boolean = true

    override fun getViewModel() = ChatMessageViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()
        adapter = InboxHeaderAdapter(requireContext())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        getData()

    }

    private fun setClickListener() {
        binding.swipeRefresh.setOnRefreshListener { getData() }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ChatMessageFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = ChatMessageRepository()
    override fun onClick(v: View?) {
        when (v?.id) {

        }
    }

    private fun getData() {
        if (SocketConnector.getInstance() != null) {
            if (SocketConnector.getSocket()!!.connected()) {
                emitInbox()
                //binding.progressBar.visibility = View.VISIBLE
                onInbox()
            }
        }
    }

    private fun emitInbox() {
        val jsonObject = JSONObject()
        try {
            jsonObject.put(
                "token",
                Prefrences.getPreferences(
                    requireContext(),
                    Constants.API_TOKEN
                )
            )
            SocketConnector.getSocket()!!.emit("getInbox", jsonObject)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    // retrieve inbox data
    private fun onInbox() {
        SocketConnector.getSocket()!!.on("setInbox", Emitter.Listener {
            val data = it[0] as JSONObject
            Log.d("TAG", "onInbox: ".plus(data.toString()))
            binding.swipeRefresh.isRefreshing = false
            val inboxResponse: InboxResponse = Gson()
                .fromJson(
                    data.toString(), object : TypeToken<InboxResponse?>() {}.type
                )
            // val userList: ArrayList<BarbersItem> = ArrayList()
            val adminList: ArrayList<BarbersItem> = ArrayList()
            val barberList: ArrayList<BarbersItem> = ArrayList()

            //val userMap: MutableMap<String, ArrayList<UsersItem>> = HashMap()
            val barberMap: MutableMap<String, ArrayList<BarbersItem>> = HashMap()
            val adminMap: MutableMap<String, ArrayList<BarbersItem>> = HashMap()

            adminList.addAll(inboxResponse.admin as List<BarbersItem>)
            barberList.addAll(inboxResponse.barbers!! as List<BarbersItem>)

            for (user in adminList) {
                adminMap[user.role!!] = adminList
            }
            for (user in barberList) {
                barberMap[user.role!!] = barberList
            }

            /*for (admin in inboxResponse.admin!!) {
                adminMap[admin!!.role!!] = adminList
            }
            adminList.addAll(inboxResponse.admin as List<AdminItem>)
            for (user in inboxResponse.users!!) {
                userMap[user!!.role!!] = userList
            }
            userList.addAll(inboxResponse.users as List<UsersItem>)*/

            headerList!!.clear()

            for ((key, value) in adminMap.entries) {
                headerList!!.add(HeaderInbox(key, value))
            }

            for ((key, value) in barberMap.entries) {
                headerList!!.add(HeaderInbox(key, value))
            }
            if (firstTime)
                notifyList()
        })
    }

    private fun notifyList() {
        if (activity != null) {
            activity?.runOnUiThread {
                binding.recyclerView.recycledViewPool.clear()
                adapter!!.notifyDataSetChanged()
                activity?.runOnUiThread {
                    firstTime = false
                    adapter!!.updateList(headerList as ArrayList<HeaderInbox>)
                    Log.d("TAG", "headerList size " + headerList!!.size)
                    if (headerList!!.size == 0) {
                        binding.tvNoData.visibility = View.VISIBLE
                    } else {
                        binding.tvNoData.visibility = View.GONE
                    }
                }
            }
        }
    }

}