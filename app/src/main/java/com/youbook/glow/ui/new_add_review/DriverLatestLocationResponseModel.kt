package com.android.fade.ui.new_add_review

import com.google.gson.annotations.SerializedName

data class DriverLatestLocationResponseModel(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Barber(

	@field:SerializedName("role_id")
	val roleId: String? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("latest_latitude")
	val latestLatitude: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("latest_longitude")
	val latestLongitude: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
)

data class Result(

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("barber_id")
	val barberId: Int? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("latitude")
	val latitude: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("barber")
	val barber: Barber? = null,

	@field:SerializedName("longitude")
	val longitude: String? = null
)
