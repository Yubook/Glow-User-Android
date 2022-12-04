package com.youbook.glow.ui.new_add_review

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.fade.extension.loadingImage
import com.android.fade.network.MyApi
import com.android.fade.network.Resource
import com.android.fade.ui.add_review.ImageAdapter
import com.android.fade.ui.chat.ChatActivity
import com.android.fade.ui.new_add_review.NewAddReviewRepository
import com.android.fade.ui.new_add_review.NewAddReviewViewModel
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.enable
import com.android.fade.utils.Utils.snackbar
import com.android.fade.utils.Utils.toast
import com.android.fade.utils.Utils.visible
import com.youbook.glow.R
import com.youbook.glow.ui.get_location.GetLocationActivity
import com.youbook.glow.utils.FileUtil
import com.youbook.glow.databinding.ActivityNewAddReviewBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.util.ToastUtil.context
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NewAddReviewActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mCurrentPhotoPath: String
    private lateinit var binding: ActivityNewAddReviewBinding
    private lateinit var viewModel: NewAddReviewViewModel
    private lateinit var barberId: String
    private lateinit var userId: String
    private var serviceRatingEnable = false
    var hygieneRatingEnable = false
    var valueRatingEnable = false
    private var isBarberFav = false
    private var orderLat: String = ""
    private var orderLon: String = ""
    private var orderId: String = ""
    var mapFragment: SupportMapFragment? = null
    private var myGoogleMap: GoogleMap? = null
    private val requestTakePhoto = 1
    private val requestGetPhoto = 2
    private lateinit var imageAdapter: ImageAdapter
    private val arrayListImage = ArrayList<String>()
    private var uriTemp: Uri? = null
    private var serviceRate: Float = 0f
    private var hygieneRate: Float = 0f
    private var valueRate: Float = 0f
    private var reviewId: String = ""
    private var isOrderComplete = ""
    private var reviewImages = ""
    private var barberDistance: Double = 0.0
    private var isChatEnable: Boolean? = false
    private var groupId: String? = ""
    private var barberName: String = ""
    private var orderType: String = ""
    private var orderStatus = 0
    private var currentTime = Calendar.getInstance().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewAddReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        mapFragment = supportFragmentManager.findFragmentById(R.id.myMap) as SupportMapFragment?

        binding.imageRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        imageAdapter = ImageAdapter(this, arrayListImage)
        binding.imageRecyclerView.adapter = imageAdapter

        userId = Prefrences.getPreferences(context, Constants.USER_ID)!!
        getOrderDetails()
        setOnClickListener()
        getDriverLatestLocation()

        viewModel.addReviewResponse.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            binding.tvAddReview.visible(true)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    binding.tvAddReview.visible(true)
                    if (it.value.success!!) {
                        this.toast(it.value.message!!)
                        finish()
                    } else {
                        this.toast(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.visible(false)
                    binding.tvAddReview.visible(true)
                }
            }
        }

        viewModel.makeBarberFavUnFav.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    if (it.value.success!!) {
                        if (!isBarberFav) {
                            binding.ivFavourite.setImageResource(R.drawable.ic_fav_barber)
                        } else {
                            binding.ivFavourite.setImageResource(R.drawable.ic_unfav_barber)
                        }
                        isBarberFav = !isBarberFav
                    } else {
                        this.toast(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.visible(false)
                }
            }
        }

        viewModel.driverLatestLocation.observe(this) {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    Log.d(
                        "TAG",
                        "Driver Location: Driver Latest Location Get successfully" + it.value
                    )
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.visible(false)
                    //Log.d("TAG", "Driver Location Error: ${it.errorBody.toString()}")
                }
            }
        }

        Log.d("TAG", "onCreate: ${currentTime}")

        val sdf = SimpleDateFormat("EEEE, d MMM yyyy hh:mm a")

        val currentTime: String = sdf.format(Date())
        val c1 = Calendar.getInstance()
        c1.time = sdf.parse(currentTime)

        var orderTime = intent.getStringExtra(Constants.BOOKED_DATE)!!.replace("@ ", "")
        val c = Calendar.getInstance()
        c.time = sdf.parse(orderTime)
        c.add(Calendar.MINUTE, -30)
        Log.d("TAG", "onCreate c : ${c.time}")
            // c.add(Calendar.HOUR, -c1.time.hours)
        Log.d("TAG", "onCreate c1 : ${c1.time}")

        try {
            val date1 = c.time
            val date2 = c1.time

            val timeDiff = date2.time - date1.time

            val diffDate = Date(timeDiff).time

            val timeDiffString = sdf.format(diffDate)

            val minutes: Long = TimeUnit.MILLISECONDS.toMinutes(diffDate)

            println("Time Diff = $timeDiffString")

            if(minutes in 1..30){
                binding.tvTrackDriver.visibility = View.VISIBLE
            }else{
                binding.tvTrackDriver.visibility = View.GONE
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun getDriverLatestLocation() {
        viewModel.viewModelScope.launch {
            viewModel.driverLatestLocation(orderId)
        }
    }

    private fun getOrderDetails() {
        barberId = intent.getStringExtra(Constants.DRIVER_ID)!!
        orderId = intent.getStringExtra(Constants.ORDER_ID)!!
        orderType = intent.getStringExtra(Constants.ORDER_TYPE)!!

        barberName = intent.getStringExtra(Constants.DRIVER_NAME).toString()
        binding.tvBarberName.text = barberName
        loadingImage(
            context, intent.getStringExtra(Constants.DRIVER_IMAGE).toString(),
            binding.ivUserProfile,
            true
        )
        binding.tvBookedService.text = intent.getStringExtra(Constants.BOOKED_SERVICE)
        binding.tvBookingDate.text = intent.getStringExtra(Constants.BOOKED_DATE)
        binding.tvTotalPrice.text = intent.getStringExtra(Constants.DRIVER_SERVICE_PRICE)
        binding.tvAddress.text = intent.getStringExtra(Constants.ORDER_ADDRESS)
        orderLat = intent.getStringExtra(Constants.ORDER_LAT)!!
        orderLon = intent.getStringExtra(Constants.ORDER_LON)!!
        isOrderComplete = intent.getStringExtra(Constants.IS_ORDER_COMPLETE)!!
        reviewImages = intent.getStringExtra(Constants.REVIEW_IMAGE)!!
        barberDistance = intent.getDoubleExtra(Constants.BARBER_DISTANCE, 0.0)

        isChatEnable = intent.getBooleanExtra(Constants.IS_CHAT_ENABLE, false)
        groupId = intent.getStringExtra(Constants.GROUP_ID)
        orderStatus = intent.getIntExtra(Constants.ORDER_STATUS, 0)

        // 0 incomplete 1 completed 2 Rejected

        if (isChatEnable!!) {
            binding.ivChat.enable(true)
            binding.llDisableMap.visibility = View.GONE
            binding.relMap1.visibility = View.VISIBLE
        } else {
            binding.ivChat.enable(false)
            binding.llDisableMap.visibility = View.VISIBLE
            binding.relMap1.visibility = View.GONE
        }

        val milesAway = String.format("%.2f", barberDistance) + " miles away"
        binding.tvBarberDistance.text = milesAway
        val distance = "Get Directions - " + String.format("%.2f", barberDistance) + " mile"
        binding.tvDistance.text = distance

        val split = reviewImages.split(",")
        isBarberFav = intent.getBooleanExtra(Constants.IS_BARBER_FAV, false)

        if (split.isNotEmpty()) {
            split.forEach {
                if (it != "") {
                    arrayListImage.add(Constants.STORAGE_URL + it)
                }
            }
        }

        if (isBarberFav) {
            binding.ivFavourite.setImageResource(R.drawable.ic_fav_barber)
        } else {
            binding.ivFavourite.setImageResource(R.drawable.ic_unfav_barber)
        }

        reviewId = intent.getStringExtra(Constants.REVIEW_ID).toString()
        if (reviewId != "" && reviewId != "null") {
            serviceRate = intent.getStringExtra(Constants.SERVICE_RATING).toString().toFloat()
            valueRate = intent.getStringExtra(Constants.VALUE_RATING).toString().toFloat()
            hygieneRate = intent.getStringExtra(Constants.HYGIENE_RATING).toString().toFloat()

            binding.serviceRatingBar.setIsIndicator(true)
            binding.valueRatingBar.setIsIndicator(true)
            binding.hygieneRatingBar.setIsIndicator(true)

            binding.serviceRatingBar.rating = serviceRate
            binding.valueRatingBar.rating = valueRate
            binding.hygieneRatingBar.rating = hygieneRate

            // if already review given than hide add review button
            binding.tvAddReview.visibility = View.GONE
            binding.relAddImage.visibility = View.GONE

            if (arrayListImage.isEmpty()) {
                binding.photos.text = getString(R.string.no_photos_found)
            } else {
                binding.photos.text = getString(R.string.photos)
            }
        } else {
            binding.tvAddReview.visibility = View.VISIBLE
            binding.relAddImage.visibility = View.VISIBLE

            binding.serviceRatingBar.setIsIndicator(false)
            binding.valueRatingBar.setIsIndicator(false)
            binding.hygieneRatingBar.setIsIndicator(false)
        }

        if (isOrderComplete == "1") {
            binding.photos.visibility = View.VISIBLE
            binding.relImage.visibility = View.VISIBLE
            binding.relReview.visibility = View.VISIBLE
        } else {
            binding.photos.visibility = View.GONE
            binding.relImage.visibility = View.GONE
            binding.relReview.visibility = View.GONE

        }
        mapFragment?.getMapAsync(callback)
    }

    private fun setOnClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvEditServiceRating.setOnClickListener(this)
        binding.tvEditHygieneRating.setOnClickListener(this)
        binding.tvEditValueRating.setOnClickListener(this)
        binding.ivFavourite.setOnClickListener(this)
        binding.relAddImage.setOnClickListener(this)
        binding.tvAddReview.setOnClickListener(this)
        binding.ivChat.setOnClickListener(this)
        binding.relMap1.setOnClickListener(this)
        binding.tvTrackDriver.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.ivChat -> openChatActivity()
            R.id.relMap1 -> drawPath()
            R.id.tvEditServiceRating -> {
                serviceRatingEnable = !serviceRatingEnable
                binding.serviceRatingBar.setIsIndicator(serviceRatingEnable)
                val drawableReview: Drawable = binding.serviceRatingBar.progressDrawable
                if (!serviceRatingEnable) {
                    drawableReview.setColorFilter(
                        Color.parseColor("#222327"),
                        PorterDuff.Mode.SRC_ATOP
                    )

                } else {
                    drawableReview.setColorFilter(
                        Color.parseColor("#9B9B9B"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }

            }
            R.id.tvEditHygieneRating -> {
                hygieneRatingEnable = !hygieneRatingEnable
                binding.hygieneRatingBar.setIsIndicator(hygieneRatingEnable)
                val drawableReview: Drawable = binding.hygieneRatingBar.progressDrawable
                if (!hygieneRatingEnable) {
                    drawableReview.setColorFilter(
                        Color.parseColor("#222327"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    drawableReview.setColorFilter(
                        Color.parseColor("#9B9B9B"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
            R.id.tvEditValueRating -> {
                valueRatingEnable = !valueRatingEnable
                binding.valueRatingBar.setIsIndicator(valueRatingEnable)
                val drawableReview: Drawable = binding.valueRatingBar.progressDrawable
                if (!valueRatingEnable) {
                    drawableReview.setColorFilter(
                        Color.parseColor("#222327"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                } else {
                    drawableReview.setColorFilter(
                        Color.parseColor("#9B9B9B"),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
            R.id.ivFavourite -> makeBarberFavUnFav()
            R.id.relAddImage -> chooseImage()
            R.id.tvAddReview -> addReview()
            R.id.tvTrackDriver -> openDriverLocation()
        }
    }

    private fun openDriverLocation() {
        val intent = Intent(this, GetLocationActivity::class.java)
        intent.putExtra(Constants.USER_ADDRESS, "")
        intent.putExtra(Constants.FROM, "OrderDetail")
        intent.putExtra(Constants.ORDER_ID, orderId)
        startActivity(intent)
    }


    private fun drawPath() {

        var mCurrentLat = Prefrences.getPreferences(this, Constants.LATEST_LAT)!!.toDouble()
        var mCurrentLon = Prefrences.getPreferences(this, Constants.LATEST_LON)!!.toDouble()

        val uri =
            "http://maps.google.com/maps?saddr=$mCurrentLat,$mCurrentLon&daddr=$orderLat,$orderLon"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        startActivity(Intent.createChooser(intent, "Select an application"))

    }

    private fun openChatActivity() {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra(Constants.INTENT_KEY_CHAT_TITLE, barberName)
        intent.putExtra(Constants.INTENT_KEY_CHAT_GROUP_ID, groupId)
        context.startActivity(intent)
    }

    private fun addReview() {

        val serviceRating = binding.serviceRatingBar.rating.toInt().toString()
        val hygieneRating = binding.hygieneRatingBar.rating.toInt().toString()
        val valueRating = binding.valueRatingBar.rating.toInt().toString()

        val partList: MutableList<MultipartBody.Part> = ArrayList<MultipartBody.Part>()
        for (i in arrayListImage.indices) {
            val imagePath: String? = Utils.compressImage(this, arrayListImage[i])
            partList.add(prepareFilePart("image[]", imagePath!!)!!)
        }

        if (partList.isEmpty()) {
            val file = RequestBody.create(MultipartBody.FORM, "")
            partList.add(MultipartBody.Part.createFormData("file", "", file))
        }

        when {
            serviceRating == "0" -> {
                binding.root.snackbar(getString(R.string.select_stars_for_service))
            }
            hygieneRating == "0" -> {
                binding.root.snackbar(getString(R.string.select_stars_for_hygiene))
            }
            valueRating == "0" -> {
                binding.root.snackbar(getString(R.string.select_stars_for_value))
            }
            else -> {
                serverCallForAddReview(partList, serviceRating, hygieneRating, valueRating)
            }
        }
    }

    private fun serverCallForAddReview(
        partList: MutableList<MultipartBody.Part>,
        serviceRating: String,
        hygieneRating: String,
        valueRating: String
    ) {
        viewModel.viewModelScope.launch {
            viewModel.addReview(partList, getParams(serviceRating, hygieneRating, valueRating))
        }
    }

    private fun getParams(
        serviceRating: String,
        hygieneRating: String,
        valueRating: String
    ): Map<String, String> {
        val params = HashMap<String, String>()
        params["user_id"] = Prefrences.getPreferences(this, Constants.USER_ID)!!
        params["barber_id"] = barberId
        params["order_id"] = orderId
        params["service"] = serviceRating
        params["hygiene"] = hygieneRating
        params["value"] = valueRating
        return params
    }

    private fun prepareFilePart(partName: String, fileUri: String): MultipartBody.Part? {
        val file: File = File(fileUri)
        // create RequestBody instance from file
        val requestFile: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    private fun chooseImage() {
        val btnSheet = layoutInflater.inflate(R.layout.choose_image_dialog, null)
        val dialog = BottomSheetDialog(this)
        val tvCaptureImage: TextView = btnSheet.findViewById(R.id.tvCaptureImage)
        val tvUploadImage: TextView = btnSheet.findViewById(R.id.tvUploadImage)
        dialog.setContentView(btnSheet)
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
        mCurrentPhotoPath = FileUtil.getPath(context, uri)
        arrayListImage.add(mCurrentPhotoPath)
        imageAdapter.notifyDataSetChanged()
    }

    private fun takePicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            var photoFile: File? = null
            photoFile = createImageFile()
            // Continue only if the File was successfully created
            if (photoFile != null) {
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                takePictureIntent.putExtra(
                    MediaStore.EXTRA_OUTPUT,
                    FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider", photoFile
                    )
                )
                startActivityForResult(takePictureIntent, requestTakePhoto)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FADE")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val file = File(storageDir.path.toString() + File.separator + imageFileName)
        mCurrentPhotoPath = file.absolutePath
        return file
    }

    private val callback = OnMapReadyCallback { googleMap ->
        var driverLatLon: LatLng? = null
        myGoogleMap = googleMap

        googleMap.clear()
        driverLatLon = LatLng(
            orderLat.toDouble(),
            orderLon.toDouble()
        )

        googleMap.uiSettings.isScrollGesturesEnabled = false
        googleMap.uiSettings.isZoomGesturesEnabled = false
        googleMap.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_map))
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLatLon, 14F))
        googleMap.addMarker(
            MarkerOptions().position(driverLatLon)
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                requestTakePhoto -> {
                    val f = File(mCurrentPhotoPath)
                    uriTemp = FileProvider.getUriForFile(
                        this,
                        applicationContext.packageName + ".provider",
                        f
                    )
                    cropImage(uriTemp!!)
                }

                requestGetPhoto -> {
                    uriTemp = data?.data
                    cropImage(uriTemp!!)
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    mCurrentPhotoPath = Utils.getRealPathFromURI(this, result.uri)
                    File(mCurrentPhotoPath)
                    arrayListImage.add(mCurrentPhotoPath)
                    imageAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun cropImage(uriTemp: Uri) {
        CropImage.activity(uriTemp)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
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
}