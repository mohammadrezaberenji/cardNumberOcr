package com.example.cardnumberocr.ui

import android.util.Log

data class CardDetail(
    var cardNumber: String = "",
    var cvv2: String = "",
    var expirationDate: String = "",
    var cardColor: String = ""
) {
    fun isValidFormat():Boolean {
        Log.i("TAG", "isValidFormat: cardNumber: $cardNumber cvv2: $cvv2 expiry: $expirationDate")
        return expirationDate.isNotEmpty()
                && cvv2.isNotEmpty()
                && cardNumber.isNotEmpty()

    }
}

data class CardAppearance(
    val cardColor:String,
    val extractData:String
)