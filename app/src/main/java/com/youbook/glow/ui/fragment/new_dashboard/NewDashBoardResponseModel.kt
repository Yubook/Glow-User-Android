package com.android.fade.ui.fragment.new_dashboard

data class NewDashBoardResponseModel(
	val result: Result? = null,
	val success: Boolean? = null,
	val message: String? = null
)

data class Result(
	val is_read: Boolean? = null,
	val customer_support_chat_id: String? = "",
	val nearByBarbers: List<NearByBarbersItem?>? = null
)

data class NearByBarbersItem(
	val distance: Double? = null,
	val gender: String? = null,
	val city: City? = null,
	val latitude: String? = null,
	val latest_latitude: String? = null,
	val profile: String? = null,
	val mobile: Long? = null,
	val latest_longitude: String? = null,
	val average_rating: Int? = null,
	var is_favourite: Boolean? = null,
	var services: List<ServicesItem?>? = null,
	val barber_id: Int? = null,
	val name: String? = null,
	val address_line_1: String? = null,
	val address_line_2: String? = null,
	val total_reviews: Int? = null,
	val is_available: Int? = null,
	val state: State? = null,
	val email: String? = null,
	val longitude: String? = null
)

data class Service(
	val image: String? = null,
	val is_active: Int? = null,
	var name: String? = null,
	val id: Int? = null,
	val category: Category? = null,
	val subcategory: Subcategory? = null
)

data class City(
	val any: Any? = null
)

data class CategoryName(
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class Category(
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class Subcategory(
	val category_name: CategoryName? = null,
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class ServicesItem(
	val service_price: Double? = null,
	val service: Service? = null,
	val id: Int? = null
)

data class State(
	val any: Any? = null
)

