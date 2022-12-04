package com.android.fade.ui.select_service

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.youbook.glow.databinding.ItemServicesBinding

class ServiceAdapter(private val context: Context) :
    RecyclerView.Adapter<ServiceAdapter.ViewHolder>() {
    private val serviceList = ArrayList<ResultItem>()

    class ViewHolder(val binding: ItemServicesBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemServicesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val services = serviceList[position]
        holder.binding.tvServiceName.text = services.name
        holder.binding.tvServicePrice.text =
            context.getText(R.string.pound_sign).toString().plus(" ").plus(services.price.toString())

    }

    fun getServiceName() : String{
        if (serviceList.size > 0){
            return serviceList[0].name.toString()
        }
        return ""
    }

    fun getServiceId() : String{
        if (serviceList.size > 0){
            return serviceList[0].id.toString()
        }
        return ""
    }

    fun getServicePrice() : String{
        if (serviceList.size > 0){
            return serviceList[0].price.toString()
        }
        return "0"
    }

    override fun getItemCount(): Int = serviceList.size

    fun updateList(newServices: List<ResultItem>) {
        serviceList.clear()
        serviceList.addAll(newServices)
        notifyDataSetChanged()
    }
}