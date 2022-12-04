package com.android.fade.ui.add_review

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Prefrences
import com.android.fade.utils.Utils
import com.android.fade.utils.Utils.compressImage
import com.android.fade.utils.Utils.snackbar
import com.android.fade.utils.Utils.toast
import com.android.fade.utils.Utils.visible
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityAddReviewBinding
import com.youbook.glow.ui.add_review.AddReviewViewModelFactory
import com.youbook.glow.utils.FileUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
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


class AddReviewActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mCurrentPhotoPath: String
    private lateinit var binding: ActivityAddReviewBinding
    private lateinit var viewModel: AddReviewViewModel
    private lateinit var imageAdapter: ImageAdapter
    private val arrayListImage = ArrayList<String>()
    var driverId: String = ""
    var orderId: String = ""
    var driverName: String = ""
    var driverImage: String = ""
    private val requestTakePhoto = 1
    private val requestGetPhoto = 2
    private var uriTemp: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            AddReviewViewModelFactory(
                AddReviewRepository(
                    MyApi.getInstanceToken(
                        Prefrences.getPreferences(this, Constants.API_TOKEN)!!
                    )
                )
            )
        )[AddReviewViewModel::class.java]

        binding.imageRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL, false
        )
        imageAdapter = ImageAdapter(this, arrayListImage)
        binding.imageRecyclerView.adapter = imageAdapter

        driverId = intent.getStringExtra(Constants.DRIVER_ID)!!
        driverName = intent.getStringExtra(Constants.DRIVER_NAME)!!
        driverImage = intent.getStringExtra(Constants.DRIVER_IMAGE)!!
        orderId = intent.getStringExtra(Constants.ORDER_ID)!!

        setDriverData()
        setClickListener()

        viewModel.addReviewResponse.observe(this, androidx.lifecycle.Observer {
            binding.progressBar.visible(it is Resource.Loading)
            when (it) {
                is Resource.Success -> {
                    binding.progressBar.visible(false)
                    if (it.value.success!!) {
                        this.toast(it.value.message!!)
                        finish()
                    } else {
                        this.toast(it.value.message!!)
                    }
                }
                is Resource.Failure -> Utils.handleApiError(binding.root, it) {
                    binding.progressBar.visible(false)
                }
            }
        })
    }

    private fun setDriverData() {
        binding.tvDriverName.text = driverName
        loadingImage(this, driverImage, binding.ivDriverImage, true)
    }

    private fun setClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvAddReview.setOnClickListener(this)
        binding.relAddImage.setOnClickListener(this)
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
        val file = File(storageDir.getPath().toString() + File.separator + imageFileName)
        mCurrentPhotoPath = file.absolutePath

        return file
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.tvAddReview -> addReview()
            R.id.relAddImage -> chooseImage()
        }
    }

    private fun addReview() {

        val rating = binding.driverReviewRatingBar.rating.toInt().toString()
        val comment = binding.edtReviewComment.text.toString().trim()

        val partList: MutableList<MultipartBody.Part> = ArrayList<MultipartBody.Part>()

        for (i in arrayListImage.indices) {
            val imagePath: String? = compressImage(this, arrayListImage[i])
            partList.add(prepareFilePart("image[]", imagePath!!)!!)
        }

        if (partList.isEmpty()){
            val file = RequestBody.create(MultipartBody.FORM, "")
            partList.add(MultipartBody.Part.createFormData("file", "", file))
        }

        if (rating == "0") {
            binding.root.snackbar("Please select stars for rating")
        } else if (comment.isEmpty()) {
            binding.root.snackbar("Please enter your comments")
        } else {
            serverCallForAddReview(partList, rating, comment)
        }
    }

    private fun serverCallForAddReviewWithoutImage(rating: String, comment: String) {
        viewModel.viewModelScope.launch {
            viewModel.addReviewWithoutImage(getParams(rating, comment))
        }
    }

    private fun serverCallForAddReview(
        partList: MutableList<MultipartBody.Part>,
        rating: String,
        comment: String,
    ) {
        viewModel.viewModelScope.launch {
            viewModel.addReview(partList, getParams(rating, comment))
        }
    }

    private fun getParams(rating: String, comment: String): Map<String, String> {
        val params = HashMap<String, String>()
        params["from_id"] = Prefrences.getPreferences(this, Constants.USER_ID)!!
        params["to_id"] = driverId
        params["rating"] = rating
        params["message"] = comment
        params["order_id"] = orderId
        return params
    }


    private fun prepareFilePart(partName: String, fileUri: String): MultipartBody.Part? {
        val file: File = File(fileUri)
        // create RequestBody instance from file
        val requestFile: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.name, requestFile)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                requestTakePhoto -> {
                    val f = File(mCurrentPhotoPath!!)
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
                    val file = File(mCurrentPhotoPath)

                    arrayListImage.add(mCurrentPhotoPath)
                    imageAdapter.notifyDataSetChanged()

                    /*Glide.with(application)
                        .load(file)
                        .placeholder(R.drawable.ic_avtar_placeholder)
                        .error(R.drawable.ic_avtar_placeholder)
                        .into(binding.ivUserProfile)*/
                }
            }
        }
    }

    fun cropImage(uriTemp: Uri) {
        CropImage.activity(uriTemp)
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this)
    }

}