package com.android.fade.ui.select_postcode

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.youbook.glow.MainActivity
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivitySelectPostCodeBinding
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils


class SelectPostCodeActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivitySelectPostCodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivitySelectPostCodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListener()

    }

    private fun setOnClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.ivConfirm.setOnClickListener(this)
        binding.ivClearSearch.setOnClickListener(this)

        binding.edtPostcode.setOnEditorActionListener { v, actionId, event ->
            if (actionId and EditorInfo.IME_MASK_ACTION != 0) {
                getLatLonFromPostCode()
                this.hideKeyboard(binding.root)
                true
            } else {
                false
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.ivBackButton -> {
                finish()
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
            }
            R.id.ivConfirm -> {
                getLatLonFromPostCode()
            }

            R.id.ivClearSearch ->{
                binding.edtPostcode.setText("")
            }
        }
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun getLatLonFromPostCode() {

        val postalCode = binding.edtPostcode.text.toString().trim()
        if (isValid()){
            val latLng = Utils.getLatLonFromPostalCode(postalCode, this)
            if (latLng.isNotEmpty()){
                val split = latLng.split(",")
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra(Constants.OTHER_ADDRESS_LAT_LON, latLng)
                startActivity(i)
                finish()
            }
        }
    }

    private fun isValid(): Boolean {
        return if (binding.edtPostcode.text.toString().trim().isEmpty()){
            Toast.makeText(this, "Please enter postal code", Toast.LENGTH_LONG).show()
            false
        } else{
            true
        }
    }
}