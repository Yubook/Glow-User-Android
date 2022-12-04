package com.android.fade.network

import com.android.fade.ui.barber_details.BarberAvailableSlotsResponse
import com.android.fade.ui.barber_details.BarberDetailsResponseModel
import com.android.fade.ui.barber_details.ISFavouriteResponseModel
import com.android.fade.ui.fragment.booking_list.NewBookingListResponse
import com.android.fade.ui.fragment.dashboard.DriversResponse
import com.android.fade.ui.fragment.favourite_list.FavBarberResponseModel
import com.android.fade.ui.fragment.new_dashboard.NewDashBoardResponseModel
import com.android.fade.ui.fragment.new_dashboard.ServiceResponseModel
import com.android.fade.ui.login.CountryCodeResponseModel
import com.android.fade.ui.new_add_review.DriverLatestLocationResponseModel
import com.android.fade.ui.notification.NotificationListResponse
import com.android.fade.ui.payment_details.BookSlotsResponse
import com.android.fade.ui.payment_details.PaymentKeyResponse
import com.android.fade.ui.payment_details.StripePaymentResponse
import com.android.fade.ui.payment_history.UserPaymentHistoryResponse
import com.android.fade.ui.profile.ProfileResponse
import com.android.fade.ui.review.ReviewResponse
import com.android.fade.ui.select_service.ServicesResponse
import com.android.fade.ui.terms_privacy.TermsPrivacyResponse
import com.youbook.glow.ui.login_with_email.UserLoginResponse
import com.youbook.glow.utils.Constants
import com.google.gson.GsonBuilder
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface MyApi {

    @Multipart
    @POST("addProfile")
    suspend fun addProfile(
        @Part files: List<MultipartBody.Part>,
        @QueryMap params: Map<String, String>
    ): ProfileResponse

    @Multipart
    @POST("editProfile")
    suspend fun updateProfile(
        @Part files: List<MultipartBody.Part>,
        @QueryMap params: Map<String, String>
    ): ProfileResponse

    @FormUrlEncoded
    @POST("editProfile")
    suspend fun updateProfileWithoutPhoto(
        @FieldMap params: Map<String, String>
    ): ProfileResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(@Field("mobile") mobile: String): ProfileResponse

    @FormUrlEncoded
    @POST("loginUserOtpVerify")
    suspend fun loginUserOtpVerify(
        @Field("email") mobile: String,
        @Field("otp") otp: Int
    ): ProfileResponse

    @FormUrlEncoded
    @POST("addDeviceIdAndToken")
    suspend fun getDashBoardData(
        @FieldMap params: Map<String, String>
    ): NewDashBoardResponseModel

    @FormUrlEncoded
    @POST("loginUser")
    suspend fun loginUser(@Field("email") mobile: String): UserLoginResponse

    @FormUrlEncoded
    @POST("getNearByBarberByLocations")
    suspend fun getDashBoardDataWithOtherLatLon(
        @FieldMap params: Map<String, String>
    ): NewDashBoardResponseModel

    @FormUrlEncoded
    @POST("is_favourite")
    suspend fun makeBarberFavUnFav(
        @FieldMap params: Map<String, String>
    ): ISFavouriteResponseModel

    @GET("driverLatestLocation")
    suspend fun getDriverLatestLocation(
        @Query("order_id") forUser: String
    ): DriverLatestLocationResponseModel

    @FormUrlEncoded
    @POST("paymentProcess")
    suspend fun makePayment(
        @FieldMap params: Map<String, String>
    ): StripePaymentResponse


    @Multipart
    @POST("addReview")
    suspend fun addReview(
        @Part files: List<MultipartBody.Part>,
        @QueryMap params: Map<String, String>
    ): CommonResponse

    @Multipart
    @POST("addReview")
    suspend fun addReviewWithoutImage(
        @QueryMap params: Map<String, String>
    ): CommonResponse


    @FormUrlEncoded
    @POST("bookUserSlot")
    suspend fun bookUserSlot(
        @FieldMap params: Map<String, String>
    ): BookSlotsResponse

    @FormUrlEncoded
    @POST("bookUserSlotByWallet")
    suspend fun bookUserSlotByWallet(
        @FieldMap params: Map<String, String>
    ): CommonResponse

    @FormUrlEncoded
    @POST("rejectAndBookUserSlot")
    suspend fun rescheduleOrder(
        @FieldMap params: Map<String, String>
    ): CommonResponse

    @POST("logout")
    suspend fun logout(): CommonResponse

    @GET("getNearestDriver")
    suspend fun getNearestDriver(): DriversResponse

    @GET("getServices")
    suspend fun getServices(): ServiceResponseModel

    @GET("myFavouriteBarbers")
    suspend fun getFavouriteBarbers(): FavBarberResponseModel

    @GET("getCountries")
    suspend fun getCountryCode(): CountryCodeResponseModel

    @FormUrlEncoded
    @POST("searchDriver")
    suspend fun searchDriver(
        @FieldMap params: Map<String, String>
    ): DriversResponse

    @FormUrlEncoded
    @POST("bookAppointment")
    suspend fun bookAppointment(
        @FieldMap params: Map<String, String>,
        @Field("slot_ids[]") slotIdsList : ArrayList<String>,
        @Field("service_ids[]") serviceIds : ArrayList<String>
    ): CommonResponse

    @FormUrlEncoded
    @POST("checkBookAppointmentSlots")
    suspend fun getDriverSlots(
        @Field("barber_id") barber_id: String?,
        @Field("date") date: String?,
        @Field("total_time") total_time: String?
    ): BarberAvailableSlotsResponse

    @GET("getDriverProfile")
    suspend fun getBarberDetails(
        @Query("barber_id") driver_id: String?,
    ): BarberDetailsResponseModel

    @GET("getOrder")
    suspend fun getUserOrders(
        @Query("user_id") driver_id: String?
    ): NewBookingListResponse

    @GET("getUserBookings")
    suspend fun getUserBookings(): NewBookingListResponse

    @FormUrlEncoded
    @POST("cancelOrder")
    suspend fun orderCancelByUser(
        @FieldMap params: Map<String, String>
    ): CommonResponse

    @GET("getUserPaymentHistory")
    suspend fun getUserPaymentHistory(): UserPaymentHistoryResponse

    @GET("getReview")
    suspend fun getReview(): ReviewResponse

    @GET("getNotification")
    suspend fun getNotification(): NotificationListResponse

    @GET("read_notification")
    suspend fun readNotification(): CommonResponse

    @GET("getTermsPolicy")
    suspend fun getTermsPolicy(
        @Query("for") forUser : String
    ): TermsPrivacyResponse

    @GET("createPaymentToken")
    suspend fun getPaymentToken(): PaymentKeyResponse

    @GET("getService")
    suspend fun getService(): ServicesResponse


    companion object {

        var myApi: MyApi? = null

        fun getInstanceToken(authToken: String): MyApi {
            val client = OkHttpClient.Builder().apply {
                addInterceptor(AuthenticationInterceptor(authToken))
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }.build()

            val gson = GsonBuilder()
                .setLenient()
                .create()


            val retrofit = Retrofit.Builder()
            retrofit.baseUrl(Constants.BASE_URL)
            retrofit.client(client)
            retrofit.addConverterFactory(GsonConverterFactory.create(gson))
            retrofit.addConverterFactory(ScalarsConverterFactory.create())
            myApi = retrofit.build().create(MyApi::class.java)

            return myApi!!
        }

        fun getInstance(): MyApi {

            val client = OkHttpClient.Builder().apply {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            }.build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            if (myApi == null) {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
                myApi = retrofit.create(MyApi::class.java)
            }
            return myApi!!
        }
    }

}