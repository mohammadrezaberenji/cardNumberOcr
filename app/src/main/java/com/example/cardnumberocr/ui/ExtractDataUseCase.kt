package com.example.cardnumberocr.ui

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer

@ExperimentalGetImage
class ExtractDataUseCase(private val textRecognizer: TextRecognizer) {
    private val TAG = "ExtractDataUseCase"
    fun process(imageProxy: ImageProxy, onSuccess:(String)->Unit, onFailure:(Exception)->Unit) {
        Log.i(TAG, "process: bitmap: ${imageProxy.toBitmap()}")
        if (imageProxy.image==null) return
        val imageInput = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)
        val p = textRecognizer.process(imageInput)
        p.addOnSuccessListener {
            onSuccess.invoke(it.text)
        }
        p.addOnFailureListener {
            onFailure.invoke(it)
        }
        p.addOnCompleteListener {
            imageProxy.close()
        }
    }

}