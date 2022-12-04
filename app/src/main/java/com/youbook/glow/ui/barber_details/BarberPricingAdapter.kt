package com.android.fade.ui.barber_details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.android.fade.ServicesItem
import com.youbook.glow.databinding.ItemBarberPricingBinding


class BarberPricingAdapter(
    private val context: Context,
    private val serviceItemClick: ((item: ServicesItem) -> Unit)? = null
) :
    RecyclerView.Adapter<BarberPricingAdapter.ViewHolder>() {

    private val priceItems = ArrayList<ServicesItem>()

    class ViewHolder(
        val binding: ItemBarberPricingBinding
    ) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemBarberPricingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val services = priceItems[position]
        if (services.isSelected!!) {
            holder.binding.tvAddService.setBackgroundResource(R.drawable.drawable_add_service_btn_black_bg)
            holder.binding.tvAddService.setTextColor(ContextCompat.getColor(context, R.color.app_black))
        } else {
            holder.binding.tvAddService.setBackgroundResource(R.drawable.drawable_add_service_btn_grey_bg)
            holder.binding.tvAddService.setTextColor(ContextCompat.getColor(context, R.color.app_black))
        }

        holder.binding.tvServiceType.text = services.service!!.name
        holder.binding.tvServicePrice.text =
            context.getString(R.string.pound_sign).plus(services.service_price.toString())
        holder.binding.tvServiceTime.text = services.service!!.time!!.toInt().toString().plus("m")

        holder.binding.tvAddService.setOnClickListener {
            services.isSelected = !services.isSelected!!
            serviceItemClick!!.invoke(services)
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int = priceItems.size

    fun getList(): List<ServicesItem> {
        return priceItems
    }

    fun getSelectedServices(): ArrayList<ServicesItem> {
        val selectedServices = ArrayList<ServicesItem>()
        selectedServices.clear()
        for (i in priceItems.indices) {
            if (priceItems[i].isSelected!!) {
                selectedServices += priceItems[i]
            }
        }
        return selectedServices
    }

    fun updateList(newPortfolioList: List<ServicesItem>) {
        priceItems.clear()
        priceItems.addAll(newPortfolioList)
        notifyDataSetChanged()
    }
}

