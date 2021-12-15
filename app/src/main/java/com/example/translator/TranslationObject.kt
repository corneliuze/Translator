package com.example.translator
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class TranslationObject(
    @SerializedName("result")
    val result: Result
) : Parcelable