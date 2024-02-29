package com.example.rappeler

import android.os.Parcel
import android.os.Parcelable

data class Rescue(
    val id: String,
    val species: String,
    val age: Int,
    val gender: String,
    val color: String,
    val vetEvaluation: String,
    val imageString: String,
    val status: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?:"",
    parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(species)
        parcel.writeInt(age)
        parcel.writeString(gender)
        parcel.writeString(color)
        parcel.writeString(vetEvaluation)
        parcel.writeString(imageString)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Rescue> {
        override fun createFromParcel(parcel: Parcel): Rescue {
            return Rescue(parcel)
        }

        override fun newArray(size: Int): Array<Rescue?> {
            return arrayOfNulls(size)
        }
    }
}
