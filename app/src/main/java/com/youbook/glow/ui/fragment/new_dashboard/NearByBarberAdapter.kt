package com.youbook.glow.ui.fragment.new_dashboard

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.youbook.glow.R
import com.android.fade.extension.loadingImage
import com.android.fade.ui.barber_details.BarberDetailsActivity
import com.android.fade.ui.fragment.new_dashboard.NearByBarbersItem
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.snackbar
import com.youbook.glow.databinding.ItemBarberNearMeBinding

class NearByBarberAdapter(
    private val context: Context,
    private val itemClick : ((item :NearByBarbersItem, po : Int)->Unit)?=null, ) :
    RecyclerView.Adapter<NearByBarberAdapter.ViewHolder>() {

    private val nearByBarberList = ArrayList<NearByBarbersItem>()

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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nearByBarber = nearByBarberList[position]
        holder.binding.tvBarberName.text = nearByBarber.name.toString()

        if (nearByBarber.average_rating != null) {
            holder.binding.barberRatingBar.rating = nearByBarber.average_rating.toFloat()
        }

        loadingImage(context, Constants.STORAGE_URL + nearByBarber.profile!!,holder.binding.ivBarberImage, true)

        if (nearByBarber.total_reviews != null) {
            if (nearByBarber.total_reviews != 0) {
                if (nearByBarber.total_reviews == 1)
                    holder.binding.tvDriverTotalReview.text =
                        "(".plus(nearByBarber.total_reviews.toString() + " Rating)")
                else
                    holder.binding.tvDriverTotalReview.text =
                        "(".plus(nearByBarber.total_reviews.toString() + " Ratings)")
            } else{
                holder.binding.tvDriverTotalReview.text = "(0 Rating)"
            }
        } else {
            holder.binding.tvDriverTotalReview.text = "(0 Rating)"
        }

        if (nearByBarber.is_favourite!!){
            holder.binding.ivBarberFav.setImageResource(R.drawable.ic_fav_barber)
        } else {
            holder.binding.ivBarberFav.setImageResource(R.drawable.ic_unfav_barber)
        }

        if (nearByBarber.services!!.isNotEmpty()){

            val haircutPosition= nearByBarber.services!!.map { x->x!!.service!!.name }.indexOf("Haircut")


            if(haircutPosition > 0){
                val serviceNamePrice = String.format("%.2f",nearByBarber.distance)+" miles away / "+nearByBarber.services!![haircutPosition]!!.service!!.name +" "+context.getString(R.string.pound_sign)+nearByBarber.services!![haircutPosition]!!.service_price
                holder.binding.tvBarberDistance.text = serviceNamePrice
            } else {
                val serviceNamePrice = String.format("%.2f",nearByBarber.distance)+" miles away / "+nearByBarber.services!![0]!!.service!!.name +" "+context.getString(R.string.pound_sign)+nearByBarber.services!![0]!!.service_price
                holder.binding.tvBarberDistance.text = serviceNamePrice
            }

        } else {
            val serviceNamePrice = String.format("%.2f",nearByBarber.distance)+" miles away"
            holder.binding.tvBarberDistance.text = serviceNamePrice
        }

        holder.binding.ivBarberFav.setOnClickListener{
            if (Utils.isConnected(context)){
                itemClick?.invoke(nearByBarber,position)
            } else {
                holder.binding.root.snackbar(context.getString(R.string.check_internet_connection))
            }
        }

        holder.itemView.setOnClickListener{
            val intent = Intent(context, BarberDetailsActivity::class.java)
            intent.putExtra(Constants.BARBER_ID, nearByBarber.barber_id.toString())
            intent.putExtra(Constants.BARBER_DISTANCE, String.format("%.2f",nearByBarber.distance))
            intent.putExtra(Constants.IS_BARBER_FAV, nearByBarber.is_favourite)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = nearByBarberList.size

    fun getList(): List<NearByBarbersItem> {
        return nearByBarberList
    }

    fun updateList(newBarberItem: List<NearByBarbersItem>) {
        nearByBarberList.clear()
        nearByBarberList.addAll(newBarberItem)
        notifyDataSetChanged()
    }
}