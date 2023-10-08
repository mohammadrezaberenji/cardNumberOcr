package com.tools.cardnumberocr

interface AnalyzeCallBack {
    fun complete(callBack:(CardDetail)->Unit):Builder
}