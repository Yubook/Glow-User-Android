package com.youbook.glow.ui.fragment.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Secure
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.youbook.glow.R
import com.youbook.glow.databinding.FragmentHomeBinding
import com.android.fade.extension.loadingImage
import com.android.fade.extension.snackBar
import com.android.fade.extension.visible
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.appointment.AppointmentsActivity
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.fragment.dashboard.HomeRepository
import com.android.fade.ui.fragment.dashboard.HomeViewModel
import com.android.fade.ui.fragment.dashboard.Offer
import com.android.fade.ui.fragment.dashboard.ResultItem
import com.android.fade.ui.notification.NotificationActivity
import com.android.fade.ui.review.ReviewsActivity
import com.android.fade.ui.search_place.AutoCompleteAdapter
import com.youbook.glow.utils.Constants
import com.android.fade.utils.ManagePermissions
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.hide
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_search_place.*
import kotlinx.coroutines.launch
import java.util.*

class HomeFragment() : BaseFragment<HomeViewModel, FragmentHomeBinding, HomeRepository>(),
    View.OnClickListener {
    private val PermissionsRequestCode = 111
    private var driverLat: String? = null
    private var driverLon: String? = null
    private var driverAddress: String? = null
    private var driverId: String? = ""
    private var driverName: String? = ""
    private var driverImage: String? = ""
    private var driverRating: String? = ""
    private var driverTotalReview: String? = ""
    private val PLACE_PICKER_REQUEST = 3
    var mapFragment: SupportMapFragment? = null
    lateinit var placesClient: PlacesClient
    lateinit var mAdapter: AutoCompleteAdapter
    var mCurrentLat: String? = null
    var mCurrentLon: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var managePermissions: ManagePermissions
    private var myGoogleMap: GoogleMap? = null

    val list = listOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val callback = OnMapReadyCallback { googleMap ->
        var driverLatLon: LatLng? = null
        myGoogleMap = googleMap

        //googleMap.clear()
        if (driverLat != null && driverLon != null) {
            driverLatLon = LatLng(
                driverLat!!.toDouble(),
                driverLon!!.toDouble()
            )
        } else {
            if (mCurrentLat != null && mCurrentLon != null) {
                driverLatLon = LatLng(
                    mCurrentLat!!.toDouble(),
                    mCurrentLon!!.toDouble()
                )
            }
        }

        if (driverLatLon != null) {
            googleMap.addMarker(
                MarkerOptions()
                    .position(driverLatLon)
                    .title(binding.tvDriverName.text.toString())
                    .snippet(driverAddress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_truck_pin))
            )
            binding.relMap.visibility = View.VISIBLE
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_map))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLon, 14F))
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        managePermissions = ManagePermissions(requireActivity(), list, PermissionsRequestCode)
        setClickListener()

        // Initialize Places.
        var apiKey = getString(R.string.place_api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey)
        }
        placesClient = Places.createClient(requireContext())
        setUpAutoCompleteTextView()

        val currentAddress = Utils.getAddress(
            Prefrences.getPreferences(
                requireContext(),
                Constants.LAT
            )!!.toDouble(),
            Prefrences.getPreferences(requireContext(), Constants.LON)!!.toDouble(),
            requireContext()
        )
        binding.tvBookingLocation.text = currentAddress

        viewModel.getOfferResponse.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    getNearestDriver()
                    if (it.value.result != null) {
                        if (it.value.result.is_read!!) {
                            binding.ivNotificationBadge.visibility = View.VISIBLE
                        } else {
                            binding.ivNotificationBadge.visibility = View.GONE
                        }

                        if (it.value.result.checkoffer!!) {
                            openPopupForTakeDeal(it.value.result.offer)
                            Prefrences.savePreferencesString(
                                requireContext(),
                                Constants.OFFER_PER,
                                it.value.result.offer!!.offerPercentage.toString()
                            )
                        } else {
                            Prefrences.savePreferencesString(
                                requireContext(),
                                Constants.OFFER_PER,
                                ""
                            )
                        }

                        if (it.value.result.wallet != null) {

                            Prefrences.savePreferencesString(
                                requireContext(),
                                Constants.OFFER_AMOUNT,
                                it.value.result.wallet.offer_amount.toString()
                            )

                            Prefrences.savePreferencesString(
                                requireContext(),
                                Constants.WALLET_PRICE,
                                it.value.result.wallet.amount.toString()
                            )

                            Prefrences.savePreferencesString(
                                requireContext(),
                                Constants.WALLET_ID,
                                it.value.result.wallet.id.toString()
                            )
                        }
                    } else {
                        Utils.showErrorDialog(requireContext(), it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                    getOffer()
                }
            }
        })

        viewModel.getDriversResponse.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.hide()
                    if (it.value.result != null && it.value.result.isNotEmpty()) {
                        binding.relDriverCard.visibility = View.VISIBLE
                        setNearestDriverData(it.value.result)
                    } else {
                        //myGoogleMap!!.clear()
                        binding.relDriverCard.visibility = View.GONE
                        binding.root.snackBar("No Driver Found at this location!!")
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                }
            }
        })

    }

    private fun setUpAutoCompleteTextView() {
        autocomplete.threshold = 1
        autocomplete.onItemClickListener = autocompleteClickListener
        mAdapter = AutoCompleteAdapter(requireContext(), placesClient)
        autocomplete.setAdapter(mAdapter)
    }

    private fun checkLocation() {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocationDialog()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        val token = arrayOf("")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (task.isComplete) {
                    token[0] = task.result
                    Prefrences.savePreferencesString(
                        requireContext(),
                        Constants.FIREBASE_TOKEN,
                        token[0]
                    )
                }
            }
        }

        checkLocation()
        binding.autocomplete.setText("")
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun enableLocationDialog() {
        val dialog = Dialog(requireContext(), R.style.CustomDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.dialog_enable_location_permission)
        val tvDone = dialog.findViewById(R.id.tvDone) as TextView
        tvDone.setOnClickListener {
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
            dialog.dismiss()
        }
        dialog.show()
    }

    var autocompleteClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { parent, view, position, id ->
            var item: AutocompletePrediction? = mAdapter.getItem(position)
            var placeID = item?.placeId
            var placeFields: List<Place.Field> = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            var request: FetchPlaceRequest? = null
            if (placeID != null) {
                request = FetchPlaceRequest.builder(placeID, placeFields).build()
            }

            if (request != null) {
                placesClient.fetchPlace(request).addOnSuccessListener {
                    //                responseView.setText(task.getPlace().getName() + "\n" + task.getPlace().getAddress());
                    val place = it.place
                    var stringBuilder = StringBuilder()
                    stringBuilder.append("Name: ${place.name}\n")
                    var queriedLocation: LatLng? = place.latLng
                    stringBuilder.append("Latitude: ${queriedLocation?.latitude}\n")
                    stringBuilder.append("Longitude: ${queriedLocation?.longitude}\n")
                    stringBuilder.append("Address: ${place.address}\n")

                    mCurrentLat = queriedLocation?.latitude.toString()
                    mCurrentLon = queriedLocation?.longitude.toString()

                    searchDriver(queriedLocation?.latitude, queriedLocation?.longitude)
//                response_view.text = stringBuilder
                    Log.i("TAG", "Called getPlaceById to get Place details for $placeID")

                }.addOnFailureListener {
                    it.printStackTrace()
//                response_view.text = it.message
                }
            }
            requireActivity().hideKeyboard(binding.root)
        }

    private fun searchDriver(latitude: Double?, longitude: Double?) {
        val params = HashMap<String, String>()
        params["latitude"] = latitude.toString()
        params["longitude"] = longitude.toString()
        viewModel.viewModelScope.launch {
            viewModel.searchDriver(params)
        }
    }


    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (managePermissions.checkPermissions()) {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null /* Looper */
                )
            }
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f //170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //according to your app
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    val addresses: List<Address>?
                    val geoCoder = Geocoder(requireContext(), Locale.getDefault())

                    mCurrentLat = locationResult.lastLocation.latitude.toString()
                    mCurrentLon = locationResult.lastLocation.longitude.toString()
                    mapFragment?.getMapAsync(callback)
                    getOffer()

                    Prefrences.savePreferencesString(
                        requireContext(),
                        Constants.LAT,
                        mCurrentLat.toString()
                    )
                    Prefrences.savePreferencesString(
                        requireContext(),
                        Constants.LON,
                        mCurrentLon.toString()
                    )
                }
            }
        }
    }

    private fun setNearestDriverData(result: List<ResultItem?>) {
        driverId = result[0]!!.id.toString()
        driverImage = Constants.STORAGE_URL.plus(result[0]!!.image)
        driverName = result[0]!!.name
        driverRating = result[0]!!.average_rating!!.toString()
        driverTotalReview = if (result[0]!!.total_rating!! == 1) {
            "Review : ".plus(result[0]!!.total_rating.toString())
        } else {
            "Reviews : ".plus(result[0]!!.total_rating.toString())
        }

        binding.tvDriverName.text = driverName
        var services: String? = ""
        for (i in result[0]!!.service!!.indices) {
            services = if (result[0]!!.service!!.isNotEmpty()) {
                result[0]!!.service!![i]!!.name.toString()
            } else {
                result[0]!!.service!![i]!!.name.toString().plus(", ")
            }
        }
        binding.tvDriverServices.text = services
        binding.tvDriverTotalReview.text = driverTotalReview
        loadingImage(
            requireContext(),
            driverImage!!,
            binding.ivDriverImage,
            false
        )

        binding.driverReviewRatingBar.rating = result[0]!!.average_rating!!.toFloat()
        driverAddress = result[0]!!.address
        driverLat = result[0]!!.latitude
        driverLon = result[0]!!.longitude
        mapFragment?.getMapAsync(callback)

    }

    private fun getNearestDriver() {
        viewModel.viewModelScope.launch {
            viewModel.getNearestDriver()
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
            Constants.STORAGE_URL.plus(offer.offerApplyOnService!!.image),
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

    @SuppressLint("HardwareIds")
    private fun getOffer() {
        val params = HashMap<String, String>()
        val deviceId = Secure.getString(requireContext().contentResolver, Secure.ANDROID_ID)
        params["device_id"] = deviceId

        var firebaseToken =
            Prefrences.getPreferences(requireContext(), Constants.FIREBASE_TOKEN).toString()
        if (firebaseToken.isEmpty())
            firebaseToken = "abcdef"

        params["push_token"] = firebaseToken
        params["type"] = "2"

        if (mCurrentLon == null) {
            mCurrentLon = "0.0"
        }
        if (mCurrentLat == null) {
            mCurrentLat = "0.0"
        }
        params["latest_latitude"] = mCurrentLat!!
        params["latest_longitude"] = mCurrentLon!!

        viewModel.viewModelScope.launch {
//            viewModel.getOffer(params)
        }
    }

    private fun setClickListener() {
        binding.tvBookAppointment.setOnClickListener(this)
        binding.relBarber.setOnClickListener(this)
        binding.relNotification.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
    }

    override fun getViewModel() = HomeViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentHomeBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = HomeRepository(
        MyApi.getInstanceToken(
            Prefrences.getPreferences(requireContext(), Constants.API_TOKEN)!!
        )
    )

    private fun setRatingStarColor(drawable: Drawable, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(drawable, color)
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvBookAppointment -> bookAnAppointment()
            R.id.relBarber -> goToReviewsActivity()
            R.id.relNotification -> goToNotificationActivity()
            R.id.ivClear -> binding.autocomplete.setText("")
        }
    }

    private fun goToNotificationActivity() {
        startActivity(Intent(context, NotificationActivity::class.java))
    }

    private fun goToReviewsActivity() {
        startActivity(
            Intent(context, ReviewsActivity::class.java).putExtra(
                Constants.DRIVER_ID,
                driverId
            )
        )
    }

    private fun bookAnAppointment() {
        val intent = Intent(context, AppointmentsActivity::class.java)
        intent.putExtra(Constants.DRIVER_ID, driverId)
        intent.putExtra(Constants.DRIVER_NAME, driverName)
        intent.putExtra(Constants.DRIVER_IMAGE, driverImage)
        intent.putExtra(Constants.DRIVER_RATING, driverRating)
        intent.putExtra(Constants.DRIVER_REVIEW_TOTAL_COUNT, driverTotalReview)
        intent.putExtra(Constants.DRIVER_SERVICE, binding.tvDriverServices.text.toString())
        intent.putExtra(Constants.IS_RESCHEDULE, "no")
        intent.putExtra(Constants.ORDER_LAT, mCurrentLat)
        intent.putExtra(Constants.ORDER_LON, mCurrentLon)
        startActivity(intent)
    }
}