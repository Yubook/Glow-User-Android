package com.android.fade.ui.fragment.new_dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.youbook.glow.databinding.ItemBarberServicesBinding
import com.android.fade.extension.loadingImage
import com.youbook.glow.utils.Constants

class BarberServicesAdapter(
    private val context: Context,
    private val serviceItemClick: ((item: ResultItem) -> Unit)? = null
) :
    RecyclerView.Adapter<BarberServicesAdapter.ViewHolder>() {
    private val servicesItem = ArrayList<ResultItem>()
    private var selectedServices = ""

    class ViewHolder(
        val binding: ItemBarberServicesBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemBarberServicesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val service = servicesItem[position]
        holder.binding.tvServiceName.text = service.name
        loadingImage(
            context,
            Constants.STORAGE_URL + service.image!!,
            holder.binding.ivServiceIcon,
            false
        )

        if (service.isServiceSelected) {
            holder.binding.relService.setBackgroundResource(R.drawable.service_selected_bg)
            holder.binding.ivServiceSelected.visibility = View.VISIBLE
        } else {
            holder.binding.relService.setBackgroundResource(R.drawable.service_unselected_bg)
            holder.binding.ivServiceSelected.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            service.isServiceSelected = !service.isServiceSelected
            serviceItemClick!!.invoke(service)
            notifyDataSetChanged()
        }
    }

    fun getSelectedServices(): String {
        selectedServices = ""
        for (i in servicesItem.indices) {
            if (servicesItem[i].isServiceSelected) {
                if (i != 0 && selectedServices.isNotEmpty())
                    selectedServices += ","
                selectedServices += servicesItem[i].name!!

            }
        }
        return selectedServices
    }

    override fun getItemCount(): Int = servicesItem.size

    fun getList(): List<ResultItem> {
        return servicesItem
    }

    fun updateList(newServices: List<ResultItem>) {
        servicesItem.clear()
        servicesItem.addAll(newServices)
        notifyDataSetChanged()
    }
}