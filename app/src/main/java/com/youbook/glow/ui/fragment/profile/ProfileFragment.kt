package com.youbook.glow.ui.fragment.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.viewModelScope
import com.android.fade.extension.loadingImage
import com.android.fade.extension.visible
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.booking.BookingsHistoryActivity
import com.android.fade.ui.chat.ChatActivity
import com.android.fade.ui.fragment.dashboard.Offer
import com.android.fade.ui.fragment.profile.ProfileFragmentRepository
import com.android.fade.ui.fragment.profile.ProfileViewModel
import com.android.fade.ui.notification.NotificationActivity
import com.android.fade.ui.payment_history.PaymentHistoryActivity
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.hide
import com.android.fade.utils.Utils.snackbar
import com.youbook.glow.BuildConfig
import com.youbook.glow.R
import com.youbook.glow.databinding.ProfileFragmentBinding
import com.youbook.glow.ui.login_with_email.EmailLoginActivity
import com.youbook.glow.ui.profile.ProfileActivity
import com.youbook.glow.utils.Constants
import kotlinx.coroutines.launch

class ProfileFragment :
    BaseFragment<ProfileViewModel, ProfileFragmentBinding, ProfileFragmentRepository>(),
    View.OnClickListener {
    var userId: String? = null
    var offer: Offer? = null
    private var customerSupportChatId : String = ""

    override fun getViewModel() = ProfileViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListener()

        val name = Prefrences.getPreferences(requireContext(), Constants.USER_NAME)
        userId = Prefrences.getPreferences(requireContext(), Constants.USER_ID)
        binding.tvUserName.text = name

        viewModel.getDashboardResponse.observe(requireActivity()) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()

                    if (it.value.result != null) {
                        customerSupportChatId = it.value.result.customer_support_chat_id!!
                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }
        }

        viewModel.logoutResponse.observe(requireActivity()) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    if (it.value.success!!) {
                        Prefrences.clearPreferences(requireContext())
                        val intent = Intent(requireContext(), EmailLoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val image = Prefrences.getPreferences(requireContext(), Constants.USER_PROFILE_IMAGE)
        loadingImage(requireContext(), image!!, binding.ivUserProfile, true)
        binding.tvUserName.text = Prefrences.getPreferences(requireContext(), Constants.USER_NAME)
        getDashBoardData()
    }

    private fun setClickListener() {
        binding.tvEditProfile.setOnClickListener(this)
        binding.relBookings.setOnClickListener(this)
        binding.relSubscription.setOnClickListener(this)
        binding.relWallet.setOnClickListener(this)
        binding.relNotification.setOnClickListener(this)
        binding.relShare.setOnClickListener(this)
        binding.relHelp.setOnClickListener(this)
        binding.relLogout.setOnClickListener(this)
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Fade")
            var shareMessage = """
            Fade User App

        Tell your friends and family about Fade

        Install App Now
            
        """
            shareMessage = """
        ${shareMessage}Download Android App
        https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
            
        Download iOS App 
        ${Constants.IOS_APP_LINK}
            """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
        }
    }

    private fun openPopupForTakeDeal(offer: Offer?) {
        val dialog = Dialog(requireContext(), R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_take_deal_dialog)
        val tvCancel = dialog.findViewById(R.id.tvCancel) as TextView
        val tvTakeDeal = dialog.findViewById(R.id.tvTakeDeal) as TextView
        val tvDealPrice = dialog.findViewById(R.id.tvDealPrice) as TextView
        val tvOfferName = dialog.findViewById(R.id.tvOfferName) as TextView
        val ivOfferImage = dialog.findViewById(R.id.ivOfferImage) as ImageView

        tvDealPrice.text = offer!!.offerPercentage.toString().plus("% Off")
        tvOfferName.text = offer.offerApplyOnService!!.name
        loadingImage(
            requireContext(),
            Constants.STORAGE_URL.plus(offer.offerApplyOnService.image),
            ivOfferImage,
            false
        )

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        tvTakeDeal.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = ProfileFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = ProfileFragmentRepository(
        MyApi.getInstanceToken(
            Prefrences.getPreferences(
                requireContext(),
                Constants.API_TOKEN
            )!!
        )
    )

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEditProfile -> goToProfileActivity()
            R.id.relBookings -> goToBookingsActivity()
            R.id.relWallet -> goToPaymentHistoryActivity()
            R.id.relNotification -> goToNotificationActivity()
            R.id.relSubscription -> showOffer()
            R.id.relShare -> shareApp()
            R.id.relHelp -> openAdminChatScreen()
            R.id.relLogout -> openPopupForLogout()
        }
    }

    private fun openAdminChatScreen() {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, Constants.CUSTOMER_SUPPORT)
        intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, customerSupportChatId)
        startActivity(intent)
    }

    private fun showOffer() {
        if (offer != null) {
            openPopupForTakeDeal(offer)
        } else {
            binding.root.snackbar("No offer found at this time!")
        }

    }

    @SuppressLint("HardwareIds")
    private fun getDashBoardData() {
        val params = HashMap<String, String>()
        val deviceId =
            Settings.Secure.getString(requireContext().contentResolver, Settings.Secure.ANDROID_ID)
        params["device_id"] = deviceId

        var firebaseToken =
            Prefrences.getPreferences(requireContext(), Constants.FIREBASE_TOKEN).toString()
        if (firebaseToken.isEmpty())
            firebaseToken = "abcdef"

        params["push_token"] = firebaseToken
        params["type"] = "2"
        params["latest_latitude"] =
            Prefrences.getPreferences(requireContext(), Constants.LAT).toString()
        params["latest_longitude"] =
            Prefrences.getPreferences(requireContext(), Constants.LON).toString()

        viewModel.viewModelScope.launch {
            viewModel.getDashBoardData(params)
        }
    }

    private fun goToNotificationActivity() {
        startActivity(Intent(context, NotificationActivity::class.java))
    }

    private fun goToPaymentHistoryActivity() {
        startActivity(Intent(context, PaymentHistoryActivity::class.java))
    }

    private fun goToBookingsActivity() {
        startActivity(Intent(context, BookingsHistoryActivity::class.java))
    }

    private fun goToProfileActivity() {
        startActivity(Intent(context, ProfileActivity::class.java))
    }

    private fun openPopupForLogout() {
        val dialog = Dialog(requireContext(), R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.dialog_logout)
        val tvCancel = dialog.findViewById(R.id.tvCancel) as TextView
        val tvLogout = dialog.findViewById(R.id.tvLogout) as TextView
        /* body.text = title
         val yesBtn = dialog.findViewById(R.id.yesBtn) as Button
         val noBtn = dialog.findViewById(R.id.noBtn) as TextView*/

        tvCancel.setOnClickListener {
            dialog.dismiss()
        }

        tvLogout.setOnClickListener {
            dialog.dismiss()
            logoutUser()
        }
        dialog.show()

    }

    private fun logoutUser() {
        viewModel.viewModelScope.launch {
            viewModel.logoutUser()
        }
    }

}