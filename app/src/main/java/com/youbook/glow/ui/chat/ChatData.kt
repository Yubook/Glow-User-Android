package com.android.fade.ui.chat

import com.google.gson.annotations.SerializedName

class ChatData {

    @SerializedName("group_id")
    val groupId = 0

    @SerializedName("success")
    val success = 0

    @SerializedName("receiver_id")
    val receiverId = 0

    @SerializedName("messageData")
    val messageData: List<MessageDataItem>? = null

    @SerializedName("token")
    val token: String? = null
}