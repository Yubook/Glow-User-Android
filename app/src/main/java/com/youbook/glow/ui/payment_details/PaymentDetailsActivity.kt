package com.android.fade.ui.payment_details

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.youbook.glow.MainActivity
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityPaymentDetailsBinding
import com.android.fade.extension.loadingImage
import com.android.fade.extension.snackBar
import com.android.fade.network.MyApi
import com.android.fade.network.MyStripeApi
import com.android.fade.network.Resource
import com.youbook.glow.utils.Constants
import com.android.fade.utils.CreditCardNumberFormattingTextWatcher
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.enable
import com.android.fade.utils.Utils.getAddress
import com.android.fade.utils.Utils.handleApiError
import com.android.fade.utils.Utils.toast
import com.android.fade.utils.Utils.visible
import com.youbook.glow.ui.payment_details.PaymentDetailsViewModelFactory
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class PaymentDetailsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityPaymentDetailsBinding
    private lateinit var viewModel: PaymentDetailsViewModel
    private lateinit var stripe: Stripe
    private var driverId: String? = null
    private var driverName: String? = null
    private var driverRating: String? = null
    private var driverTotalReview: String? = null
    private var driverImage: String? = null
    private var serviceName: String? = null
    private var servicePrice: String? = "0"
    private var bookedService: String? = null
    private var bookeddate: String? = null
    private var walletBalance: String? = "0"
    private var walletId: String? = null
    private var offerPercentage: String? = null
    private var secretKey: String? = ""
    private var serviceIds = ArrayList<String>()
    private var orderSlotId = ArrayList<String>()
    private var tokenId: String? = null
    private var orderLat: String? = null
    private var orderLon: String? = null
    private var offerAmount: String? = "0.0"
    private var offerAmountParams: String? = "0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            PaymentDetailsViewModelFactory(
                PaymentDetailsRepository(
                    MyApi.getInstanceToken(Prefrences.getPreferences(this, Constants.API_TOKEN)!!),
                    MyStripeApi.getInstance()
                )
            )
        ).get(PaymentDetailsViewModel::class.java)

        stripe = Stripe(this, PaymentConfiguration.getInstance(applicationContext).publishableKey)

        setClickListener()
        getDriverData()
        getPaymentToken()

        viewModel.paymentTokenResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.status.equals("success")) {
                        secretKey = it.value.secret_key
                        print(secretKey)
                    }
                }
                is Resource.Failure -> {
                    getPaymentToken()
                }
            }
        }

        viewModel.cardTokenResponse.observe(this) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.id != null) {
                        tokenId = it.value.id
                        createNewCardPayment(tokenId!!)
                    }
                }
                is Resource.Failure -> handleApiError(binding.root, it) {}
            }
        }

        viewModel.bookAppointmentResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.tvContinue.visibility = View.VISIBLE
                    binding.progressBar.visible(false)
                    if (it.value.success!!) {
                        bookingDone()
                    } else {
                        binding.root.snackBar(it.value.message!!)
                    }
                }
                is Resource.Failure -> {
                    binding.tvContinue.visibility = View.VISIBLE
                    binding.progressBar.visible(false)
                }
            }
        }

        viewModel.bookUserSlotByWalletResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.tvContinue.visibility = View.VISIBLE
                    binding.progressBar.visible(false)
                    if (it.value.success!!) {
                        bookingDone()
                    } else {
                        binding.root.snackBar(it.value.message!!)
                    }
                }
                is Resource.Failure -> {
                    binding.tvContinue.visibility = View.VISIBLE
                    binding.progressBar.visible(false)
                }
            }
        }

        // keep it here after wallet balance get
        setListener()
    }

    private fun createNewCardPayment(tokenId: String) {
        val params = HashMap<String, String>()

        // pass amount * 100
        params["amount"] = (servicePrice!!.toDouble() * 100.0f).toString()
        params["card_token"] = tokenId
        params["user_id"] = Prefrences.getPreferences(application, Constants.USER_ID).toString()
        params["barber_id"] = driverId!!
        params["address"] = getAddress(Prefrences.getPreferences(this, Constants.LAT)!!.toDouble(), Prefrences.getPreferences(this, Constants.LON)!!.toDouble(), this).toString()
        params["latitude"] = Prefrences.getPreferences(this, Constants.LAT).toString()
        params["longitude"] = Prefrences.getPreferences(this, Constants.LON).toString()
        params["payment_type"] = "1" // 1 for card payment , 2 for crypto
        viewModel.viewModelScope.launch {

            print("Slot idssssssssssssss")
            print(orderSlotId)

            print("Service idssssssssssssss")
            print(serviceIds)
            viewModel.bookAppointment(params,orderSlotId,serviceIds)
        }
    }

    private fun setListener() {
        binding.cbDiscount.setOnClickListener(cbDiscountListener)
        binding.cbWalletBal.setOnClickListener(cbWalletListener)
        if (walletBalance!!.toFloat() >= servicePrice!!.toFloat()) {
            binding.cbWalletBal.enable(true)
        } else {
            binding.cbWalletBal.enable(false)
        }

        binding.edtCardExpiryDate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, removed: Int, added: Int) {
                if (s!!.length == 2) {
                    if (start == 2 && removed == 1 && !s.toString().contains("/")) {
                        binding.edtCardExpiryDate.setText("" + s.toString()[0])
                        binding.edtCardExpiryDate.setSelection(1)
                    } else {
                        binding.edtCardExpiryDate.setText(s.toString().plus("/"))
                        binding.edtCardExpiryDate.setSelection(3)
                    }
                }
            }
        })

        binding.edtCardNumber.addTextChangedListener(CreditCardNumberFormattingTextWatcher())
    }

    private fun getDriverData() {
        driverId = intent.getStringExtra(Constants.DRIVER_ID)
        driverName = intent.getStringExtra(Constants.DRIVER_NAME)
        driverImage = intent.getStringExtra(Constants.DRIVER_IMAGE)
        driverRating = intent.getStringExtra(Constants.DRIVER_RATING)
        driverTotalReview = intent.getStringExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT)
        servicePrice = intent.getStringExtra(Constants.DRIVER_SERVICE_PRICE)
        bookedService = intent.getStringExtra(Constants.BOOKED_SERVICE)
        bookeddate = intent.getStringExtra(Constants.BOOKED_DATE)
        serviceIds = intent.getStringArrayListExtra(Constants.BOOKED_SERVICE_ID)!!
        orderSlotId = intent.getStringArrayListExtra(Constants.ORDER_SLOT_ID)!!

        binding.tvDriverName.text = driverName
        binding.tvDriverServices.text = serviceName
        loadingImage(this, Constants.STORAGE_URL + driverImage!!, binding.ivDriverImage, true)
        binding.tvTotalPrice.text = getString(R.string.pound_sign).plus(servicePrice)
        binding.tvBookedService.text = bookedService
        binding.tvDriverTotalReview.text = "Review : $driverTotalReview"
        binding.driverReviewRatingBar.rating = driverRating!!.toFloat()
        binding.tvBookingDate.text = bookeddate

        /*walletBalance = Prefrences.getPreferences(application, Constants.WALLET_PRICE)
        offerAmount = Prefrences.getPreferences(application, Constants.OFFER_AMOUNT).toString()*/

        /*
        Wallet and discount flow

        if (offerAmount.equals("0")) {
            offerAmount = "0.0"
            binding.tvOfferAmount.visibility = View.GONE
        } else {
            binding.tvOfferAmount.visibility = View.VISIBLE
        }
        if (walletBalance.equals("")) {
            walletBalance = "0.0"
        }
        if (servicePrice!!.toFloat() - walletBalance!!.toFloat() <= offerAmount!!.toFloat()) {
            offerAmountParams = (servicePrice!!.toFloat() - walletBalance!!.toFloat()).toString()
        }
        binding.tvOfferAmount.text = "(including £$offerAmount offer amount)"
        walletBalance = (walletBalance!!.toFloat() + offerAmount!!.toFloat()).toString()
        walletId = Prefrences.getPreferences(application, Constants.WALLET_ID).toString()
        offerPercentage = Prefrences.getPreferences(application, Constants.OFFER_PER)
        if (walletBalance.equals("0.0")) {
            binding.cbWalletBal.visibility = View.GONE
        } else {
            binding.cbWalletBal.visibility = View.VISIBLE
        }
        if (offerPercentage.equals("")) {
            binding.cbDiscount.visibility = View.GONE
        } else {
            binding.cbDiscount.visibility = View.VISIBLE
        }
        binding.cbWalletBal.text = getString(R.string.wallet_balance).plus(" £").plus(walletBalance)
        binding.cbDiscount.text = getString(R.string.available_discount).plus(" ").plus(
            offerPercentage
        ).plus("%")*/

    }

    private var cbDiscountListener =
        View.OnClickListener {
            binding.cbWalletBal.isChecked = false
        }

    private var cbWalletListener =
        View.OnClickListener {
            binding.cbDiscount.isChecked = false
        }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvContinue.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.tvContinue -> newPaymentFlow()
        }
    }

    private fun newPaymentFlow() {
        if (validateCardDetails()) {
            var cardDates: List<String>? = null
            if (binding.edtCardExpiryDate.text.length > 4) {
                cardDates = binding.edtCardExpiryDate.text.toString().split("/")
            }
            binding.tvContinue.visibility = View.GONE
            val cardMonth = cardDates!![0]
            val cardYear = cardDates[1]
            val card = HashMap<String, String>()
            card["card[number]"] = binding.edtCardNumber.text.toString()
            card["card[exp_month]"] = cardMonth
            card["card[exp_year]"] = cardYear
            card["card[cvc]"] = binding.edtCVV.text.toString()
            card["card[name]"] = binding.edtUserName.text.toString()
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.getCardTokens(card, "Bearer ".plus(secretKey!!))
            }
        }
    }

    private fun getPaymentToken() {
        viewModel.viewModelScope.launch {
            viewModel.getCardToken()
        }
    }

    private fun bookingDone() {
        val dialog = Dialog(this, R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_booking_done)
        val tvDone = dialog.findViewById(R.id.tvDone) as TextView
        tvDone.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            MainActivity.open = Constants.BOOKING_LIST
            startActivity(intent)
        }
        dialog.show()
    }

    private fun validateCardDetails(): Boolean {
        return when {
            binding.edtUserName.text.isEmpty() -> {
                this.toast(getString(R.string.enter_name_on_card))
                false
            }
            binding.edtCardNumber.text.isEmpty() -> {
                this.toast(getString(R.string.enter_card_number))
                false
            }
            binding.edtCardExpiryDate.text.isEmpty() -> {
                this.toast(getString(R.string.enter_card_expiry_date))
                false
            }
            binding.edtCVV.text.isEmpty() -> {
                this.toast(getString(R.string.enter_cvv))
                false
            }
            else -> {
                true
            }
        }
    }

    private fun paymentContinue() {
        binding.progressBar.visibility = View.VISIBLE
        if (!binding.cbWalletBal.isChecked) {
            if (validateCardDetails()) {
                var cardDates: List<String>? = null
                if (binding.edtCardExpiryDate.text.length > 4) {
                    cardDates = binding.edtCardExpiryDate.text.toString().split("/")
                }
                binding.tvContinue.visibility = View.GONE
                val cardMonth = cardDates!![0]
                val cardYear = cardDates[1]
                val card = HashMap<String, String>()
                card["card[number]"] = binding.edtCardNumber.text.toString()
                card["card[exp_month]"] = cardMonth
                card["card[exp_year]"] = cardYear
                card["card[cvc]"] = binding.edtCVV.text.toString()
                card["card[name]"] = binding.edtUserName.text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.getCardTokens(card, "Bearer ".plus(secretKey!!))
                }
            }
        } else {

            if (servicePrice!!.toFloat() > walletBalance!!.toFloat()) {
                Utils.showErrorDialog(
                    this,
                    "You do not have sufficient wallet balance to Payment, Please go through Card Payment",
                    "Sorry!"
                )
            } else {
                binding.tvContinue.visibility = View.GONE
                viewModel.viewModelScope.launch {
                    val params = HashMap<String, String>()
                    var newServicePrice =
                        (servicePrice!!.toFloat() - offerAmountParams!!.toFloat()).toString()
                    params["amount"] = newServicePrice
                    params["slot_id"] = orderSlotId!!.toString()
                    params["user_id"] =
                        Prefrences.getPreferences(application, Constants.USER_ID).toString()
                    params["service_id"] = serviceIds!!.toString()
                    params["driver_id"] = driverId!!
                    params["wallet_id"] = walletId!!
                    params["latitude"] = orderLat!!
                    params["longitude"] = orderLon!!
                    params["offer_amount"] = offerAmountParams!!
                    viewModel.bookUserSlotByWallet(params)
                }
            }
        }
    }

    private fun createCardPayment(tokenId: String) {
        val params = HashMap<String, String>()
        if (!offerPercentage.equals("0") && binding.cbDiscount.isChecked) {
            // Pay with discount
            val minusPer = (servicePrice!!.toInt() * Prefrences.getPreferences(
                application,
                Constants.OFFER_PER
            )!!.toFloat()) / 100.0f
            val finalPrice = servicePrice!!.toFloat() - minusPer
            params["amount"] = (finalPrice * 100.0F).toString()
            params["card_token"] = tokenId
            params["slot_id"] = orderSlotId.toString()
            params["user_id"] = Prefrences.getPreferences(application, Constants.USER_ID).toString()
            params["service_id"] = serviceIds.toString()
            params["driver_id"] = driverId!!
            params["latitude"] = orderLat!!
            params["longitude"] = orderLon!!
            params["discount"] =
                Prefrences.getPreferences(application, Constants.OFFER_PER).toString()
        } else {
            // Pay Without discount
            params["amount"] = (servicePrice!!.toInt() * 100.0F).toString()
            params["card_token"] = tokenId
            params["slot_id"] = orderSlotId.toString()
            params["user_id"] = Prefrences.getPreferences(application, Constants.USER_ID).toString()
            params["service_id"] = serviceIds.toString()
            params["driver_id"] = driverId!!
            params["latitude"] = orderLat!!
            params["longitude"] = orderLon!!
        }

        CoroutineScope(Dispatchers.IO).launch {
            viewModel.makePayment(params)
        }

    }

}