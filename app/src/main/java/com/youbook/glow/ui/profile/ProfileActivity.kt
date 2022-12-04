package com.youbook.glow.ui.profile
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.fade.extension.loadingImage
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.youbook.glow.ui.get_location.GetLocationActivity
import com.android.fade.ui.profile.ProfileRepository
import com.android.fade.ui.profile.Result
import com.android.fade.utils.*
import com.android.fade.utils.Utils.handleApiError
import com.android.fade.utils.Utils.hide
import com.android.fade.utils.Utils.snackbar
import com.android.fade.utils.Utils.toast
import com.android.fade.utils.Utils.visible
import com.bumptech.glide.Glide
import com.youbook.glow.MainActivity
import com.youbook.glow.databinding.ActivityProfileBinding
import com.youbook.glow.utils.FileUtil
import com.google.android.gms.location.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.util.ToastUtil
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.youbook.glow.R
import com.youbook.glow.utils.Constants
import gun0912.tedimagepicker.util.ToastUtil.context

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private val _tag = "ProfileActivity"
    private var mCurrentPhotoPath: String? = null
    private lateinit var binding: ActivityProfileBinding
    private lateinit var viewModel: ProfileViewModel
    var mobileNumber: String? = null
    var isFirstTime: String? = null
    private val _requestTakePhoto = 1
    private val _requestGetPhoto = 2
    private var uriTemp: Uri? = null
    private val _permissionsRequestCode = 111
    private val _storagePermissionsRequestCode = 222
    var mCurrentLat: String? = null
    var mCurrentLon: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var managePermissions: ManagePermissions
    private lateinit var managePermissions2: ManagePermissions
    private var _selectedGender = "Male"

    val list = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val list2 = listOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvCountryCode.text = Prefrences.getPreferences(context, Constants.SELECTED_COUNTRY_CODE)

        managePermissions = ManagePermissions(this, list, _permissionsRequestCode)
        managePermissions2 = ManagePermissions(this, list2, _storagePermissionsRequestCode)

        viewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(
                ProfileRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(
                            this,
                            Constants.API_TOKEN
                        )!!
                    )
                )
            )
        ).get(ProfileViewModel::class.java)

        checkLocation()

        isFirstTime = intent.getStringExtra(Constants.IS_FIRST_TIME)
        mobileNumber = intent.getStringExtra(Constants.USER_MOBILE_NO)
        binding.edtPhoneNumber.setText(mobileNumber)
        startLocationUpdates()
        setClickListener()
        if (isFirstTime.equals("YES")) {
            binding.ivBackButton.visibility = View.GONE
            binding.tvEditProfile.text = getString(R.string.save_profile)
            binding.edtEmailAddress.isEnabled = true
        } else {
            binding.ivBackButton.visibility = View.VISIBLE
            binding.tvEditProfile.text = getString(R.string.edit_profile)
            binding.edtEmailAddress.isEnabled = false
            setUserData()
        }

        viewModel.updateProfileResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    if (it.value.result != null) {
                        savePreferences(it.value.result)
                        binding.progressBar.hide()
                        if (isFirstTime.equals("YES")) {
                            val i = Intent(this@ProfileActivity, MainActivity::class.java)
                            i.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                            finish()
                        } else {
                            binding.root.snackbar(it.value.message!!)
                        }
                    } else {
                        binding.root.snackbar(it.value.message!!)
                    }
                }
                is Resource.Failure -> handleApiError(binding.root, it) {
                    addProfileOrEditProfile()
                }
                else -> {

                }
            }

        }
    }

    private fun setUserData() {
        val name = Prefrences.getPreferences(application, Constants.USER_NAME)
        val image = Prefrences.getPreferences(application, Constants.USER_PROFILE_IMAGE)
        val email = Prefrences.getPreferences(application, Constants.EMAIL_ID)
        val mobile = Prefrences.getPreferences(application, Constants.USER_MOBILE_NO)
        val address = Prefrences.getPreferences(application, Constants.USER_ADDRESS)

        loadingImage(this, image!!, binding.ivUserProfile, true)
        binding.edtUserName.setText(name)
        binding.edtEmailAddress.setText(email)
        binding.edtPhoneNumber.setText(mobile)
        binding.edtAddress.setText(address)

        viewModel.profilePic.value = image
        viewModel.userName.value = name

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.ivUserProfile -> {
                if (managePermissions2.checkPermissions()) {
                    chooseImage()
                }
            }
            R.id.tvEditProfile -> {
                addProfileOrEditProfile()
            }
            R.id.edtAddress -> {
                if (Utils.isConnected(this)) {
                    val intent = Intent(this, GetLocationActivity::class.java)
                    intent.putExtra(Constants.USER_ADDRESS, binding.edtAddress.text.toString())
                    resultLauncher.launch(intent)

                } else {
                    binding.root.snackbar(
                        getString(R.string.no_internet_connection)
                    )
                }

            }
        }
    }

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            val address = data!!.getStringExtra("ADDRESS").toString()
            mCurrentLat = data.getStringExtra(Constants.LATEST_LAT).toString()
            mCurrentLon = data.getStringExtra(Constants.LATEST_LON).toString()
            binding.edtAddress.setText(address)
        }
    }

    private fun checkLocation() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showAlertLocation()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLocationUpdates()
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
                    null /* Looper */
                )
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            _permissionsRequestCode -> {
                val isPermissionsGranted = managePermissions
                    .processPermissionsResult(requestCode, permissions, grantResults)
                if (isPermissionsGranted) {
                    startLocationUpdates()
                } else {
                    this.toast("Permissions denied.")
                }
                return
            }
            _storagePermissionsRequestCode -> {
                chooseImage()
            }
        }
    }

    // Stop location updates
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun showAlertLocation() {
        val dialog = AlertDialog.Builder(this)
        dialog.setMessage("Your location settings is set to Off, Please enable location to use this application")
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
        stopLocationUpdates()

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
                    mCurrentLat = locationResult.lastLocation.latitude.toString()
                    mCurrentLon = locationResult.lastLocation.longitude.toString()

                }
            }

        }
    }

    private fun savePreferences(data: Result) {
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.USER_ID,
            data.id.toString()
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.ROLE_ID,
            data.role_id!!.toString()
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.USER_MOBILE_NO,
            data.mobile!!.toString()
        )

        if (data.token != null && data.token.isNotEmpty()) {
            Prefrences.savePreferencesString(this@ProfileActivity, Constants.API_TOKEN, data.token)
        }

        Prefrences.savePreferencesString(this@ProfileActivity, Constants.USER_NAME, data.name!!)

        Prefrences.savePreferencesString(this@ProfileActivity, Constants.EMAIL_ID, data.email!!)
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.USER_ADDRESS,
            data.address_line_1!! + " " + data.address_line_2
        )
        Prefrences.savePreferencesString(this@ProfileActivity, Constants.LAT, data.latitude!!)
        Prefrences.savePreferencesString(this@ProfileActivity, Constants.LON, data.longitude!!)
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.LATEST_LAT,
            data.latest_latitude!!
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.LATEST_LON,
            data.latest_longitude!!
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.VAN_NUMBER,
            data.van_number!!
        )
        Prefrences.savePreferencesString(this@ProfileActivity, Constants.USER_GENDER, data.gender!!)
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.PROFILE_APPROVED,
            data.profile_approved!!.toString()
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.IS_ACTIVE,
            data.is_active!!.toString()
        )
        Prefrences.savePreferencesString(
            this@ProfileActivity,
            Constants.USER_PROFILE_IMAGE,
            Constants.STORAGE_URL.plus(data.profile!!)
        )

        /*viewModel.image = data.image*/

        viewModel.profilePic.value = data.profile
        viewModel.userName.value = data.name

        Log.e(_tag, "savePreferences: " + Gson().toJson(data))
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.ivUserProfile.setOnClickListener(this)
        binding.tvEditProfile.setOnClickListener(this)
        binding.edtAddress.setOnClickListener(this)

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            val rb = findViewById<View>(checkedId) as RadioButton
            _selectedGender = rb.text.toString()
        }
    }

    private fun addProfileOrEditProfile() {

        if (binding.tvEditProfile.text.equals("Edit Profile")) {
            val params = HashMap<String, String>()
            val parts = ArrayList<MultipartBody.Part>()
            val body: MultipartBody.Part?
            val name = binding.edtUserName.text.toString().trim()
            val address = binding.edtAddress.text.toString().trim()

            if (name.isEmpty()) {
                binding.root.snackbar("Please enter name")
            } else if (address.isEmpty()) {
                binding.root.snackbar("Please enter address")
            } else {

                if (mCurrentPhotoPath != null) {
                    val imagePath: String = Utils.compressImage(this@ProfileActivity, mCurrentPhotoPath!!).toString()
                    body = prepareFilePart("profile", imagePath)
                    parts.add(body)
                    params["name"] = name
                    params["address"] = address
                    params["latitude"] = mCurrentLat!!
                    params["longitude"] = mCurrentLon!!

                    viewModel.viewModelScope.launch { viewModel.updateProfile(parts, params) }

                } else {
                    params["name"] = name
                    params["address"] = address
                    params["latitude"] = mCurrentLat!!
                    params["longitude"] = mCurrentLon!!

                    viewModel.viewModelScope.launch {
                        viewModel.updateProfileWithoutPhoto(params)
                    }
                }
            }

        } else {
            val params = HashMap<String, String>()
            val parts = ArrayList<MultipartBody.Part>()
            val body: MultipartBody.Part?
            val name = binding.edtUserName.text.toString().trim()
            val email = binding.edtEmailAddress.text.toString().trim()
            val address = binding.edtAddress.text.toString().trim()
            if (name.isEmpty()) {
                binding.root.snackbar("Please enter name")
            } else if (email.isEmpty() || !Utils.isEmailValid(email)) {
                binding.root.snackbar("Please enter valid email id")
            } else if (address.isEmpty()) {
                binding.root.snackbar("Please enter address")
            } else if (mCurrentPhotoPath == null) {
                binding.root.snackbar("Please select profile image")
            } else {
                if (mCurrentPhotoPath != null) {
                    val imagePath: String =
                        Utils.compressImage(this@ProfileActivity, mCurrentPhotoPath!!).toString()
                    body = prepareFilePart("profile", imagePath)
                    parts.add(body)
                }

                params["phone_code"] = Prefrences.getPreferences(context, Constants.SELECTED_COUNTRY_CODE)!!
                params["country_id"] = Prefrences.getPreferences(context, Constants.PREFS_CODE)!!

                params["mobile"] = mobileNumber.toString()
                params["name"] = name
                params["email"] = email
                params["role_id"] = "3"
                params["address"] = address
                params["latitude"] = mCurrentLat!!
                params["longitude"] = mCurrentLon!!

                viewModel.viewModelScope.launch {
                    viewModel.addProfile(parts, params)
                }
            }
        }

    }

    private fun chooseImage() {
        val btnsheet = layoutInflater.inflate(R.layout.choose_image_dialog, null)
        val dialog = BottomSheetDialog(this)
        val tvCaptureImage: TextView = btnsheet.findViewById(R.id.tvCaptureImage)
        val tvUploadImage: TextView = btnsheet.findViewById(R.id.tvUploadImage)
        dialog.setContentView(btnsheet)
        tvCaptureImage.setOnClickListener {
            takePicture()
            dialog.cancel()
        }
        tvUploadImage.setOnClickListener {
            TedImagePicker.with(this)
                .start { uri -> showSingleImage(uri) }
            dialog.cancel()
        }
        dialog.show()
    }

    private fun showSingleImage(uri: Uri) {
        mCurrentPhotoPath = FileUtil.getPath(ToastUtil.context, uri)
        Glide.with(ToastUtil.context).load(mCurrentPhotoPath).into(binding.ivUserProfile)
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File?
            photoFile = createImageFile()
            if (photoFile != null) {
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider", photoFile
                    )
                )
                startActivityForResult(takePictureIntent, _requestTakePhoto)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        var storageDir: File? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storageDir = File(
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                Constants.DirectoryName
            )
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                storageDir = File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    Constants.DirectoryName
                )
            }
        }

        if (!storageDir!!.exists()) {
            val mkdir = storageDir.mkdirs()
            if (!mkdir) {
                Log.e("TAG", "Directory creation failed.")
            } else {
                Log.e("TAG", "Directory created.")
            }

        }
        val file = File(storageDir.path.toString() + File.separator + imageFileName)
        mCurrentPhotoPath = file.absolutePath
        return file
    }

    private fun prepareFilePart(partName: String, fileUri: String): MultipartBody.Part {
        val file = File(fileUri)
        // create RequestBody instance from file
        val requestFile: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                _requestTakePhoto -> {
                    val f = File(mCurrentPhotoPath!!)
                    uriTemp = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider",
                        f
                    )

                    cropImage(uriTemp!!)
                }

                _requestGetPhoto -> {
                    uriTemp = data?.data

                    cropImage(uriTemp!!)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    mCurrentPhotoPath = Utils.getRealPathFromURI(this, result.uri)
                    val file = File(mCurrentPhotoPath!!)
                    Glide.with(application)
                        .load(file)
                        .placeholder(R.drawable.ic_avtar_placeholder)
                        .error(R.drawable.ic_avtar_placeholder)
                        .into(binding.ivUserProfile)
                }
            }
        }
    }

    fun cropImage(uriTemp: Uri) {
        CropImage.activity(uriTemp)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .start(this)
    }
}