package com.example.cardnumberocr.ui.bottomSheet

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import com.example.cardnumberocr.R
import com.example.cardnumberocr.databinding.BottomSheetCardBinding
import com.example.cardnumberocr.ui.CardDetail
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CardBottomSheet(private val cardDetail: CardDetail?, private val cardColor: String?) :
    BottomSheetDialogFragment() {
    private val TAG = CardBottomSheet::class.java.simpleName
    private lateinit var binding: BottomSheetCardBinding

    constructor() : this(null, null) {}

    var analyzeCallBack: AnalyzeCallBack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.i(TAG, "onCreateDialog: ")
        val bottomSheetDialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener {
            val frameLayout: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (frameLayout != null) {
                val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.addBottomSheetCallback(object :
                    BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        Log.i(TAG, "onStateChanged: on state change : $newState")
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            val behevior = BottomSheetBehavior.from(bottomSheet)
                            behevior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        Log.i(TAG, "onSlide: ")
                    }
                })
            }
        }

        return bottomSheetDialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            BottomSheetCardBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        Log.i(TAG, "init: ")
        Log.i(TAG, "init: cardDetail: $cardDetail cardColor: $cardColor")
        if (!cardColor.isNullOrEmpty())
            binding.card.setCardBackgroundColor(Color.parseColor(cardColor))

        Log.i(TAG, "init: card color : $cardColor")

        if (!cardColor.isNullOrEmpty()) {
            Log.i(
                TAG,
                "init: color is dark  : ${isBackgroundColourDark(Color.parseColor(cardColor))}"
            )
        }


        binding.cardNumberTv.text =
            if (cardDetail?.cardNumber.isNullOrEmpty()) getString(R.string.card_not_found) else cardDetail?.cardNumber
        binding.expireDateTv.text = cardDetail?.concatExpireData()
        binding.cvv2Tv.text = cardDetail?.cvv2

        binding.tryAgainBtn.setOnClickListener {
            dismiss()
            analyzeCallBack?.tryAgain()
        }

        binding.okBtn.setOnClickListener {
            dismiss()
            analyzeCallBack?.complete(cardDetail!!)
        }
    }

    private fun isBackgroundColourDark(color: Int): Boolean {
        // if calculation is less than 0.25 , color considered as dark
        val darkness =
            1 - 0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color) / 255
        return darkness >= 0.5


    }
}

interface AnalyzeCallBack {
    fun tryAgain()
    fun complete(cardDetail: CardDetail)
}