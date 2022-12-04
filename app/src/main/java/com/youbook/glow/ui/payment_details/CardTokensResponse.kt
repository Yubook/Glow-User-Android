package com.android.fade.ui.payment_details

import com.google.gson.annotations.SerializedName

data class CardTokensResponse(

	@field:SerializedName("livemode")
	val livemode: Boolean? = false,

	@field:SerializedName("created")
	val created: Int? = 0,

	@field:SerializedName("client_ip")
	val client_ip: String? = "",

	@field:SerializedName("id")
	val id: String? = "",

	@field:SerializedName("used")
	val used: Boolean? = false,

	@field:SerializedName("type")
	val type: String? = "",

	@field:SerializedName("card")
	val card: CardDetails? = null,

	@field:SerializedName("object")
	val objectType: String? = ""
)

data class Metadata(
	val any: Any? = null
)

data class CardDetails(

	@field:SerializedName("address_zip_check")
	val addressZipCheck: Any? = null,

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("last4")
	val last4: String? = null,

	@field:SerializedName("funding")
	val funding: String? = null,

	@field:SerializedName("metadata")
	val metadata: Metadata? = null,

	@field:SerializedName("address_country")
	val addressCountry: Any? = null,

	@field:SerializedName("address_state")
	val addressState: Any? = null,

	@field:SerializedName("exp_month")
	val expMonth: Int? = null,

	@field:SerializedName("exp_year")
	val expYear: Int? = null,

	@field:SerializedName("address_city")
	val addressCity: Any? = null,

	@field:SerializedName("tokenization_method")
	val tokenizationMethod: Any? = null,

	@field:SerializedName("cvc_check")
	val cvcCheck: String? = null,

	@field:SerializedName("address_line2")
	val addressLine2: Any? = null,

	@field:SerializedName("address_line1")
	val addressLine1: Any? = null,

	@field:SerializedName("fingerprint")
	val fingerprint: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("address_line1_check")
	val addressLine1Check: Any? = null,

	@field:SerializedName("address_zip")
	val addressZip: Any? = null,

	@field:SerializedName("dynamic_last4")
	val dynamicLast4: Any? = null,

	@field:SerializedName("brand")
	val brand: String? = null,

	@field:SerializedName("object")
	val objectType: String? = null
)
