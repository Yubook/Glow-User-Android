package com.android.fade.ui.fragment.booking_list

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.youbook.glow.databinding.ItemBookingBinding
import com.android.fade.extension.loadingImage
import com.android.fade.extension.visible
import com.android.fade.ui.add_review.AddReviewActivity
import com.android.fade.ui.chat.ChatActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils
import com.youbook.glow.ui.new_add_review.NewAddReviewActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class BookingListAdapter(
    private val context: Context,
    private val itemClick: ((item: DataItem, type: String) -> Unit)? = null
) :
    RecyclerView.Adapter<BookingListAdapter.ViewHolder>() {
    private val orderItem = ArrayList<DataItem>()
    private var mOrderType: String = ""

    class ViewHolder(val binding: ItemBookingBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = orderItem[position]
        holder.binding.tvDriverName.text = item.barber!!.name
        var services: String? = ""
        for (i in item.service!!.indices) {
            if (i == 0) {
                services += item.service[i]!!.serviceName.toString()
            } else {
                services += ", "
                services += item.service[i]!!.serviceName.toString()
            }
        }

        if (item.service[0]!!.slotTime.toString().length == 4) {
            item.service[0]!!.slotTime = "0" + item.service[0]!!.slotTime
        }

        // handle add review button - order type wise
        // is_order_complete = 0 - Pending , 1- completed, 2-cancelled
        when {
            item.isOrderComplete!!.toString() == "0" -> {
                if (mOrderType == Constants.ORDER_TYPE_PREVIOUS) {
                    holder.binding.tvAddReview.visibility = View.GONE
                    holder.binding.tvChatHistory.visibility = View.GONE
                } else {
                    holder.binding.tvAddReview.text = context.getString(R.string.cancel_booking)
                    holder.binding.tvAddReview.visibility = View.VISIBLE
                    holder.binding.tvChatHistory.visibility = View.GONE
                }

            }

            item.isOrderComplete.toString() == "1" -> {
                if (mOrderType == Constants.ORDER_TYPE_PREVIOUS) {
                    holder.binding.tvChatHistory.visibility = View.GONE
//                    holder.binding.tvAddReview.text = context.getString(R.string.add_review)
//                    holder.binding.tvAddReview.visibility = View.VISIBLE
                } else {
                    holder.binding.tvChatHistory.visibility = View.VISIBLE
                }

            }
            item.isOrderComplete.toString() == "2" -> {
                holder.binding.tvChatHistory.visibility = View.GONE
                holder.binding.tvAddReview.visibility = View.GONE
            }
            else -> {
//                holder.binding.tvOrderStatus.visibility = View.GONE
                holder.binding.tvAddReview.visibility = View.GONE
                holder.binding.tvChatHistory.visibility = View.GONE
            }

        }

        if (item.chat!!) {
            holder.binding.ivChat.visible(true)
            if (holder.binding.tvChatHistory.visibility == View.VISIBLE)
                holder.binding.tvChatHistory.visible(false)
        } else {
            holder.binding.ivChat.visible(false)
            if (holder.binding.tvChatHistory.visibility == View.VISIBLE)
                holder.binding.tvChatHistory.visible(false)
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val currentTime: String = sdf.format(Date())
        val slotNewDate = item.service[0]!!.slotDate.plus(" ").plus(
            item.service[0]!!.slotTime.toString().substring(0, 5)
        )
        val currentTimeDate = sdf.parse(currentTime)
        val slotDate = sdf.parse(slotNewDate)

//        var time = LocalDateTime.parse(slotDate!!.toString())

        //    time.minute
        if (!currentTimeDate!!.after(slotDate)) {
            holder.binding.tvAddReview.setBackgroundResource(R.drawable.drawable_black_rounded_corner_bg)
        } else {
            if (item.isOrderComplete.toString() == "0") {
                holder.binding.tvAddReview.setBackgroundResource(R.drawable.drawable_dark_grey_rounded_corner_bg)
            }
        }
        val slotTime = Utils.formatDate(
            "hh:mm", "hh:mm a",
            item.service[0]?.slotTime!!
        )

        val orderDate =
            Utils.formatDate("yyyy-MM-dd", "EEEE, dd MMM yyyy", item.service[0]?.slotDate)
                .plus(" @ ").plus(
                    slotTime
                )

        holder.binding.tvBookingDate.text = orderDate

        holder.binding.tvDriverServices.text = services
        holder.binding.tvBookedService.text = services

        holder.binding.tvDriverTotalReview.text =
            "Review : ".plus(item.barber.totalReviews.toString())
        loadingImage(
            context,
            Constants.STORAGE_URL.plus(item.barber.profile.toString()),
            holder.binding.ivDriverImage,
            true
        )
        holder.binding.driverReviewRatingBar.rating = item.barber.averageRating!!.toFloat()
        holder.binding.tvTotalPrice.text =
            context.getText(R.string.pound_sign).toString().plus("")
                .plus(item.amount.toString())

        holder.binding.tvChatHistory.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, item.barber.name)
            intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, item.chat_group_id)
            intent.putExtra("History", "YES")
            context.startActivity(intent)
        }

        holder.binding.ivChat.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, item.barber.name)
            intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, item.chat_group_id)
            context.startActivity(intent)
        }

        holder.binding.linBooking.setOnClickListener {
            var reviewImages = ""
            val intent = Intent(context, NewAddReviewActivity::class.java)
            intent.putExtra(Constants.DRIVER_ID, item.barber.id.toString())
            intent.putExtra(Constants.ORDER_ID, item.id.toString())
            intent.putExtra(Constants.DRIVER_NAME, item.barber.name.toString())
            intent.putExtra(Constants.ORDER_TYPE, mOrderType)
            intent.putExtra(
                Constants.DRIVER_IMAGE,
                Constants.STORAGE_URL.plus(item.barber.profile.toString())
            )
            intent.putExtra(
                Constants.DRIVER_RATING,
                item.barber.totalReviews.toString()
            )
            intent.putExtra(
                Constants.DRIVER_REVIEW_TOTAL_COUNT,
                item.barber.averageRating
            )
            intent.putExtra(Constants.BOOKED_SERVICE, services)
            intent.putExtra(Constants.BOOKED_DATE, orderDate)
            intent.putExtra(
                Constants.DRIVER_SERVICE_PRICE,
                context.getText(R.string.pound_sign).toString().plus(" ")
                    .plus(item.amount.toString())
            )
            intent.putExtra(Constants.ORDER_ADDRESS, item.address)
            intent.putExtra(Constants.ORDER_LAT, item.latitude)
            intent.putExtra(Constants.ORDER_LON, item.longitude)
            intent.putExtra(Constants.IS_BARBER_FAV, item.isBarberFavourite)
            intent.putExtra(Constants.IS_ORDER_COMPLETE, item.isOrderComplete.toString())
            intent.putExtra(Constants.BARBER_DISTANCE, item.distance)

            intent.putExtra(Constants.IS_CHAT_ENABLE, item.chat)
            intent.putExtra(Constants.GROUP_ID, item.chat_group_id)
            intent.putExtra(Constants.ORDER_STATUS, item.isOrderComplete)

            if (item.review!!.isEmpty()) {
                intent.putExtra(Constants.REVIEW_ID, "")

                intent.putExtra(Constants.SERVICE_RATING, "")
                intent.putExtra(Constants.HYGIENE_RATING, "")
                intent.putExtra(Constants.VALUE_RATING, "")
                intent.putExtra(Constants.REVIEW_IMAGE, "")
            } else {
                intent.putExtra(Constants.REVIEW_ID, item.review[0]!!.id.toString())
                intent.putExtra(Constants.SERVICE_RATING, item.review[0]!!.service.toString())
                intent.putExtra(Constants.HYGIENE_RATING, item.review[0]!!.hygiene.toString())
                intent.putExtra(Constants.VALUE_RATING, item.review[0]!!.value.toString())

                item.review[0]!!.reviewImages!!.forEach {
                    reviewImages += it!!.image.toString() + ","
                }
                intent.putExtra(Constants.REVIEW_IMAGE, reviewImages)
            }
            context.startActivity(intent)

        }
        holder.binding.tvAddReview.setOnClickListener {
            if (item.isOrderComplete.toString() == "0") {
                if (!currentTimeDate.after(slotDate)) {
                    cancelOrderDialog(context, item)
                }
            } else {
                giveReviewToDriver(
                    item.barber.id.toString(), item.barber.name.toString(),
                    Constants.STORAGE_URL.plus(item.barber.profile.toString()), item.id.toString()
                )
            }
        }
    }

    private fun cancelOrderDialog(context: Context, item: DataItem) {
        val dialog = Dialog(this.context, R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.cancel_order_dialog)
        val tvYes = dialog.findViewById(R.id.tvYes) as TextView
        val tvNo = dialog.findViewById(R.id.tvNo) as TextView

        tvYes.setOnClickListener {
            itemClick!!.invoke(item, "Cancel")
            dialog.cancel()
        }

        tvNo.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
    }

    private fun giveReviewToDriver(
        driverId: String,
        driverName: String,
        driverImage: String,
        orderId: String?
    ) {
        val intent = Intent(context, AddReviewActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, driverId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)
        intent.putExtra(Constants.ORDER_ID, orderId)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = orderItem.size

    fun getList(): List<DataItem> {
        return orderItem
    }

    fun updateList(newOrders: ArrayList<DataItem>, orderType: String) {
        mOrderType = orderType
        orderItem.clear()
        orderItem.addAll(newOrders)
        notifyDataSetChanged()
    }
}