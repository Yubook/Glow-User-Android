package com.android.fade.ui.barber_details

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.youbook.glow.R
import com.android.fade.ServicesItem

import com.android.fade.extension.loadingImage
import com.android.fade.extension.visible
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.appointment.AppointmentsActivity
import com.android.fade.ui.chat.ChatActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.enable
import com.android.fade.utils.Utils.hide
import com.android.fade.utils.Utils.snackbar
import com.youbook.glow.databinding.ActivityBarberDetailsBinding
import com.youbook.glow.ui.barber_details.BarberDetailsViewModelFactory
import gun0912.tedimagepicker.util.ToastUtil.context
import kotlinx.coroutines.launch
import java.util.*

class BarberDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityBarberDetailsBinding
    private lateinit var viewModel: BarberDetailsViewModel
    private lateinit var barberId: String
    private lateinit var userId: String
    private var driverName: String? = ""
    private var totalTime: String? = ""
    private var driverRating: String? = ""
    private var driverTotalReview: String? = ""
    private var driverImage: String? = ""
    private var selectedDate: String? = ""
    private var driverServices: String? = ""
    private var orderLat: String? = ""
    private var orderLon: String? = ""
    private var distance: Double = 0.0
    private lateinit var portfolioAdapter: BarberPortfolioAdapter
    private lateinit var pricingAdapter: BarberPricingAdapter
    private var selectedServices = ArrayList<ServicesItem>()
    private var isBarberFav = false
    private var isChatEnable = false
    private var groupId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBarberDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            BarberDetailsViewModelFactory(
                BarberDetailsRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(
                            this,
                            Constants.API_TOKEN
                        )!!
                    )
                )
            )
        ).get(BarberDetailsViewModel::class.java)

        userId = Prefrences.getPreferences(this, Constants.USER_ID)!!
        barberId = intent.getStringExtra(Constants.BARBER_ID)!!
        isBarberFav = intent.getBooleanExtra(Constants.IS_BARBER_FAV, false)
        distance = intent.getDoubleExtra(Constants.BARBER_DISTANCE, 0.0)

        onClickListener()
        selectPortfolio()
        getBarberDetails()

        viewModel.makeBarberFavUnFav.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    if (it.value.success!!) {
                        if (it.value.result != null) {
                            binding.root.snackbar(it.value.message!!)
                            isBarberFav = it.value.result.favouriteType!! == 1
                            if (isBarberFav) {
                                binding.ivFavourite.setImageResource(R.drawable.ic_fav_barber)
                            } else {
                                binding.ivFavourite.setImageResource(R.drawable.ic_unfav_barber)
                            }
                        }

                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }
        }

        viewModel.barberDetailsResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    if (it.value.result != null) {
                        setBarberData(it.value.result)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }
        }

    }

    private fun setBarberData(result: Result) {
        driverName = result.name
        driverImage = result.profile
        driverRating = result.averageRating.toString()
        driverTotalReview = result.totalReviews.toString()

        binding.ivFavourite.visibility = View.VISIBLE

        if (isBarberFav) {
            binding.ivFavourite.setImageResource(R.drawable.ic_fav_barber)
        } else {
            binding.ivFavourite.setImageResource(R.drawable.ic_unfav_barber)
        }

        binding.tvBarberDistance.text = distance.toString().plus(" miles away")

        isChatEnable = result.chat!!

        if (result.chat_group_id != null)
            groupId = result.chat_group_id

        if (isChatEnable) {
            binding.ivChat.enable(true)
        } else {
            binding.ivChat.enable(false)
        }

        binding.tvBarberName.text = driverName
        loadingImage(this, Constants.STORAGE_URL + driverImage, binding.ivUserProfile, true)

        // portfolio data
        binding.portfolioRecyclerView.layoutManager = GridLayoutManager(this, 3)
        portfolioAdapter = BarberPortfolioAdapter(this)
        binding.portfolioRecyclerView.adapter = portfolioAdapter

        if (result.portfolios!!.isEmpty()) {
            binding.tvNoPortfolioDataFound.visibility = View.VISIBLE
        } else {
            binding.tvNoPortfolioDataFound.visibility = View.GONE
            portfolioAdapter.updateList(result.portfolios as List<PortfoliosItem>)
        }

        // pricing data
        binding.priceRecyclerView.layoutManager = LinearLayoutManager(this)
        pricingAdapter = BarberPricingAdapter(this) {
            if (pricingAdapter.getSelectedServices().isEmpty()) {
                binding.tvBookAppointment.visibility = View.GONE
            } else {
                selectedServices = pricingAdapter.getSelectedServices()
                binding.tvBookAppointment.visibility = View.VISIBLE

            }
        }

        binding.priceRecyclerView.adapter = pricingAdapter
        if (result.services!!.isEmpty()) {
            binding.tvNoPricingDataFound.visibility = View.VISIBLE
        } else {
            binding.tvNoPricingDataFound.visibility = View.GONE
            pricingAdapter.updateList(result.services as List<ServicesItem>)
        }

        // review data
        binding.tvFiveStarReview.text = result.fivestar.toString()
        binding.tvFourStarReview.text = result.fourstar.toString()
        binding.tvThreeStarReview.text = result.threestar.toString()
        binding.tvTwoStarReview.text = result.twostar.toString()
        binding.tvOneStarReview.text = result.onestar.toString()

        if (result.reviews!!.isNotEmpty())
            findingValueServiceHygieneRates(result.reviews)

        if (result.totalReviews != 0) {
            binding.fiveStarProgress.progress = result.fivestar!! * 100 / result.totalReviews!!
            binding.fourStarProgress.progress = result.fourstar!! * 100 / result.totalReviews
            binding.threeStarProgress.progress = result.threestar!! * 100 / result.totalReviews
            binding.twoStarProgress.progress = result.twostar!! * 100 / result.totalReviews
            binding.oneStarProgress.progress = result.onestar!! * 100 / result.totalReviews
        }

        // Terms Data
        var terms: String = result.policyAndTerm!!.content!!

        if (terms.isEmpty()) {
            binding.tvNoTermsDataFound.visibility = View.VISIBLE
        } else {
            binding.tvNoTermsDataFound.visibility = View.GONE
            binding.tvBarberTerms.text = terms
        }

    }

    private fun findingValueServiceHygieneRates(reviews: List<ReviewsItem?>?) {
        var totalValueRating = 0.0
        var totalServiceRating = 0.0
        var totalHygieneRating = 0.0

        reviews!!.forEach {
            totalHygieneRating += it!!.hygiene!!
            totalValueRating += it.value!!
            totalServiceRating += it.service!!
        }

        val valueRate = totalValueRating / reviews.size
        val serviceRate = totalServiceRating / reviews.size
        val hygieneRate = totalHygieneRating / reviews.size

        binding.tvValueRating.text = String.format("%.2f", valueRate)
        binding.tvServiceRating.text = String.format("%.2f", serviceRate)
        binding.tvHygieneRating.text = String.format("%.2f", hygieneRate)

        val totalRating = valueRate + serviceRate + hygieneRate
        val overAllRate = totalRating / 3

        binding.tvOverallReview.text = String.format("%.2f", overAllRate)

    }

    private fun getBarberDetails() {
        viewModel.viewModelScope.launch {
            viewModel.getBarberDetails(barberId)
        }
    }

    private fun onClickListener() {
        binding.tvPortfolio.setOnClickListener(this)
        binding.tvPricing.setOnClickListener(this)
        binding.tvReview.setOnClickListener(this)
        binding.tvTerms.setOnClickListener(this)
        binding.ivBackButton.setOnClickListener(this)
        binding.ivFavourite.setOnClickListener(this)
        binding.tvBookAppointment.setOnClickListener(this)
        binding.ivChat.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvPortfolio -> selectPortfolio()
            R.id.tvPricing -> selectPricing()
            R.id.tvReview -> selectReview()
            R.id.tvTerms -> selectTerms()
            R.id.ivFavourite -> makeBarberFavUnFav()
            R.id.tvBookAppointment -> goToSelectSlotScreen()
            R.id.ivChat -> openChatScreen()
            R.id.ivBackButton -> finish()
        }
    }

    private fun openChatScreen() {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, driverName)
        intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, groupId)
        context.startActivity(intent)
    }

    private fun goToSelectSlotScreen() {
        val intent = Intent(context, AppointmentsActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, barberId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)
        intent.putExtra(Constants.DRIVER_RATING, driverRating)
        intent.putExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT, driverTotalReview)
        intent.putParcelableArrayListExtra(
            Constants.BOOKED_SERVICE,
            selectedServices as ArrayList<out Parcelable?>?
        )
        startActivity(intent)
    }

    private fun makeBarberFavUnFav() {
        val params = HashMap<String, String>()
        params["user_id"] = userId
        params["barber_id"] = barberId
        if (isBarberFav) {
            // if already fav make it un-favourite
            params["type"] = "0"
        } else {
            params["type"] = "1"
        }
        viewModel.viewModelScope.launch {
            viewModel.makeBarberFavUnFav(params)
        }
    }

    private fun selectTerms() {
        binding.relTerms.visibility = View.VISIBLE
        binding.relPortfolio.visibility = View.GONE
        binding.relPricing.visibility = View.GONE
        binding.relReview.visibility = View.GONE
        selectedButton(binding.tvTerms)
        unselectedButton(binding.tvPortfolio)
        unselectedButton(binding.tvReview)
        unselectedButton(binding.tvPricing)
    }

    private fun selectReview() {
        binding.relReview.visibility = View.VISIBLE
        binding.relPortfolio.visibility = View.GONE
        binding.relPricing.visibility = View.GONE
        binding.relTerms.visibility = View.GONE
        selectedButton(binding.tvReview)
        unselectedButton(binding.tvPortfolio)
        unselectedButton(binding.tvTerms)
        unselectedButton(binding.tvPricing)
    }

    private fun selectPricing() {
        binding.relPricing.visibility = View.VISIBLE
        binding.relPortfolio.visibility = View.GONE
        binding.relReview.visibility = View.GONE
        binding.relTerms.visibility = View.GONE
        selectedButton(binding.tvPricing)
        unselectedButton(binding.tvPortfolio)
        unselectedButton(binding.tvReview)
        unselectedButton(binding.tvTerms)
    }

    private fun selectPortfolio() {
        binding.relPortfolio.visibility = View.VISIBLE
        binding.relPricing.visibility = View.GONE
        binding.relReview.visibility = View.GONE
        binding.relTerms.visibility = View.GONE
        selectedButton(binding.tvPortfolio)
        unselectedButton(binding.tvPricing)
        unselectedButton(binding.tvReview)
        unselectedButton(binding.tvTerms)
    }

    private fun unselectedButton(tvUnSelectedTextView: TextView) {
        tvUnSelectedTextView.setBackgroundResource(R.drawable.drawable_light_gray_rounded_corner_bg)
        tvUnSelectedTextView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.unselected_rating_color
            )
        )
    }

    private fun selectedButton(tvSelectedTextView: TextView) {
        tvSelectedTextView.setBackgroundResource(R.drawable.drawable_black_rounded_corner_bg)
        tvSelectedTextView.setTextColor(ContextCompat.getColor(this, R.color.black))
    }

}