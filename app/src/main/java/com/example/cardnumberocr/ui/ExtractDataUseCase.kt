package com.example.cardnumberocr.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer


@ExperimentalGetImage
class ExtractDataUseCase(private val textRecognizer: TextRecognizer, private val context: Context) {
    private val TAG = "ExtractDataUseCase"
    fun process(
        imageProxy: ImageProxy,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (imageProxy.image == null) return
        val imageInput =
            InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        val bimap = imageProxy.toBitmap()


        Log.i(TAG, "process: ")

        val displayMetrics = DisplayMetrics()

        val activity = context as CardNumberOcrActivity

        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = bimap.height
        val width = bimap.width

        var left = 0
        var right = 0
        var top = 0
        var bottom = 0
        var diameter = 0

        diameter = width;

        val offset = (0.05 * diameter)
        diameter -= offset.toInt()

        Log.i(TAG, "process: height : $height")
        Log.i(TAG, "process: width : $width")
        Log.i(TAG, "process: diameter : $diameter")

        if (height < width) {
            diameter = height
        }



        left = width / 2 - diameter / 3;
        top = height / 2 - diameter / 3;
        right = width / 2 + diameter / 3;
        bottom = height / 2 + diameter / 3;

        Log.i(TAG, "process: image width : ${imageProxy.image!!.width}")
        Log.i(TAG, "process: image height : ${imageProxy.image!!.height}")

        Log.i(TAG, "process: left : $left")
        Log.i(TAG, "process: top : $top")
        Log.i(TAG, "process: right : $right")
        Log.i(TAG, "process:  bottom : $bottom")

        Log.i(TAG, "process: bitmap width : ${bimap.width}")
        Log.i(TAG, "process: bitmap height : ${bimap.height}")

        Log.i(TAG, "process: box width : ${activity.boxWidth}")
        Log.i(TAG, "process: box height : ${activity.boxHeight}")


        val newBitMap = Bitmap.createBitmap(bimap, left, top, activity.boxWidth, activity.boxHeight)

        Log.i(TAG, "process: new bitmap width : ${newBitMap.width}")
        Log.i(TAG, "process: new bitmap height ; ${newBitMap.height}")

        val pixel = newBitMap.getPixel((newBitMap.width / 2), 0 )
        val red = Color.red(pixel)
        val blue = Color.blue(pixel)
        val green = Color.green(pixel)
        val hexColor = String.format("#%02x%02x%02x", red, green, blue)
        Log.d(TAG, "Color: ${hexColor}")


        val p = textRecognizer.process(
            InputImage.fromBitmap(
                newBitMap,
                imageProxy.imageInfo.rotationDegrees
            )
        )
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