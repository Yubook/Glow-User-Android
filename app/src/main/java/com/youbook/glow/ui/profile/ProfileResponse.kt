package com.android.fade.ui.profile

data class ProfileResponse(
	val result: Result? = null,
	val success: Boolean? = false,
	val message: String? = ""
)

data class Result(
	val profile: String? = "",

	val address_line_1: String? = "",
	val address_line_2: String? = "",
	val is_active: Int? = 0,
	val gender: String? = "",
	val latitude: String? = "",
	val latest_latitude: String? = "",
	val mobile: String? = "",
	val latest_longitude: String? = "",
	val profile_approved: Int? = 0,
	val token: String? = "",
	val role_id: Int? = 0,
	val van_number: String? = "",
	val name: String? = "",
	val id: Int? = 0,
	val email: String? = "",
	val longitude: String? = ""
)

