package com.youbook.glow.ui.login_with_email

import com.google.gson.annotations.SerializedName

data class UserLoginResponse(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Result(

	@field:SerializedName("otp")
	val otp: Int? = null
)
