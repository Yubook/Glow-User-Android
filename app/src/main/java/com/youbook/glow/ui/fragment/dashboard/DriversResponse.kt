package com.android.fade.ui.fragment.dashboard

data class DriversResponse(
	val result: List<ResultItem?>? = null,
	val success: Boolean? = false,
	val message: String? = ""
)

data class ServiceItem(
	val image: String? = "",
	val isActive: Int? = 0,
	val price: Int? = 0,
	val name: String? = "",
	val requiredTime: Int? = 0,
	val id: Int? = 0
)

data class ResultItem(
	val image: String? = "",
	val address: String? = "",
	val is_active: Int? = 0,
	val gender: String? = "",
	val distance: Double? = null,
	val latitude: String? = "",
	val latest_latitude: String? = "",
	val mobile: Long? = 0,
	val latest_longitude: String? = "",
	val average_rating: Double? = null,
	val profile_approved: Int? = 0,
	val role_id: Int? = 0,
	val service: List<ServiceItem?>? = null,
	val van_number: String? = "",
	val name: String? = "",
	val total_rating: Int? = 0,
	val id: Int? = 0,
	val email: String? = "",
	val longitude: String? = ""
)

