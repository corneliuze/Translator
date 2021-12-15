package com.example.translator
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import android.os.Parcelable

@Parcelize
data class Result(
    @SerializedName("examples")
    val examples: List<String>,
    @SerializedName("word")
    val word: List<String>
) : Parcelable