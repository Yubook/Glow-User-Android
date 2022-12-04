package com.android.fade.ui.fragment.favourite_list

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.youbook.glow.databinding.ItemBarberNearMeBinding
import com.android.fade.extension.loadingImage
import com.android.fade.ui.barber_details.BarberDetailsActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar

class MyFavBarberAdapter(
    private val context: Context,
    private val itemClick: ((item: ResultItem, po: Int) -> Unit)? = null
) :
    RecyclerView.Adapter<MyFavBarberAdapter.ViewHolder>() {

    private val nearByBarberList = ArrayList<ResultItem>()

    class ViewHolder(
        val binding: ItemBarberNearMeBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding = ItemBarberNearMeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nearByBarber = nearByBarberList[position]
        holder.binding.tvBarberName.text = nearByBarber.name.toString()

        if (nearByBarber.average_rating != null) {
            holder.binding.barberRatingBar.rating = nearByBarber.average_rating.toFloat()
        }

        loadingImage(
            context,
            Constants.STORAGE_URL + nearByBarber.profile!!,
            holder.binding.ivBarberImage,
            true
        )

        if (nearByBarber.total_reviews != null) {
            if (nearByBarber.total_reviews != 0) {
                if (nearByBarber.total_reviews == 1)
                    holder.binding.tvDriverTotalReview.text =
                        "(" + nearByBarber.total_reviews.toString() + "Rating)"
                else
                    holder.binding.tvDriverTotalReview.text =
                        "(" + nearByBarber.total_reviews.toString() + "Ratings)"
            }
        }

        holder.binding.ivBarberFav.setImageResource(R.drawable.ic_fav_barber)

        if (nearByBarber.services!!.isNotEmpty()) {
            val serviceNamePrice = String.format(
                "%.2f",
                nearByBarber.distance
            ) + " miles away / " + nearByBarber.services[0]!!.service!!.name + " " + context.getString(
                R.string.pound_sign
            ) + nearByBarber.services!![0]!!.service_price
            holder.binding.tvBarberDistance.text = serviceNamePrice
        }

        holder.binding.ivBarberFav.setOnClickListener {
            if (Utils.isConnected(context)) {
                itemClick?.invoke(nearByBarber, position)
            } else {
                holder.binding.root.snackbar(context.getString(R.string.check_internet_connection))
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, BarberDetailsActivity::class.java)
            intent.putExtra(Constants.BARBER_ID, nearByBarber.barber_id.toString())
            intent.putExtra(Constants.BARBER_DISTANCE, String.format("%.2f", nearByBarber.distance))
            intent.putExtra(Constants.IS_BARBER_FAV, true)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = nearByBarberList.size

    fun getList(): List<ResultItem> {
        return nearByBarberList
    }

    fun updateList(newBarberItem: List<ResultItem>) {
        nearByBarberList.clear()
        nearByBarberList.addAll(newBarberItem)
        notifyDataSetChanged()
    }
}