package com.youbook.glow

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

import com.android.fade.extension.loadingImage
import com.youbook.glow.ui.fragment.booking_list.BookingListFragment
import com.youbook.glow.ui.fragment.chat_list.ChatMessageFragment
import com.youbook.glow.ui.fragment.dashboard.HomeFragment
import com.youbook.glow.ui.fragment.favourite_list.FavouriteFragment
import com.youbook.glow.ui.fragment.profile.ProfileFragment
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.youbook.glow.databinding.ActivityMainBinding
import com.youbook.glow.ui.fragment.new_dashboard.NewDashBoardFragment

import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var currentPage: Int? = 0
    private var otherAddressLatLon = ""

    companion object {
        var open: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root


        if (intent.getStringExtra(Constants.OTHER_ADDRESS_LAT_LON) != null)
            otherAddressLatLon = intent.getStringExtra(Constants.OTHER_ADDRESS_LAT_LON)!!

        setContentView(view)
        setOnClickListener()

        if (open == Constants.BOOKING_LIST) {
            loadBookingListFragment()
            open = ""
        } else {
            loadNewDashboardFragment()
        }
    }

    private fun setOnClickListener() {
        binding.relNavHome.setOnClickListener(this)
        binding.relNavChat.setOnClickListener(this)
        binding.relNavCalendar.setOnClickListener(this)
        binding.relNavProfile.setOnClickListener(this)
        binding.relNavFav.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.relNavHome -> loadNewDashboardFragment()
            R.id.relNavChat -> loadChatFragment()
            R.id.relNavCalendar -> loadBookingListFragment()
            R.id.relNavProfile -> loadProfileFragment()
            R.id.relNavFav -> loadFavouriteFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        val image = Prefrences.getPreferences(this, Constants.USER_PROFILE_IMAGE)!!
        loadingImage(this, image, binding.ivNavProfileSelected, true)
        loadingImage(this, image, binding.ivNavProfileUnselected, true)

        val token = arrayOf("")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.isComplete) {
                    token[0] = task.result
                    Prefrences.savePreferencesString(this, Constants.FIREBASE_TOKEN, token[0])
                }
            }
        }
    }

    private fun loadHomeFragment() {
        if (currentPage != 1) {
            selectHomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, HomeFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun loadNewDashboardFragment() {
        if (currentPage != 1) {
            selectHomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, NewDashBoardFragment(otherAddressLatLon))
                .commitAllowingStateLoss()
        }
    }

    private fun loadChatFragment() {
        if (currentPage != 2) {
            selectChatFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, ChatMessageFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun loadBookingListFragment() {
        if (currentPage != 3) {
            selectCalendarFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, BookingListFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun loadProfileFragment() {
        if (currentPage != 4) {
            selectProfileFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, ProfileFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun loadFavouriteFragment() {
        if (currentPage != 5) {
            selectFavFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrameContainer, FavouriteFragment())
                .commitAllowingStateLoss()
        }
    }

    private fun selectCalendarFragment() {
        currentPage = 3
        binding.ivNavHome.setImageResource(R.drawable.ic_b_home_unselected)
        binding.ivNavChat.setImageResource(R.drawable.ic_b_chat_unselected)
        binding.ivNavFav.setImageResource(R.drawable.ic_b_fav_unselected)
        binding.ivNavCalendar.setImageResource(R.drawable.ic_b_calendar_selected)
        binding.ivNavProfileUnselected.visibility = View.VISIBLE
        binding.ivNavProfileSelected.visibility = View.GONE
    }

    private fun selectFavFragment() {
        currentPage = 5
        binding.ivNavHome.setImageResource(R.drawable.ic_b_home_unselected)
        binding.ivNavChat.setImageResource(R.drawable.ic_b_chat_unselected)
        binding.ivNavCalendar.setImageResource(R.drawable.ic_b_calendar_unselected)
        binding.ivNavFav.setImageResource(R.drawable.ic_b_fav_selected)
        binding.ivNavProfileUnselected.visibility = View.VISIBLE
        binding.ivNavProfileSelected.visibility = View.GONE
    }

    private fun selectHomeFragment() {
        currentPage = 1
        binding.ivNavHome.setImageResource(R.drawable.ic_b_home_selected)
        binding.ivNavChat.setImageResource(R.drawable.ic_b_chat_unselected)
        binding.ivNavCalendar.setImageResource(R.drawable.ic_b_calendar_unselected)
        binding.ivNavFav.setImageResource(R.drawable.ic_b_fav_unselected)
        binding.ivNavProfileUnselected.visibility = View.VISIBLE
        binding.ivNavProfileSelected.visibility = View.GONE
    }

    private fun selectChatFragment() {
        currentPage = 2
        binding.ivNavHome.setImageResource(R.drawable.ic_b_home_unselected)
        binding.ivNavChat.setImageResource(R.drawable.ic_b_chat_selected)
        binding.ivNavCalendar.setImageResource(R.drawable.ic_b_calendar_unselected)
        binding.ivNavProfileUnselected.visibility = View.VISIBLE
        binding.ivNavFav.setImageResource(R.drawable.ic_b_fav_unselected)
        binding.ivNavProfileSelected.visibility = View.GONE
    }

    private fun selectProfileFragment() {
        currentPage = 4
        binding.ivNavHome.setImageResource(R.drawable.ic_b_home_unselected)
        binding.ivNavChat.setImageResource(R.drawable.ic_b_chat_unselected)
        binding.ivNavCalendar.setImageResource(R.drawable.ic_b_calendar_unselected)
        binding.ivNavFav.setImageResource(R.drawable.ic_b_fav_unselected)
        binding.ivNavProfileUnselected.visibility = View.GONE
        binding.ivNavProfileSelected.visibility = View.VISIBLE
    }

}