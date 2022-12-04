package com.android.fade.ui.chat

import com.google.gson.annotations.SerializedName

class MessageData {
    @SerializedName("result")
    val result: MessageDataItem? = null

    @SerializedName("success")
    val success = 0

    @SerializedName("id")
    val id: String? = null

    @SerializedName("token")
    val token: String? = null
}