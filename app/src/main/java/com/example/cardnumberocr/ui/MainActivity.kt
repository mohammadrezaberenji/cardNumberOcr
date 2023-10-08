package com.example.cardnumberocr.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.camera.core.ExperimentalGetImage
import com.example.cardnumberocr.R
import com.tools.cardnumberocr.CardNumberOcrActivity

class MainActivity:ComponentActivity() {
    @ExperimentalGetImage override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread.sleep(500)
        startActivity(Intent(this,CardNumberOcrActivity::class.java))
    }
}