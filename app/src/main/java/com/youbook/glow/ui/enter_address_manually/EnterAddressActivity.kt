package com.youbook.glow.ui.enter_address_manually

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.fade.extension.snackBar
import com.youbook.glow.MainActivity
import com.youbook.glow.R
import com.youbook.glow.databinding.ActivityEnterAddressBinding
import com.youbook.glow.utils.Constants
import com.android.fade.utils.Utils


class EnterAddressActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityEnterAddressBinding
    private var streetAddress: String = ""
    private var country: String = ""
    private var postCode: String = ""
    private var city: String = ""
    private var houseNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.ivBackButton.setOnClickListener(this)
        binding.tvConfirm.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBackButton -> finish()
            R.id.tvConfirm -> getLatLngFromAddress()
        }
    }

    private fun getLatLngFromAddress() {
        streetAddress = binding.edtStreetAddress.text.toString().trim()
        houseNumber = binding.edtHouseNumber.text.toString().trim()
        city = binding.edtCity.text.toString().trim()
        postCode = binding.edtPostcode.text.toString().trim()
        country = binding.edtCountry.text.toString().trim()

        when {
            streetAddress.isEmpty() -> {
                binding.root.snackBar("Please enter street address")
            }
            city.isEmpty() -> {
                binding.root.snackBar("Please enter city")
            }
            postCode.isEmpty() -> {
                binding.root.snackBar("Please enter pin code")
            }
            country.isEmpty() -> {
                binding.root.snackBar("Please enter country")
            }
            else -> {
                val address = "$houseNumber, $streetAddress, $city, $country, $postCode"
                val latLonFromAddress = Utils.getLatLonFromPostalCode(address, this)
                val i = Intent(this, MainActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                i.putExtra(Constants.OTHER_ADDRESS_LAT_LON, latLonFromAddress)
                startActivity(i)
                finish()
            }
        }
    }
}