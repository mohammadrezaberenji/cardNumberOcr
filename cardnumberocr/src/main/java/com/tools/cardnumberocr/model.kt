package com.tools.cardnumberocr

import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CardDetail(
    var cardNumber: String = "",
    var cvv2: String = "",
    var expireMonth :String= "",
    var expireYear :String= "",
    var cardColor: String = ""
):Parcelable {
    fun concatExpireData():String{
        return if (expireMonth.isNotEmpty() && expireYear.isNotEmpty())
            "$expireYear/$expireMonth"
        else
            ""
    }
}

data class CardAppearance(
    val cardColor:String,
    val extractData:String
)

data class Config(
    var cancelDelay:Long = 5000 // in milli sec
)

@Parcelize
data class AnalyzeListener(val complete:(CardDetail)-> Unit):Parcelable