package com.android.fade.ui.barber_details

import com.android.fade.ServicesItem
import com.google.gson.annotations.SerializedName

data class BarberDetailsResponseModel(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class ReviewsItem(

	@field:SerializedName("barber_id")
	val barberId: Int? = null,

	@field:SerializedName("user_id")
	val userId: Int? = null,

	@field:SerializedName("service")
	val service: Int? = null,

	@field:SerializedName("hygiene")
	val hygiene: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("order_id")
	val orderId: String? = null,

	@field:SerializedName("value")
	val value: Int? = null
)

data class Subcategory(

	@field:SerializedName("category_name")
	val categoryName: CategoryName? = null,

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class PortfoliosItem(

	@field:SerializedName("path")
	val path: String? = null,

	@field:SerializedName("barber_id")
	val barberId: Int? = null
)

data class Category(

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Service(

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("time")
	val time: Int? = null,

	@field:SerializedName("category")
	val category: Category? = null,

	@field:SerializedName("subcategory")
	val subcategory: Subcategory? = null
)

data class Result(

	@field:SerializedName("fivestar")
	val fivestar: Int? = null,

	@field:SerializedName("is_favourite")
	val is_favourite: Boolean? = false,

	@field:SerializedName("chat")
	val chat: Boolean? = false,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("chat_group_id")
	val chat_group_id: String? = "",

	@field:SerializedName("profile")
	val profile: String? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("portfolios")
	val portfolios: List<PortfoliosItem?>? = null,

	@field:SerializedName("average_rating")
	val averageRating: Int? = null,

	@field:SerializedName("type")
	val favouriteType: Int? = null,

	@field:SerializedName("twostar")
	val twostar: Int? = null,

	@field:SerializedName("services")
	val services: List<ServicesItem?>? = null,

	@field:SerializedName("policy_and_term")
	val policyAndTerm: PolicyAndTerm? = null,

	@field:SerializedName("threestar")
	val threestar: Int? = null,

	@field:SerializedName("barber_id")
	val barberId: Int? = null,

	@field:SerializedName("reviews")
	val reviews: List<ReviewsItem?>? = null,

	@field:SerializedName("onestar")
	val onestar: Int? = null,

	@field:SerializedName("fourstar")
	val fourstar: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("total_reviews")
	val totalReviews: Int? = null
)

data class CategoryName(

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class PolicyAndTerm(

	@field:SerializedName("barber_id")
	val barberId: Int? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("deleted_at")
	val deletedAt: Any? = null,

	@field:SerializedName("content")
	val content: String? = null
)
