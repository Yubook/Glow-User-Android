package com.android.fade

import android.os.Parcel
import android.os.Parcelable

class ServicesItem() : Parcelable {
    var service_price: Double? = null
    var service: Service? = null
    var id: Int? = null
    var isSelected : Boolean? = false

    constructor(parcel: Parcel) : this() {
        isSelected = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        service_price = parcel.readValue(Double::class.java.classLoader) as? Double
        service = parcel.readValue(Service::class.java.classLoader) as? Service
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(isSelected)
        parcel.writeValue(id)
        parcel.writeValue(service_price)
        parcel.writeValue(service)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ServicesItem> {
        override fun createFromParcel(parcel: Parcel): ServicesItem {
            return ServicesItem(parcel)
        }

        override fun newArray(size: Int): Array<ServicesItem?> {
            return arrayOfNulls(size)
        }
    }
}