package com.android.fade.ui.fragment.chat_list

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fade.chat_models.BarbersItem
import com.android.fade.chat_models.HeaderInbox
import com.youbook.glow.databinding.ItemHeaderInboxBinding
import com.android.fade.utils.Utils

class InboxHeaderAdapter(private val context: Context) : RecyclerView.Adapter<InboxHeaderAdapter.ViewHolder>() {
    private val arrayList: ArrayList<HeaderInbox> = ArrayList()

    class ViewHolder(val binding: ItemHeaderInboxBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemHeaderInboxBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list: HeaderInbox = arrayList[position]
        holder.binding.tvHeader.text = Utils.capitalize(list.title)
        holder.binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = InboxAdapter(context, list.driverList as List<BarbersItem>)
        holder.binding.recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun getItemCount() : Int = arrayList.size

    fun updateList(newOrders: List<HeaderInbox>) {
        arrayList.clear()
        arrayList.addAll(newOrders)
        notifyDataSetChanged()
    }
}