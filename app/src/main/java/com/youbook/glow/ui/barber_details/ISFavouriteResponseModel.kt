package com.android.fade.ui.barber_details

import com.google.gson.annotations.SerializedName

data class ISFavouriteResponseModel(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

