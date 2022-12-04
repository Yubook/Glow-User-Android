package com.android.fade.network

import com.google.gson.annotations.SerializedName

data class CommonResponse(

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("type")
	val type: Int? = null

)
