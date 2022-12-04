package com.android.fade.ui.get_location

import com.google.android.gms.maps.model.PolylineOptions


interface DirectionPointListener {
    fun onPath(polyLine: PolylineOptions?)
}