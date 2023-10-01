package com.example.cardnumberocr.ui

import android.util.Log

data class CardDetail(
    var cardNumber: String = "",
    var cvv2: String = "",
    var expirationDate: String = ""
) {
    fun isValidFormat():Boolean {
        Log.i("TAG", "isValidFormat: cardNumber: $cardNumber cvv2: $cvv2 expiry: $expirationDate")
        return expirationDate.isNotEmpty()
                && cvv2.isNotEmpty()
                && cardNumber.isNotEmpty()

    }
}