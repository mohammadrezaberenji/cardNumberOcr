package com.example.cardnumberocr.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ExperimentalGetImage
import com.example.cardnumberocr.R
import com.tools.cardnumberocr.Builder
import com.tools.cardnumberocr.CARD_DETAILS_KEY
import com.tools.cardnumberocr.CardDetail
import com.tools.cardnumberocr.CardNumberOcrActivity

class MainActivity:ComponentActivity() {
    private val TAG = "MainActivity"
    @ExperimentalGetImage override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CardNumberOcrActivity.startAnalyze(
            activity = this,
            showBottomSheet = true,
            activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
                if (result.resultCode == Activity.RESULT_OK){
                    val resultData = result.data?.getParcelableExtra<CardDetail>(CARD_DETAILS_KEY)
                    Log.i(TAG, "onCreate: result data: $resultData")
                }
            }
        )

//        Builder(this)
//            .setCancelTime(5000)
//            .complete{
//                Log.i(TAG, "onCreate: cardDetail: $it")
//            }
//            .build()

    }
}