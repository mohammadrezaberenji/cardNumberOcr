package com.tools.cardnumberocr

import android.util.Log

data class CardDetail(
    var cardNumber: String = "",
    var cvv2: String = "",
    var expireMonth :String= "",
    var expireYear :String= "",
    var cardColor: String = ""
) {
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