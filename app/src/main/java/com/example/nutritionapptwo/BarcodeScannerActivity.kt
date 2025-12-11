package com.example.nutritionapptwo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


class BarcodeScannerActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var scanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        previewView = PreviewView(this)
        setContentView(previewView)

        scanner = BarcodeScanning.getClient()

        // Request camera permission at runtime if needed
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to scan barcodes",
                    Toast.LENGTH_SHORT
                ).show()
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Check the device has a back camera available
            if (!cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                Toast.makeText(this, "No back camera available", Toast.LENGTH_SHORT).show()
                setResult(RESULT_CANCELED)
                finish()
                return@addListener
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder().build().also { analyzer ->
                analyzer.setAnalyzer(
                    Executors.newSingleThreadExecutor()
                ) { imageProxy ->
                    processImageProxy(scanner, imageProxy)
                }
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to start camera: ${e.message}", Toast.LENGTH_LONG).show()
                setResult(RESULT_CANCELED)
                finish()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        scanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { value ->
                        // return scanned value and mealName (if provided) to calling activity
                        val mealName = intent.getStringExtra("mealName")
                        val resultIntent = Intent().putExtra("barcode", value).putExtra("mealName", mealName)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
