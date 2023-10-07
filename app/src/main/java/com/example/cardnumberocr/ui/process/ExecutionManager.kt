package com.example.cardnumberocr.ui.process

import android.content.Context
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.cardnumberocr.ui.CardDetail
import com.example.cardnumberocr.ui.ExtractDataUseCase
import com.example.cardnumberocr.ui.Extraction
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

@ExperimentalGetImage
class ExecutionManager(private val imageAnalysis: ImageAnalysis?,private val context: Context) {
    private val TAG = "ExecutionManager"

    private val extractDataUseCase by lazy {
        ExtractDataUseCase(TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),context)
    }
    var latestCardDetail = CardDetail()
    var getLatestCardDetail: ((CardDetail) -> Unit)? =null
    private var extraction:Extraction?=null

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
                    latestCardDetail.cardNumber = cardDetail?.cardNumber?:""

                if (latestCardDetail.cvv2.isEmpty())
                    latestCardDetail.cvv2 = cardDetail?.cvv2?:""

                if (latestCardDetail.expireMonth.isEmpty())
                    latestCardDetail.expireMonth = cardDetail?.expireMonth?:""

                if (latestCardDetail.expireYear.isEmpty())
                    latestCardDetail.expireYear = cardDetail?.expireYear?:""

                latestCardDetail.cardColor = cardDetail?.cardColor?:""

                Log.i(TAG, "analyze: cardDetails: $cardDetail")
            }, onFailure = {
                Log.e(TAG, "analyze: exception: ${it.message}")
            })
    }

    fun cancelAnalyzing() {
        Log.i(TAG, "cancelAnalyzing: ")
        imageAnalysis?.clearAnalyzer()
        getLatestCardDetail?.invoke(latestCardDetail)
        latestCardDetail = CardDetail()
        extraction = null

    }

    fun startAnalyze(){
        Log.i(TAG, "startAnalyze: ")
        extraction = Extraction()
        process()
    }

    fun getLatestCardDetail(cardDetail: (CardDetail)->Unit){
        Log.i(TAG, "getLatestCardDetail: ")
        cardDetail.invoke(latestCardDetail)
    }
}