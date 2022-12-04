package com.youbook.glow.ui.get_location

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.get_location.DirectionPointListener
import com.android.fade.ui.get_location.GetPathFromLocation
import com.android.fade.ui.new_add_review.NewAddReviewRepository
import com.android.fade.ui.new_add_review.NewAddReviewViewModel
import com.youbook.glow.ui.new_add_review.NewAddReviewViewModelFactory
import com.android.fade.ui.search_place.AutoCompleteAdapter
import com.youbook.glow.utils.Constants
import com.android.fade.utils.ManagePermissions
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.youbook.glow.databinding.ActivityGetLocationBinding
import com.google.android.gms.location.*
import com.youbook.glow.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_search_place.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors


class GetLocationActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityGetLocationBinding
    private lateinit var viewModel: NewAddReviewViewModel
    var mCurrentLat: String? = null
    var mCurrentLon: String? = null
    var isStart: Boolean? = true
    var orderId: String? = ""
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var managePermissions: ManagePermissions
    private val permissionsRequestCode = 111
    lateinit var placesClient: PlacesClient
    private var userCurrentAddress: String? = ""
    private var from: String? = ""
    lateinit var mAdapter: AutoCompleteAdapter
    var mapFragment: SupportMapFragment? = null

    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    val list = listOf<String>(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment!!.getMapAsync(this@GetLocationActivity)
        managePermissions = ManagePermissions(this, list, permissionsRequestCode)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        viewModel = ViewModelProvider(
            this,
            NewAddReviewViewModelFactory(
                NewAddReviewRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(this, Constants.API_TOKEN)!!
                    )
                )
            )
        ).get(NewAddReviewViewModel::class.java)

        //driver latest location handler
        viewModel.driverLatestLocation.observe(this) {

            // mMap.clear()
            when (it) {
                is Resource.Success -> {
                    val latUser = it.value.result?.latitude?.toDouble()
                    val longUser = it.value.result?.longitude?.toDouble()
                    if (isStart as Boolean) {
                        animateMap(latUser!!, longUser!!)
                    }

                    val latDriver = it.value.result?.barber?.latestLatitude?.toDouble()
                    val longDriver = it.value.result?.barber?.latestLongitude?.toDouble()

                    var origin = LatLng(latUser!!, longUser!!)
                    var dest = LatLng(latDriver!!, longDriver!!)

                    GetPathFromLocation(origin, dest, object : DirectionPointListener {
                        override fun onPath(polyLine: PolylineOptions?) {
                            isStart = false
                            if (polyLine != null) {
                                mMap.addPolyline(polyLine)
                            }
                        }
                    }).execute()

                    val options = MarkerOptions()
                    val options1 = MarkerOptions()
                    options.position(origin)
                    options1.position(dest)
                    mMap.clear()
                    mMap.addMarker(options)
                    mMap.addMarker(options1)
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    //Log.d("TAG", "Driver Location Error: ${it.errorBody.toString()}")7 99
                }

            }
        }

        // Initialize Places.
        var apiKey = getString(R.string.place_api_key)
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey)
        }
        placesClient = Places.createClient(this)

        userCurrentAddress = intent.getStringExtra(Constants.USER_ADDRESS)

        if (intent.getStringExtra(Constants.FROM) != null) {
            from = intent.getStringExtra(Constants.FROM)
            orderId = intent.getStringExtra(Constants.ORDER_ID)
        }
        if (userCurrentAddress!!.isNotEmpty()) {
            binding.autocomplete.setText(userCurrentAddress.toString())
        }
        if (from!!.isNotEmpty()) {
            // show root
            binding.topRelative.visibility = View.GONE
            binding.tvDone.visibility = View.GONE
            binding.ivMapLocationPin.visibility = View.GONE
            binding.ivBackButton.visibility = View.VISIBLE

            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    viewModel.viewModelScope.launch {
                        viewModel.driverLatestLocation(orderId.toString())
                    }

                }
            }, 0, 10000)

        } else {
            // show place picker.
            binding.topRelative.visibility = View.VISIBLE
            binding.tvDone.visibility = View.VISIBLE
            binding.ivMapLocationPin.visibility = View.VISIBLE
            binding.ivBackButton.visibility = View.GONE
            // startLocationUpdates()
            setUpAutoCompleteTextView()

        }
        clickListener()
    }

    private fun setUpAutoCompleteTextView() {
        autocomplete.threshold = 1
        autocomplete.onItemClickListener = autocompleteClickListener
        mAdapter = AutoCompleteAdapter(this, placesClient)
        autocomplete.setAdapter(mAdapter)
    }

    private fun checkLocation() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertLocation()
        }
        getLocationUpdates()
    }

    private fun clickListener() {
        binding.tvCancel.setOnClickListener(this)
        binding.tvDone.setOnClickListener(this)
        binding.ivClear.setOnClickListener(this)
        binding.ivBackButton.setOnClickListener(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val currentLatLon: LatLng
        if (mCurrentLat != null && mCurrentLon != null) {
            currentLatLon = LatLng(mCurrentLat!!.toDouble(), mCurrentLon!!.toDouble())
        } else {
            currentLatLon = LatLng(51.4029, 0.1667)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLon, 14F))

        mMap.setOnCameraIdleListener {
            //get latlng at the center by calling
            val midLatLng: LatLng = mMap.cameraPosition.target
            mCurrentLat = midLatLng.latitude.toString()
            mCurrentLon = midLatLng.longitude.toString()
            binding.autocomplete.setText(
                Utils.getAddress(midLatLng.latitude, midLatLng.longitude, this).toString()
            )
        }
    }

    fun animateMap(latitude: Double, longitude: Double) {
        val currentLatLon = LatLng(latitude, longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLon, 18F))
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
                    binding.autocomplete.setText(
                        Utils.getAddress(
                            queriedLocation?.latitude!!,
                            queriedLocation.longitude,
                            this
                        ).toString()
                    )
                    mCurrentLat = queriedLocation.latitude.toString()
                    mCurrentLon = queriedLocation.longitude.toString()

                    animateMap(queriedLocation.latitude, queriedLocation.longitude)
                    //searchDriver(queriedLocation?.latitude, queriedLocation?.longitude)
//                response_view.text = stringBuilder
                    Log.i("TAG", "Called getPlaceById to get Place details for $placeID")

                }.addOnFailureListener {
                    it.printStackTrace()
//                response_view.text = it.message
                }
            }
            hideKeyboard(binding.root)
        }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Your location settings is set to Off, Please enable location to get location")
        dialog.setPositiveButton("Settings") { _, _ ->
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
        dialog.setNegativeButton("Cancel") { _, _ ->
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        //stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (managePermissions.checkPermissions()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null  /*Looper*/
                )
            }
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 50000
        locationRequest.fastestInterval = 50000
        locationRequest.smallestDisplacement = 170f //170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //according to your app
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                if (locationResult.locations.isNotEmpty()) {
                    /*val location = locationResult.lastLocation
                    Log.e("location", location.toString())*/
                    val addresses: List<Address>?
                    val geoCoder = Geocoder(applicationContext, Locale.getDefault())

                    mCurrentLat = locationResult.lastLocation.latitude.toString()
                    mCurrentLon = locationResult.lastLocation.longitude.toString()

                    addresses = geoCoder.getFromLocation(
                        locationResult.lastLocation.latitude,
                        locationResult.lastLocation.longitude,
                        1
                    )
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvCancel -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            R.id.ivClear -> {
                binding.autocomplete.setText("")
            }
            R.id.tvDone -> {
                val intent = Intent()
                intent.putExtra("ADDRESS", binding.autocomplete.text.toString())
                intent.putExtra(Constants.LATEST_LAT, mCurrentLat)
                intent.putExtra(Constants.LATEST_LON, mCurrentLon)
                setResult(RESULT_OK, intent)
                finish()
            }
            R.id.ivBackButton -> {
                finish()
            }
        }
    }
}