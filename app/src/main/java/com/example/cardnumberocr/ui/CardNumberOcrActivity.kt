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
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.cardnumberocr.databinding.ActivityCardNumberOcrBinding
import com.example.cardnumberocr.ui.process.ExecutionManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


@ExperimentalGetImage
class CardNumberOcrActivity : ComponentActivity(), SurfaceHolder.Callback {

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
        window
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

            val executionManager = ExecutionManager(imageAnalysis)
            executionManager.getLatestCardDetail = {
                Log.i(TAG, "prepareCameraConfig: callBack cardDetail: $it")
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
        canvas.drawColor(0, PorterDuff.Mode.CLEAR)
        //border's properties
        val paint = Paint()
        paint.setStyle(Paint.Style.STROKE)
        paint.setColor(color)
        paint.setStrokeWidth(5F)

        left = width / 2 - diameter / 3
        top = heigh / 2 - diameter / 3
        right = width / 2 + diameter / 3
        bottom = heigh / 2 + diameter / 3

        val xOffset = left
        val Offset = top

        boxHeight = bottom - top
        boxWidth = right - left

        //Changing the value of x in diameter/x will change the size of the box ; inversely proportionate to x
        canvas.drawRect(0F, top.toFloat(), width.toFloat(), bottom.toFloat(), paint)
        holder.unlockCanvasAndPost(canvas)
    }

    override fun surfaceCreated(p0: SurfaceHolder) {

    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
        drawFocusRect(Color.parseColor("#b3dabb"), p0);

    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }
}

