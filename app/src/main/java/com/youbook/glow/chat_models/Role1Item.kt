package com.android.fade.chat_models

import com.google.gson.annotations.SerializedName

class Role1Item {

    @SerializedName("unread_count")
     var unreadCount = 0

    @SerializedName("last_active_time")
     var lastActiveTime: String? = null

    @SerializedName("updated_at")
     var updatedAt: String? = null

    @SerializedName("user_id")
     var userId = 0

    @SerializedName("group_id")
     var groupId = 0

    @SerializedName("pharmacy_id")
     var pharmacyId = 0

    @SerializedName("pharmacy_name")
     var pharmacyName: String? = null

    @SerializedName("name")
     var name: String? = null

    @SerializedName("device_token")
     var deviceToken: String? = null

    @SerializedName("profile_pic")
     var profilePic: String? = null

    @SerializedName("user_image")
     var userImage: String? = null

    @SerializedName("id")
     var id = 0

    @SerializedName("device_type")
     var deviceType: String? = null

}