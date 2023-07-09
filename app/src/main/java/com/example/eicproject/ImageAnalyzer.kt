package com.example.eicproject

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageAnalyzer(private val context: Context, private val listener: (Bitmap) -> Unit) : ImageAnalysis.Analyzer {

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val bitmapImg = imageProxy.image?.toBitmap()
        if (bitmapImg != null) {
            listener(bitmapImg)
        }
        else
        {
            Log.i("test", "image is null")
        }

        imageProxy.close()
    }

    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun centerCrop(srcBmp : Bitmap) : Bitmap {
        if (srcBmp.width >= srcBmp.height){

            return Bitmap.createBitmap(
                srcBmp,
                srcBmp.width /2 - srcBmp.height /2,
                0,
                srcBmp.height,
                srcBmp.height
            );

        }else{

            return Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.height /2 - srcBmp.width /2,
                srcBmp.width,
                srcBmp.width
            );
        }
    }
}