package com.android.fade.chat_models

import com.google.gson.annotations.SerializedName

class InboxData {

    @SerializedName("role_1")
    val role1: List<Role1Item> = ArrayList()

    @SerializedName("token")
    val token: String? = null
}