package com.example.cardnumberocr.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.cardnumberocr.R
import com.example.cardnumberocr.databinding.ActivityCardNumberOcrBinding
import com.example.cardnumberocr.databinding.BottomSheetCardBinding
import com.example.cardnumberocr.ui.bottomSheet.CardBottomSheet
import com.example.cardnumberocr.ui.process.ExecutionManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@ExperimentalGetImage
class CardNumberOcrActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private val TAG = CardNumberOcrActivity::class.java.simpleName

    private var _binding: ActivityCardNumberOcrBinding? = null
    private val binding get() = _binding!!
    private val viewModel = CardNumberOcrVM()

    private lateinit var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null
    var boxWidth = 0
    var boxHeight = 0


    private var requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            viewModel.permissionFlow.value = isGranted
            if (isGranted) {
                Log.i(TAG, "requestCameraPermissionLauncher: user already have permission")
            } else {
                Log.i(TAG, "requestCameraPermissionLauncher: user does not have camera permission")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        _binding = ActivityCardNumberOcrBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkCameraPermission()
        prepareCameraConfig()
        initSurfaceView()
    }

    private fun prepareCameraConfig() {
        Log.i(TAG, "prepareCameraConfig: ")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        mCameraProviderFuture = ProcessCameraProvider.getInstance(this)
        mCameraProviderFuture.addListener({
            val cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(binding.preview.surfaceProvider)
            }

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(720, 1488))
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()

            val executionManager = ExecutionManager(imageAnalysis, this)
            executionManager.getLatestCardDetail = {
                Log.i(TAG, "prepareCameraConfig: callBack cardDetail: $it")
                showCardBottomSheet(it)
            }

//            imageAnalysis?.setAnalyzer(Executors.newSingleThreadExecutor()) {
//                analyze(it)
//            }

            lifecycleScope.launch {
                delay(6000)
                executionManager.cancelAnalyzing()
            }

            if (camera != null) {
                Log.d(TAG, "initView: camera is null then going to unbind")
                cameraProviderFuture.get().unbindAll()
            }

            camera = cameraProviderFuture.get()
                .bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

        }, ContextCompat.getMainExecutor(this))

    }

    private fun showCardBottomSheet(cardDetail: CardDetail) {
        Log.i(TAG, "showCardBottomSheet: cardDetail: $cardDetail")
        val cardBSH = CardBottomSheet(cardDetail = cardDetail, cardColor = cardDetail.cardColor)
        cardBSH.show(supportFragmentManager, CardBottomSheet::class.java.simpleName)
    }


    private fun checkCameraPermission(/*execute: () -> Unit*/) {
        val havePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.permissionFlow.value = havePermission
        Log.i(TAG, "checkCameraPermission: havePermission: $havePermission")
        if (havePermission) {
            viewModel.permissionFlow.value
        } else requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)

    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
        _binding = null
        super.onDestroy()
    }

    private fun initSurfaceView() {
        Log.i(TAG, "initSurfaceView: ")
        val surfaceView = binding.overLay
        surfaceView.setZOrderOnTop(true);
        val holder = surfaceView.getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);
        holder.addCallback(this)


    }

    private fun drawFocusRect(color: Int, holder: SurfaceHolder) {
        Log.i(TAG, "drawFocusRect: ")
        val displaymetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displaymetrics)
        val heigh = binding.preview.getHeight()
        val width = binding.preview.getWidth()
        val cameraHeight = heigh
        val cameraWidth = width


        val left: Int
        val right: Int
        val top: Int
        val bottom: Int
        var diameter: Int


        diameter = width

        if (heigh < width) {
            diameter = heigh
        }
        val offset = (0.05 * diameter).toInt()
        diameter -= offset
        val canvas = holder.lockCanvas()
//        canvas.drawColor(0, PorterDuff.Mode.CLEAR)

        //border's properties
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        paint.color = color
        paint.strokeWidth = 15F

        left = width / 2 - diameter / 3
        top = heigh / 2 - diameter / 3
        right = width / 2 + diameter / 3
        bottom = heigh / 2 + diameter / 3

        val xOffset = left
        val Offset = top

        boxHeight = bottom - top
        boxWidth = right - left

        Log.i(TAG, "drawFocusRect: left : $left")
        Log.i(TAG, "drawFocusRect: top : $top")
        Log.i(TAG, "drawFocusRect: right : $right")
        Log.i(TAG, "drawFocusRect: bottom : $bottom")
        Log.i(TAG, "drawFocusRect: diameter : $diameter")
        Log.i(TAG, "drawFocusRect: width : $width")
        Log.i(TAG, "drawFocusRect: height : $heigh")

        //Changing the value of x in diameter/x will change the size of the box ; inversely proportionate to x
        canvas.drawRect(0F, top.toFloat(), width.toFloat(), bottom.toFloat(), paint)

        val newPaint = Paint()
        newPaint.color = ContextCompat.getColor(this, R.color.black_transparent)
        canvas.drawRect(0f, 0f, width.toFloat(), top.toFloat(), newPaint)
        canvas.drawRect(0f, diameter.toFloat() - 2 * offset + diameter / 2 , width.toFloat(), heigh.toFloat(), newPaint)

        Log.i(TAG, "drawFocusRect: diameter + diameter / 2 : ${diameter.toFloat() + diameter / 2 - offset}" )
        holder.unlockCanvasAndPost(canvas)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        Log.i(TAG, "surfaceCreated: ")

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        Log.i(TAG, "surfaceChanged: ")
        drawFocusRect(Color.parseColor("#b3dabb"), p0);

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        Log.i(TAG, "surfaceDestroyed: ")
    }
}