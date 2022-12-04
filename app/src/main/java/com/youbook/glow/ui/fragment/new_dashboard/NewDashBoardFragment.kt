package com.youbook.glow.ui.fragment.new_dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fade.extension.visible
import com.youbook.glow.R
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.base.BaseFragment
import com.android.fade.ui.change_location.ChangeLocationActivity
import com.android.fade.ui.fragment.new_dashboard.*
import com.android.fade.ui.notification.NotificationActivity
import com.youbook.glow.utils.Constants
import com.android.fade.utils.ManagePermissions
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.hide
import com.android.fade.utils.Utils.snackbar
import com.youbook.glow.databinding.FragmentNewDashBoardBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class NewDashBoardFragment(otherAddressLatLon: String) :
    BaseFragment<NewDashboardViewModel, FragmentNewDashBoardBinding, NewDashboardRepository>(),
    View.OnClickListener {

    private var userId: String? = null
    private var otherAddressLatLon: String? = otherAddressLatLon
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var managePermissions: ManagePermissions
    val list = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val permissionsRequestCode = 111

    var filterList = listOf(
        FilterModel(Constants.FILTER_RATING, false),
        FilterModel(Constants.FILTER_PRICE, false),
        FilterModel(Constants.FILTER_DISTANCE, true),
        FilterModel(Constants.FILTER_AVAILABILITY, false)
    )

    private lateinit var servicesAdapter: BarberServicesAdapter
    private lateinit var nearByBarberAdapter: NearByBarberAdapter
    private lateinit var filterAdapter: BarberFilterAdapter
    private lateinit var nearByBarberList: List<NearByBarbersItem>
    private var tempNearByBarberList: List<NearByBarbersItem> = emptyList()
    private lateinit var serviceList: List<ResultItem>
    private val filteredServiceItem = ArrayList<ResultItem>()
    private lateinit var serviceListFilter: List<ServicesItem>
    var newBarberList = ArrayList<NearByBarbersItem>()
    var mCurrentLat: String? = null
    var mCurrentLon: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        managePermissions = ManagePermissions(requireActivity(), list, permissionsRequestCode)

        setUpServicesRecyclerView()
        setOnClickListener()
        getServices()
        setUpBarberRecyclerView()

        viewModel.getDashboardResponse.observe(viewLifecycleOwner) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {

                    if (it.value.result != null) {
                        if (it.value.result.is_read!!) {
                            binding.ivNotificationBadge.visibility = View.VISIBLE
                        } else {
                            binding.ivNotificationBadge.visibility = View.GONE
                        }


                        if (it.value.result.nearByBarbers != null) {
                            for (i in it.value.result.nearByBarbers.indices) {

                                serviceListFilter = it.value.result.nearByBarbers[i]!!.services!!.sortedBy { it!!.service_price } as List<ServicesItem>

                                it.value.result.nearByBarbers[i]!!.services = serviceListFilter
                            }

                            nearByBarberList =
                                (it.value.result.nearByBarbers as List<NearByBarbersItem>?)!!
                            nearByBarberAdapter.updateList(nearByBarberList)
                        }
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.hide()
                    getDashBoardData()
                }
            }
        }

        viewModel.makeBarberFavUnFav.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    binding.root.snackbar(it.value.message!!)
                    getDashBoardData()
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                }
            }
        }

        viewModel.getServiceResponse.observe(viewLifecycleOwner) {
            when (it) {
                is Resource.Success -> {
                    if (it.value.result != null) {
                        serviceList = (it.value.result as List<ResultItem>?)!!
                        servicesAdapter.updateList(serviceList)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                }
            }
        }
    }

    private fun setUpServicesRecyclerView() {
        nearByBarberList  = emptyList()
        binding.serviceRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        servicesAdapter = BarberServicesAdapter(requireContext()) { item ->
            filterBarberBasedOnService(item)
        }
        binding.serviceRecyclerView.adapter = servicesAdapter
    }

    private fun filterBarberBasedOnService(item: ResultItem) {
        val selectedServices = servicesAdapter.getSelectedServices().split(",")

        if (servicesAdapter.getSelectedServices().isEmpty()) {

            if (nearByBarberList.isEmpty()) {
                binding.tvNoBarber.visibility = View.VISIBLE
            } else {
                binding.tvNoBarber.visibility = View.GONE
            }
            nearByBarberAdapter.updateList(nearByBarberList)
        } else {
            newBarberList.clear()
            tempNearByBarberList = newBarberList
            for (barber in nearByBarberList) {
                for (service in barber.services!!) {
                    for (serviceType in selectedServices) {
                        if (serviceType == service!!.service!!.name) {
                            if (!newBarberList.contains(barber)) {
                                newBarberList.add(barber)
                            }
                        }
                    }
                }
            }

            if (newBarberList.isEmpty()) {
                binding.tvNoBarber.visibility = View.VISIBLE
            } else {
                binding.tvNoBarber.visibility = View.GONE
            }

            tempNearByBarberList = newBarberList
            nearByBarberAdapter.updateList(newBarberList)

        }
    }

    private fun setUpBarberRecyclerView() {
        binding.barberRecyclerView.layoutManager = LinearLayoutManager(context)
        nearByBarberAdapter = NearByBarberAdapter(requireContext()) { item, po ->
            makeBarberFavUnFav(item)
        }
        binding.barberRecyclerView.adapter = nearByBarberAdapter
    }

    private fun makeBarberFavUnFav(nearByBarberItem: NearByBarbersItem) {
        userId = Prefrences.getPreferences(requireContext(), Constants.USER_ID)
        val barberId = nearByBarberItem.barber_id
        val params = HashMap<String, String>()
        params["user_id"] = userId!!
        params["barber_id"] = barberId.toString()

        if (nearByBarberItem.is_favourite!!) {
            // if already fav make it un-favourite
            params["type"] = "0"
            nearByBarberItem.is_favourite = false
        } else {
            params["type"] = "1"
            nearByBarberItem.is_favourite = true
        }
        nearByBarberAdapter.notifyDataSetChanged()

        viewModel.viewModelScope.launch {
            viewModel.makeBarberFavUnFav(params)
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

        if (mCurrentLon == null) {
            mCurrentLon = "0.0"
        }
        if (mCurrentLat == null) {
            mCurrentLat = "0.0"
        }
        params["latest_latitude"] = mCurrentLat!!
        params["latest_longitude"] = mCurrentLon!!

        viewModel.viewModelScope.launch {
            viewModel.getDashBoardData(params)
        }
    }

    private fun getDashboardDataWithOtherLatLon(lat: String, lon: String) {
        val params = HashMap<String, String>()
        params["latest_latitude"] = lat
        params["latest_longitude"] = lon
        mCurrentLat = lat
        mCurrentLon = lon

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
        viewModel.viewModelScope.launch {
            viewModel.getDashBoardDataWithOtherLatLon(params)
        }
    }

    private fun getServices() {
        viewModel.viewModelScope.launch {
            viewModel.getServices()
        }
    }

    override fun onResume() {
        super.onResume()
        if (otherAddressLatLon!!.isEmpty()) {
            checkLocation()
            startLocationUpdates()
        } else {
            val latLon = otherAddressLatLon!!.split(",")
            getDashboardDataWithOtherLatLon(latLon[0], latLon[1])
        }
    }

    override fun onPause() {
        super.onPause()

        if (otherAddressLatLon!!.isEmpty())
            stopLocationUpdates()
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // when app open it starts to update location
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

    private fun setOnClickListener() {
        binding.tvFilter.setOnClickListener(this)
        binding.relSelectLocation.setOnClickListener(this)
        binding.relNotification.setOnClickListener(this)

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                arg0: CharSequence, arg1: Int, arg2: Int,
                arg3: Int
            ) {

            }

            override fun beforeTextChanged(
                arg0: CharSequence, arg1: Int,
                arg2: Int, arg3: Int
            ) {

            }

            override fun afterTextChanged(arg0: Editable) {
                searchData(arg0.toString())
            }
        })
    }

    @SuppressLint("DefaultLocale")
    private fun searchData(searchKeyword: String) {

        filteredServiceItem.clear()
        tempNearByBarberList = emptyList()
        newBarberList.clear()

        serviceList.forEach {
            if (it.name!!.toLowerCase().contains(searchKeyword.toLowerCase())) {
                filteredServiceItem.add(it)
            }
        }

        for (barber in nearByBarberList) {
            if (barber.name!!.toLowerCase().contains(searchKeyword.toLowerCase())) {
                newBarberList.add(barber)
            }
            for (service in barber.services!!) {
                if (service!!.service!!.name!!.toLowerCase()
                        .contains(searchKeyword.toLowerCase())
                ) {
                    if (!newBarberList.contains(barber))
                        newBarberList.add(barber)
                }
            }
        }

        tempNearByBarberList = newBarberList
        nearByBarberAdapter.updateList(tempNearByBarberList)
        servicesAdapter.updateList(filteredServiceItem)
    }

    private fun checkLocation() {
        val manager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enableLocationDialog()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getLocationUpdates()
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

                    getDashBoardData()

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

    private fun setUpBottomFilterPopup() {

        val mBottomSheetDialog = Dialog(requireContext(), R.style.MaterialDialogSheet)

        val bottomSheetLayout: View =
            layoutInflater.inflate(R.layout.layout_bottom_filter_sheet, null)
        bottomSheetLayout.layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        // your custom view
        mBottomSheetDialog.setContentView(R.layout.layout_bottom_filter_sheet)
        val filterRecyclerView =
            mBottomSheetDialog.findViewById<RecyclerView>(R.id.filterRecyclerview)

        mBottomSheetDialog.setCancelable(true)
        mBottomSheetDialog.window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        mBottomSheetDialog.window!!.setGravity(Gravity.BOTTOM)
        mBottomSheetDialog.show()

        filterAdapter = BarberFilterAdapter(requireContext()) { item: FilterModel, po: Int ->
            mBottomSheetDialog.cancel()
            filterBarberData(item)
        }

        filterRecyclerView.adapter = filterAdapter
        filterRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        filterAdapter.updateList(filterList)
    }

    private fun filterBarberData(item: FilterModel) {
        when (item.filter) {
            Constants.FILTER_AVAILABILITY -> {
                if (tempNearByBarberList.isNotEmpty()) {
                    tempNearByBarberList = tempNearByBarberList.sortedBy { it.is_available == 1 }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                } else if (servicesAdapter.getSelectedServices().isEmpty()) {
                    tempNearByBarberList = nearByBarberList.filter { it.is_available == 1 }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                    tempNearByBarberList = emptyList()
                }
            }
            Constants.FILTER_DISTANCE -> {

                if (tempNearByBarberList.isNotEmpty()) {
                    tempNearByBarberList = tempNearByBarberList.sortedBy { it.distance }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                } else if (servicesAdapter.getSelectedServices().isEmpty()) {
                    tempNearByBarberList = nearByBarberList.sortedBy { it.distance }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                    tempNearByBarberList = emptyList()
                }
            }

            Constants.FILTER_PRICE -> {
                if (tempNearByBarberList.isNotEmpty()) {
                    tempNearByBarberList =
                        tempNearByBarberList.sortedBy { it.services!![0]!!.service_price }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                } else if (servicesAdapter.getSelectedServices().isEmpty()) {
                    tempNearByBarberList =
                        nearByBarberList.sortedBy { it.services!![0]!!.service_price }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                    tempNearByBarberList = emptyList()
                }
            }

            Constants.FILTER_RATING -> {
                if (tempNearByBarberList.isNotEmpty()) {
                    tempNearByBarberList = tempNearByBarberList.sortedByDescending { it.average_rating }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                } else if (servicesAdapter.getSelectedServices().isEmpty()) {
                    tempNearByBarberList = nearByBarberList.sortedByDescending { it.average_rating }
                    nearByBarberAdapter.updateList(tempNearByBarberList)
                    tempNearByBarberList = emptyList()
                }
            }
        }
    }

    override fun getViewModel() = NewDashboardViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentNewDashBoardBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = NewDashboardRepository(
        MyApi.getInstanceToken(
            Prefrences.getPreferences(requireContext(), Constants.API_TOKEN)!!
        )
    )

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tvFilter -> setUpBottomFilterPopup()
            R.id.relSelectLocation -> goToChangeLocationScreen()
            R.id.relNotification -> goToNotificationActivity()
        }
    }

    private fun goToNotificationActivity() {
        startActivity(Intent(context, NotificationActivity::class.java))
    }

    private fun goToChangeLocationScreen() {
        val intent = Intent(context, ChangeLocationActivity::class.java)
        startActivity(intent)
    }

}