package com.android.fade.ui.fragment.favourite_list

data class FavBarberResponseModel(
	val result: List<ResultItem?>? = null,
	val success: Boolean? = null,
	val message: String? = null
)

data class Subcategory(
	val category_name: CategoryName? = null,
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class Category(
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class ResultItem(
	val gender: String? = null,
	val distance: Double? = null,
	val profile: String? = null,
	val latitude: String? = null,
	val latest_latitude: String? = null,
	val mobile: Long? = null,
	val average_rating: Int? = null,
	val latest_longitude: String? = null,
	val services: List<ServicesItem?>? = null,
	val barber_id: Int? = null,
	val name: String? = null,
	val total_reviews: Int? = null,
	val longitude: String? = null
)

data class ServicesItem(
	val service_price: Double? = null,
	val service: Service? = null,
	val id: Int? = null
)

data class CategoryName(
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null
)

data class Service(
	val image: String? = null,
	val is_active: Int? = null,
	val name: String? = null,
	val id: Int? = null,
	val time: Int? = null,
	val category: Category? = null,
	val subcategory: Subcategory? = null
)

