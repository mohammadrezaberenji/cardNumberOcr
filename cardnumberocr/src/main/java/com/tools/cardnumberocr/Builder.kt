package com.tools.cardnumberocr

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.camera.core.ExperimentalGetImage

private val TAG = "Builder"

private const val CANCEL_TIME_KEY = "cancel_time_key"
const val CARD_DETAILS_KEY = "card_details_key"

@ExperimentalGetImage
class Builder(private val activity: Activity):AnalyzeCallBack {
        private val config = Config()
        fun setCancelTime(timeInMs: Long):Builder {
            Log.i(TAG, "setCancelTime: $timeInMs")
            config.cancelDelay = timeInMs
            return this
        }

        fun build() {
            val intent = Intent(activity, CardNumberOcrActivity::class.java)
            intent.putExtra(CANCEL_TIME_KEY, config.cancelDelay)
            activity.startActivity(intent)
        }

    override fun complete(callBack: (CardDetail) -> Unit):Builder {
        Log.i(TAG, "complete: cardDetail: $callBack")
        return this
    }
}