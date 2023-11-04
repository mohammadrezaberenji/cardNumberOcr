package com.tools.cardnumberocr.process

import android.content.Context
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tools.cardnumberocr.CardDetail
import com.tools.cardnumberocr.ExtractDataUseCase
import com.tools.cardnumberocr.Extraction
import java.util.concurrent.Executors

@ExperimentalGetImage
class ExecutionManager(private val imageAnalysis: ImageAnalysis?, private val context: Context) {
    private val TAG = "ExecutionManager"

    private val extractDataUseCase by lazy {
        ExtractDataUseCase(
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
            context
        )
    }

    private var counter = 0
    var latestCardDetail = CardDetail()
    var getLatestCardDetail: ((CardDetail) -> Unit)? = null
    private var extraction: Extraction? = null

    private fun process() {
        Log.i(TAG, "process: ")

        if (imageAnalysis == null) return

        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) {
            analyze(it)
        }
    }

    private fun analyze(imageProxy: ImageProxy?) {
        Log.i(TAG, "analyze: ")
        if (imageProxy == null) {
            Log.e(TAG, "analyze: imageProxy is null")
            return
        }

        extractDataUseCase.process(imageProxy,
            onSuccess = {
                Log.i(TAG, "analyze: onSuccess: $it")
                val cardDetail = extraction?.invoke(it.extractData)
                cardDetail?.cardColor = it.cardColor

                if (latestCardDetail.cardNumber.isEmpty())
                    latestCardDetail.cardNumber = cardDetail?.cardNumber ?: ""

                if (latestCardDetail.cvv2.isEmpty())
                    latestCardDetail.cvv2 = cardDetail?.cvv2 ?: ""

                if (latestCardDetail.expireMonth.isEmpty())
                    latestCardDetail.expireMonth = cardDetail?.expireMonth ?: ""

                if (latestCardDetail.expireYear.isEmpty())
                    latestCardDetail.expireYear = cardDetail?.expireYear ?: ""

                latestCardDetail.cardColor = cardDetail?.cardColor ?: ""

                if (latestCardDetail.cardNumber.isNotEmpty()) {
                    Log.i(TAG, "analyze: if card number is not empty : $latestCardDetail")
                    validateCardDetailsAndInvoke()

                }



                Log.i(TAG, "analyze: cardDetails: $cardDetail")
            }, onFailure = {
                Log.e(TAG, "analyze: exception: ${it.message}")
            })
    }

    fun cancelAnalyzing() {
        Log.i(TAG, "cancelAnalyzing: ")


    }

    fun startAnalyze() {
        Log.i(TAG, "startAnalyze: ")
        extraction = Extraction()
        process()
    }

    fun getLatestCardDetail(cardDetail: (CardDetail) -> Unit) {
        Log.i(TAG, "getLatestCardDetail: ")
        cardDetail.invoke(latestCardDetail)
    }

    private fun validateCardDetailsAndInvoke(){
        Log.i(TAG, "validateCardDetailsAndInvoke: ")
        if (latestCardDetail.expireMonth.isNotEmpty() && latestCardDetail.expireYear.isNotEmpty()){
            Log.d(TAG, "validateCardDetailsAndInvoke: expiry is not empty")
            imageAnalysis?.clearAnalyzer()
            getLatestCardDetail?.invoke(latestCardDetail)
            latestCardDetail = CardDetail()
            extraction = null
            counter = 0
        }else{
            Log.d(TAG, "validateCardDetailsAndInvoke: expiry is empty")
            counter++
            if (counter < 20 ){
                Log.d(TAG, "validateCardDetailsAndInvoke: counter is $counter then return function")
                return
            }
            Log.d(TAG, "validateCardDetailsAndInvoke: expiry is empty and going to invoke")
            imageAnalysis?.clearAnalyzer()
            getLatestCardDetail?.invoke(latestCardDetail)
            latestCardDetail = CardDetail()
            extraction = null
            counter = 0
        }
    }
}