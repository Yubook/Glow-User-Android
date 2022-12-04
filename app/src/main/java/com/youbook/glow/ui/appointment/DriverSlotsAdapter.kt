package com.android.fade.ui.appointment

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.android.fade.ui.barber_details.AvailableSlotsItem
import com.youbook.glow.databinding.ItemDriverSlotsBinding
import gun0912.tedimagepicker.util.ToastUtil
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DriverSlotsAdapter(private val context: Context, private val requiredNumberOfSlots: Int,
                         private val slotItemClick: ((item: AvailableSlotsItem) -> Unit)? = null) :
    RecyclerView.Adapter<DriverSlotsAdapter.ViewHolder>() {

    private val slotsList = ArrayList<AvailableSlotsItem>()
    class ViewHolder(val binding: ItemDriverSlotsBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemDriverSlotsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slot = slotsList[position]

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val currentTime: String = sdf.format(Date())
        holder.binding.tvSlotTime.text = slot.time!!.time.toString()
        val slotNewDate = slot.date.plus(" ").plus(slot.time.time.toString())
        val currentTimeDate = sdf.parse(currentTime)
        val slotDate = sdf.parse(slotNewDate)

        if (slot.isBooked == 1 || currentTimeDate.after(slotDate)) {
            holder.binding.tvSlotTime.setTextColor(ContextCompat.getColor(context, R.color.black))
            holder.binding.tvSlotTime.setBackgroundResource(R.drawable.drawable_grey_rounded_corner_bg)
        } else {
            if (slot.isSelected) {
                holder.binding.tvSlotTime.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.white
                    )
                )
                holder.binding.tvSlotTime.setBackgroundResource(R.drawable.drawable_black_rounded_corner_bg)
            } else {
                holder.binding.tvSlotTime.setBackgroundResource(0)
                holder.binding.tvSlotTime.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.black
                    )
                )
            }
        }

        holder.binding.tvSlotTime.text = slot.time.time
        holder.itemView.setOnClickListener {

            if (slot.isSelected){
                if (slot.isBooked != 1 && !currentTimeDate.after(slotDate)) {
                    slot.isSelected = !slot.isSelected
                    notifyDataSetChanged()
                    slotItemClick!!.invoke(slot)
                }
            } else {
                if (getSelectedSlots().size < requiredNumberOfSlots) {
                    if (slot.isBooked != 1 && !currentTimeDate.after(slotDate)) {
                        slot.isSelected = !slot.isSelected
                        notifyDataSetChanged()
                        slotItemClick!!.invoke(slot)
                    }
                } else{
                    ToastUtil.showToast("You have selected maximum number of slots")
                }
            }

        }
    }


    fun getSelectedSlotId(): Int {
        for (i in slotsList.indices) {
            if (slotsList[i].isSelected) {
                return slotsList[i].id!!
            }
        }
        return 0
    }

    fun getSelectedSlots(): ArrayList<AvailableSlotsItem> {
        val selectedSlots = ArrayList<AvailableSlotsItem>()
        selectedSlots.clear()
        for (i in slotsList.indices) {
            if (slotsList[i].isSelected) {
                selectedSlots += slotsList[i]
            }
        }
        return selectedSlots
    }

    fun getSelectedSlotTime(): String {
        for (i in slotsList.indices) {
            if (slotsList[i].isSelected) {
                return slotsList[i].time!!.time.toString()
            }
        }
        return ""
    }

    override fun getItemCount(): Int = slotsList.size

    fun updateList(newSlots: List<AvailableSlotsItem>) {
        slotsList.clear()
        slotsList.addAll(newSlots)
        notifyDataSetChanged()
    }
}