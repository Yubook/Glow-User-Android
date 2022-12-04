package com.android.fade.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.youbook.glow.R
import com.android.fade.network.Resource
import com.youbook.glow.utils.Constants
import com.youbook.glow.utils.ImageCompress
import com.google.android.material.snackbar.Snackbar
import gun0912.tedimagepicker.util.ToastUtil.context
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Utils {

    fun getLocalTime(time: String?, timeFormat: String?): Calendar? {
        try {
            val calendar = Calendar.getInstance()
            val df = SimpleDateFormat(timeFormat, Locale.ENGLISH)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(time)
            df.timeZone = TimeZone.getDefault()
            assert(date != null)
            calendar.time = date
            return calendar
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    @SuppressLint("DefaultLocale")
    fun getLatLonFromPostalCode(postalCode: String, context: Context): String {
        val geoCoder = Geocoder(context)
        try {
            val addresses: List<Address>? = geoCoder.getFromLocationName(postalCode, 1)
            if (addresses != null && !addresses.isEmpty()) {
                val address: Address = addresses[0]
                val message = java.lang.String.format(
                    "Latitude: %f, Longitude: %f",
                    address.latitude, address.longitude
                )
                //Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                return address.latitude.toString() + "," + address.longitude
            } else {
                // Display appropriate message when Geocoder services are not available
                Toast.makeText(context, "Please enter correct postal code", Toast.LENGTH_LONG)
                    .show()
                return ""
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Something wrong with Postal Code", Toast.LENGTH_LONG).show()
            return ""
        }
    }

    //Network helper
    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    return if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        true
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        true
                    } else capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                }
            } else {
                try {
                    val activeNetworkInfo = connectivityManager.activeNetworkInfo
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                        return true
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.no_internet_connection),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return false
    }

    fun capitalize(str: String?): String? {
        return if (str == null || str.isEmpty()) {
            str
        } else str.substring(0, 1).toUpperCase() + str.substring(1)
    }

    private fun RGB565toARGB888(img: Bitmap): Bitmap? {
        val numPixels = img.width * img.height
        val pixels = IntArray(numPixels)

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)

        //Create a Bitmap of the appropriate format.
        val result = Bitmap.createBitmap(img.width, img.height, Bitmap.Config.ARGB_8888)

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun showKeyboard(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    fun Context.toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    fun View.visible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun View.enable(enabled: Boolean) {
        isEnabled = enabled
        alpha = if (enabled) 1f else 0.5f
    }

    fun handleApiError(
        view: View,
        failure: Resource.Failure,
        retry: (() -> Unit)? = null
    ) {
        when {
            failure.isNetworkError -> view.snackbar(
                context.getString(R.string.no_internet_connection),
                retry
            )
            else -> {

                val error = failure.errorBody?.string().toString()
                var jsonObject = JSONObject(error)
                val dialog = Dialog(view.context, R.style.CustomDialog)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(true)
                dialog.setContentView(R.layout.custom_error_dialog)
                val title = dialog.findViewById(R.id.tvTitle) as TextView
                val tvMessage = dialog.findViewById(R.id.tvMessage) as TextView
                if(jsonObject.has("message")){
                    tvMessage.text = jsonObject.getString("message")
                } else {
                    tvMessage.text = "Something went wrong!"
                }
                val yesBtn = dialog.findViewById(R.id.tvOk) as TextView
                yesBtn.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
            }
        }
    }

    fun formatDate(fromFormat: String?, toFormat: String?, dateToFormat: String?): String? {
        val inFormat = SimpleDateFormat(fromFormat)
        var date: Date? = null
        try {
            date = inFormat.parse(dateToFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val outFormat = SimpleDateFormat(toFormat)
        return outFormat.format(date)
    }

    fun Fragment.handleApiError(
        failure: Resource.Failure,
        retry: (() -> Unit)? = null
    ) {
        when {
            failure.isNetworkError -> requireView().snackbar(
                getString(R.string.no_internet_connection),
                retry
            )
            else -> {
                val error = failure.errorBody?.string().toString()
                requireView().snackbar(error)
            }
        }
    }

    fun compressImage(context: Context?, path: String?): String? {
        return ImageCompress.compressImage(
            context,
            1516.0f,
            1212.0f,
            path,
            getCompressFilePath(context!!).toString()
        )
    }

    /*fun getYMDFormatDate(ymd:String):String?{
        val tf = SimpleDateFormat("yyyy-MM-dd")
    }*/

    private fun getCompressFilePath(context: Context): File? {
        val state = Environment.getExternalStorageState()
        val file: File
        file = if (Environment.MEDIA_MOUNTED == state) {
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                Constants.DirectoryName + "/compress/"
            )
        } else {
            File(context.filesDir, Constants.DirectoryName + "/compress/")
        }
        if (!file.exists()) file.mkdirs()
        return File(file, getRandomImageName(context))
    }

    private fun getRandomImageName(context: Context): String? {
        val r = Random()
        val i1 = r.nextInt(1000 - 1) + 65
        return Constants.DirectoryName.replace(" ", "_") + "_" + i1 + ".png"
    }

    fun <A : Activity> Activity.startNewActivity(activity: Class<A>) {
        Intent(this, activity).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK /*or Intent.FLAG_ACTIVITY_CLEAR_TASK*/
            startActivity(it)
        }
    }


    fun ProgressBar.show() {
        visibility = View.VISIBLE
    }

    fun ProgressBar.hide() {
        visibility = View.GONE
    }

    fun View.snackbar(message: String, action: (() -> Unit)? = null) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
        action?.let {
            snackbar.setAction("Retry") {
                it()
            }
        }
        snackbar.show()
    }

    fun View.snackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).show()
    }


    fun showErrorDialog(context: Context, message: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_error_dialog)
        val title = dialog.findViewById(R.id.tvTitle) as TextView
        val tvMessage = dialog.findViewById(R.id.tvMessage) as TextView
        tvMessage.text = message
        val yesBtn = dialog.findViewById(R.id.tvOk) as TextView
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun showErrorDialog(context: Context, message: String, title: String) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_error_dialog)
        val tvTitle = dialog.findViewById(R.id.tvTitle) as TextView
        tvTitle.text = title
        val tvMessage = dialog.findViewById(R.id.tvMessage) as TextView
        tvMessage.text = message
        val yesBtn = dialog.findViewById(R.id.tvOk) as TextView
        yesBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getAddress(latitude: Double, longitude: Double, context: Context): String? {
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.isNotEmpty()) {
                val address: Address = addresses[0]
                result.append(address.getAddressLine(0))
            } else {
                result.append("Can't find any location, please select another address.")
            }
        } catch (e: IOException) {
            e.message?.let { Log.e("tag", it) }
        }
        return result.toString()
    }


    fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    fun getRealPathFromURI(context: Context, contentURI: Uri): String {
        val result: String
        val cursor = context.contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path.toString()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

}