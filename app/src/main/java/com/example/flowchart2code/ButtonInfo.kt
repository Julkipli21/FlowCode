package com.example.flowchart2code

import android.os.Parcel
import android.os.Parcelable

data class ButtonInfo(val buttonText: String, val formattedText: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(buttonText)
        parcel.writeString(formattedText)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ButtonInfo> {
        override fun createFromParcel(parcel: Parcel): ButtonInfo {
            return ButtonInfo(parcel)
        }

        override fun newArray(size: Int): Array<ButtonInfo?> {
            return arrayOfNulls(size)
        }
    }
}