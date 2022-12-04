package com.android.fade.ui.fragment.new_dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.databinding.ItemBarberFilterBinding
import kotlinx.android.synthetic.main.item_barber_services.view.*

class BarberFilterAdapter(private val context: Context,
   private val filterItemClick : ((item : FilterModel,po : Int)-> Unit)? = null) :
    RecyclerView.Adapter<BarberFilterAdapter.ViewHolder>() {

    private val filterItem = ArrayList<FilterModel>()

    class ViewHolder(val binding: ItemBarberFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemBarberFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterModel = filterItem[position]
        holder.binding.tvFilterName.text = filterModel.filter

        if (filterModel.isServiceSelected){
//            holder.binding.relService.setBackgroundResource(R.drawable.service_selected_bg)
            holder.binding.ivFilterSelected.visibility = View.VISIBLE
        } else {
//            holder.binding.relService.setBackgroundResource(R.drawable.service_unselected_bg)
            holder.binding.ivFilterSelected.visibility = View.GONE
        }

       holder.itemView.setOnClickListener{

           filterItemClick!!.invoke(filterModel,position)

           for (i in filterItem.indices) {
               filterItem[i].isServiceSelected = false
           }
           filterItem[position].isServiceSelected = true
           notifyDataSetChanged()
       }
    }

    override fun getItemCount(): Int = filterItem.size

    fun getList(): List<FilterModel> {
        return filterItem
    }

    fun updateList(newOrders: List<FilterModel>) {
        filterItem.clear()
        filterItem.addAll(newOrders)
        notifyDataSetChanged()
    }
}