package com.android.fade.ui.fragment.chat_list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.android.fade.chat_models.BarbersItem
import com.youbook.glow.databinding.ItemChatBinding
import com.android.fade.extension.loadingImage
import com.android.fade.ui.chat.ChatActivity
import com.youbook.glow.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class InboxAdapter(private val context: Context, private val driverList: List<BarbersItem>) :
    RecyclerView.Adapter<InboxAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list: BarbersItem = driverList[position]
        if (list.lastActiveTime.equals("1")) {
            holder.binding.relStatus.background = AppCompatResources.getDrawable(
                context,
                R.drawable.drawable_circle_online
            )
        } else {
            holder.binding.relStatus.background = AppCompatResources.getDrawable(
                context,
                R.drawable.drawable_circle_ofline
            )
        }

        holder.binding.tvDriverName.text = list.name.toString()
        loadingImage(
            context,
            Constants.STORAGE_URL.plus(driverList[position].profile.toString()),
            holder.binding.ivDriverImage,
            true
        )

        holder.binding.tvLastSeenTime.text =
            if (list.lastActiveTime.equals("1")) "Active Now" else "Last seen " + list.lastActiveTime

        holder.binding.tvTotalUnreadCount.visibility =
            if (list.unreadCount != 0) View.VISIBLE else View.GONE
        holder.binding.tvTotalUnreadCount.text = "".plus(list.unreadCount)


        if (list.order != null && list.order.isNotEmpty()){

            if (list.order[0]!!.service != null && list.order[0]!!.service!!.isNotEmpty()){
                if (list.order[0]!!.service?.get(0)?.slotTime.toString().length == 4){
                    list.order[0]!!.service?.get(0)?.slotTime = "0"+list.order[0]!!.service?.get(0)?.slotTime
                }
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val currentTime: String = sdf.format(Date())
            val orderSlotStartTime =
                (list.order?.get(0)!!.service?.get(0)?.slotTime.toString().substring(0, 2)).toInt() - 1
            val orderSlotEndTime = (list.order[0]!!.service?.get(0)?.slotTime.toString().substring(2, 5))
            val orderSlotStartTime1 = list.order[0]!!.service?.get(0)?.slotDate.plus(" ")
                .plus(orderSlotStartTime.toString().plus(orderSlotEndTime))
            var orderSlotEndChatTime = list.order[0]!!.service?.get(0)?.slotDate.plus(" ").plus(
                list.order?.get(0)!!.service?.get(0)?.slotTime.toString()
                    .substring(list.order[0]!!.service?.get(0)?.slotTime.toString().length - 5)
            )
            val currentTimeDate = sdf.parse(currentTime)
            val slotDate = sdf.parse(orderSlotStartTime1)
            val slotEndChatDate = sdf.parse(orderSlotEndChatTime)

            holder.itemView.setOnClickListener {
                if (list.order[0]!!.isOrderComplete == 0) {
                    if (currentTimeDate.after(slotDate) && currentTimeDate.before(slotEndChatDate)) {
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, list.name)
                        intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, list.groupId.toString())
                        intent.putExtra("History", "NO")
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "You can Chat with Driver Before 1 Hour of your Order Time", Toast.LENGTH_LONG).show()
                    }
                }
            }

        } else {
            holder.itemView.setOnClickListener {
                if (list.role!! == "admin") {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, Constants.CUSTOMER_SUPPORT)
                    intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, list.groupId.toString())
                    context.startActivity(intent)
                }
            }
        }


    }

    override fun getItemCount(): Int = driverList.size


}