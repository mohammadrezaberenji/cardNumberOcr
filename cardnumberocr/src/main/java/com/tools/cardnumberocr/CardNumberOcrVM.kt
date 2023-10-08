package com.tools.cardnumberocr

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

class CardNumberOcrVM:ViewModel() {
    val permissionFlow = MutableStateFlow<Boolean>(false)
}