package com.android.fade.ui.payment_details

import com.google.gson.annotations.SerializedName

data class StripePaymentResponse(

	@field:SerializedName("result")
	val result: Result? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class Result(

	@field:SerializedName("balance_transaction")
	val balanceTransaction: String? = null,

	@field:SerializedName("billing_details")
	val billingDetails: BillingDetails? = null,

	@field:SerializedName("metadata")
	val metadata: List<Any?>? = null,

	@field:SerializedName("livemode")
	val livemode: Boolean? = null,

	@field:SerializedName("destination")
	val destination: Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("failure_message")
	val failureMessage: Any? = null,

	@field:SerializedName("fraud_details")
	val fraudDetails: List<Any?>? = null,

	@field:SerializedName("source")
	val source: Source? = null,

	@field:SerializedName("amount_refunded")
	val amountRefunded: Int? = null,

	@field:SerializedName("refunds")
	val refunds: Refunds? = null,

	@field:SerializedName("statement_descriptor")
	val statementDescriptor: Any? = null,

	@field:SerializedName("transfer_data")
	val transferData: Any? = null,

	@field:SerializedName("receipt_url")
	val receiptUrl: String? = null,

	@field:SerializedName("shipping")
	val shipping: Any? = null,

	@field:SerializedName("review")
	val review: Any? = null,

	@field:SerializedName("captured")
	val captured: Boolean? = null,

	@field:SerializedName("calculated_statement_descriptor")
	val calculatedStatementDescriptor: String? = null,

	@field:SerializedName("currency")
	val currency: String? = null,

	@field:SerializedName("refunded")
	val refunded: Boolean? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("outcome")
	val outcome: Outcome? = null,

	@field:SerializedName("payment_method")
	val paymentMethod: String? = null,

	@field:SerializedName("order")
	val order: Any? = null,

	@field:SerializedName("dispute")
	val dispute: Any? = null,

	@field:SerializedName("amount")
	val amount: Int? = null,

	@field:SerializedName("disputed")
	val disputed: Boolean? = null,

	@field:SerializedName("failure_code")
	val failureCode: Any? = null,

	@field:SerializedName("transfer_group")
	val transferGroup: Any? = null,

	@field:SerializedName("on_behalf_of")
	val onBehalfOf: Any? = null,

	@field:SerializedName("created")
	val created: Int? = null,

	@field:SerializedName("payment_method_details")
	val paymentMethodDetails: PaymentMethodDetails? = null,

	@field:SerializedName("amount_captured")
	val amountCaptured: Int? = null,

	@field:SerializedName("source_transfer")
	val sourceTransfer: Any? = null,

	@field:SerializedName("receipt_number")
	val receiptNumber: Any? = null,

	@field:SerializedName("application")
	val application: Any? = null,

	@field:SerializedName("receipt_email")
	val receiptEmail: Any? = null,

	@field:SerializedName("paid")
	val paid: Boolean? = null,

	@field:SerializedName("application_fee")
	val applicationFee: Any? = null,

	@field:SerializedName("payment_intent")
	val paymentIntent: Any? = null,

	@field:SerializedName("invoice")
	val invoice: Any? = null,

	@field:SerializedName("statement_descriptor_suffix")
	val statementDescriptorSuffix: Any? = null,

	@field:SerializedName("application_fee_amount")
	val applicationFeeAmount: Any? = null,

	/*@field:SerializedName("object")
	val object1: String? = null,*/

	@field:SerializedName("customer")
	val customer: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class Checks(

	@field:SerializedName("cvc_check")
	val cvcCheck: String? = null,

	@field:SerializedName("address_line1_check")
	val addressLine1Check: Any? = null,

	@field:SerializedName("address_postal_code_check")
	val addressPostalCodeCheck: Any? = null
)

data class Outcome(

	@field:SerializedName("reason")
	val reason: Any? = null,

	@field:SerializedName("risk_level")
	val riskLevel: String? = null,

	@field:SerializedName("risk_score")
	val riskScore: Int? = null,

	@field:SerializedName("seller_message")
	val sellerMessage: String? = null,

	@field:SerializedName("network_status")
	val networkStatus: String? = null,

	@field:SerializedName("type")
	val type: String? = null
)

data class BillingDetails(

	@field:SerializedName("address")
	val address: Address? = null,

	@field:SerializedName("phone")
	val phone: Any? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("email")
	val email: Any? = null
)

data class Source(

	@field:SerializedName("address_zip_check")
	val addressZipCheck: Any? = null,

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("last4")
	val last4: String? = null,

	@field:SerializedName("funding")
	val funding: String? = null,

	@field:SerializedName("metadata")
	val metadata: List<Any?>? = null,

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

	/*@field:SerializedName("object")
	val object1: String? = null,*/

	@field:SerializedName("customer")
	val customer: Any? = null
)

data class Card(

	@field:SerializedName("country")
	val country: String? = null,

	@field:SerializedName("last4")
	val last4: String? = null,

	@field:SerializedName("funding")
	val funding: String? = null,

	@field:SerializedName("checks")
	val checks: Checks? = null,

	@field:SerializedName("wallet")
	val wallet: Any? = null,

	@field:SerializedName("installments")
	val installments: Any? = null,

	@field:SerializedName("fingerprint")
	val fingerprint: String? = null,

	@field:SerializedName("exp_month")
	val expMonth: Int? = null,

	@field:SerializedName("exp_year")
	val expYear: Int? = null,

	@field:SerializedName("three_d_secure")
	val threeDSecure: Any? = null,

	@field:SerializedName("brand")
	val brand: String? = null,

	@field:SerializedName("network")
	val network: String? = null
)

data class PaymentMethodDetails(

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("card")
	val card: Card? = null
)

data class Address(

	@field:SerializedName("country")
	val country: Any? = null,

	@field:SerializedName("city")
	val city: Any? = null,

	/*@field:SerializedName("state")
	val state: Any? = null,*/

	@field:SerializedName("postal_code")
	val postalCode: Any? = null,

	@field:SerializedName("line2")
	val line2: Any? = null,

	@field:SerializedName("line1")
	val line1: Any? = null
)

data class Refunds(

	@field:SerializedName("data")
	val data: List<Any?>? = null,

	@field:SerializedName("total_count")
	val totalCount: Int? = null,

	@field:SerializedName("has_more")
	val hasMore: Boolean? = null,

	@field:SerializedName("url")
	val url: String? = null,

	/*@field:SerializedName("object")
	val object1: String? = null*/
)
