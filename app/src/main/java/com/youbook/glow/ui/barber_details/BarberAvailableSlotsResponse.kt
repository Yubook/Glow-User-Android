package com.android.fade.ui.barber_details

import com.google.gson.annotations.SerializedName

data class BarberAvailableSlotsResponse(

	@field:SerializedName("result")
	val result: Result1? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Result1(

	@field:SerializedName("available_slots")
	val availableSlots: List<AvailableSlotsItem?>? = null,

	@field:SerializedName("slots_required")
	val slotsRequired: Int? = null
)

data class Time(

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("time")
	val time: String? = null
)

data class AvailableSlotsItem(

	@field:SerializedName("timing_id")
	val timingId: Int? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("is_active")
	val isActive: Int? = null,

	@field:SerializedName("barber_id")
	val barberId: Int? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("user_id")
	val userId: Any? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("time")
	val time: Time? = null,

	@field:SerializedName("is_booked")
	val isBooked: Int? = null,

	var isSelected: Boolean = false
)
