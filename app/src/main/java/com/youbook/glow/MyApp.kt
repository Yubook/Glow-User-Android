package com.youbook.glow

import android.app.Application
import com.youbook.glow.utils.Constants
import com.android.fade.utils.SocketConnector
import com.stripe.android.PaymentConfiguration

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SocketConnector.initSocket(this)

        PaymentConfiguration.init(
            applicationContext,
            Constants.PUBLISHABLE_URL
        )
    }
}