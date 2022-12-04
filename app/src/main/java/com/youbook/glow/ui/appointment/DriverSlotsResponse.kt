package com.android.fade.ui.appointment

data class DriverSlotsResponse(
	val result: Result? = null,
	val success: Boolean? = false,
	val message: String? = ""
)

data class Result(
	val first_page_url: String? = "",
	val path: String? = "",
	val per_page: Int? = 0,
	val total: Int? = 0,
	val data: List<DataItem?>? = null,
	val last_page: Int? = 0,
	val last_page_url: String? = "",
	val next_page_url: String? = "",
	val from: Int? = 0,
	val to: Int? = 0,
	val prev_page_url: String? = "",
	val current_page: Int? = 0
)

data class DataItem(
    val timing_id: Int? = 0,
    val date: String? = "",
    val driver_id: Int? = 0,
    val is_active: Int? = 0,
    val timing_slot: TimingSlot? = null,
    val updated_at: String? = "",
    val user_id: Int? = 0,
    val created_at: String? = "",
    val id: Int? = 0,
    var is_booked: Int? = 0,
    var isSelected : Boolean? = false
)

data class TimingSlot(
	val is_active: Int? = 0,
	val updated_at: String? = "",
	val created_at: String? = "",
	val id: Int? = 0,
	val time: String? = ""
)

