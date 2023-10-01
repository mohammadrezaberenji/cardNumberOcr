package com.example.cardnumberocr.ui.process

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
class ExecutionManager(private val imageAnalysis: ImageAnalysis?) {
    private val TAG = "ExecutionManager"

    private val extractDataUseCase by lazy {
        ExtractDataUseCase(TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS))
    }
    var latestCardDetail = CardDetail()
    var getLatestCardDetail: ((CardDetail) -> Unit)? =null
    init {
        process()
    }

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
                val cardDetail = Extraction.invoke(it)

                if (latestCardDetail.cardNumber.isEmpty())
                    latestCardDetail.cardNumber = cardDetail?.cardNumber?:""

                if (latestCardDetail.cvv2.isEmpty())
                    latestCardDetail.cvv2 = cardDetail?.cvv2?:""

                if (latestCardDetail.expirationDate.isEmpty())
                    latestCardDetail.expirationDate = cardDetail?.expirationDate?:""

                Log.i(TAG, "analyze: cardDetails: $cardDetail")
            }, onFailure = {
                Log.e(TAG, "analyze: exception: ${it.message}")
            })
    }

    fun cancelAnalyzing() {
        Log.i(TAG, "cancelAnalyzing: ")
        imageAnalysis?.clearAnalyzer()
        if (latestCardDetail!= null)
            getLatestCardDetail?.invoke(latestCardDetail)
    }

    fun getLatestCardDetail(cardDetail: (CardDetail)->Unit){
        Log.i(TAG, "getLatestCardDetail: ")
        if (latestCardDetail!= null)
            cardDetail.invoke(latestCardDetail)
    }
}