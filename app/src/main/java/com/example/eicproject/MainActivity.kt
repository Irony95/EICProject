package com.example.eicproject

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.example.eicproject.database.AppDatabase
import com.example.eicproject.database.PlantDao
import com.example.eicproject.database.PlantData
import com.example.eicproject.databinding.ActivityMainBinding
import com.example.eicproject.server.WebServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel

    private var readingsService : ReadingsService? = null
    private lateinit var plantDao : PlantDao

    private var server : WebServer = WebServer(this)
    var imageCapture : ImageCapture? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        plantDao = AppDatabase.getInstance(application).plantDao()

        val factory = MainActivityViewModelFactory(plantDao)
        viewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]

        startCamera()

        //start webserver
        val port = 8000
        server.startServer(port)
        val manager : WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ip : String = android.text.format.Formatter.formatIpAddress(manager.connectionInfo.ipAddress)
        binding.tvIpAddress.text = ip + String.format(":%d", port)


        viewModel.plantData.observe(this) {
            val shortened = it.takeLast(10).reversed()
            server.setNewSentData(shortened)
        }
        viewModel.plantImage.observe(this) {
            binding.imgPlant.setImageBitmap(it)

        }


        binding.btnConnect.setOnClickListener {
            if (readingsService == null) {
                try {
                    val url = binding.etSensorIP.text.toString()
                    readingsService = RetrofitInstance.getInstance("http://$url").create(ReadingsService::class.java)
                    if (readingsService != null) {
                        binding.btnConnect.text = "Disconnect"
                        binding.etSensorIP.isEnabled = false
                        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show()

                        startRequestingFromSensors(1)
                    }
                }
                catch (e : Exception) {
                    Toast.makeText(this, "smth went wrong, ip wrong?", Toast.LENGTH_SHORT).show()
                    Log.i("test", e.toString())
                }
            }
            else {
                readingsService = null
                binding.btnConnect.text = "Connect"
                binding.etSensorIP.isEnabled = true
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun startRequestingFromSensors(seconds : Int) {
        lifecycleScope.launch {
            while (readingsService != null) {
                val stream = ByteArrayOutputStream()
                viewModel.plantImage.value?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val byteArray = stream.toByteArray()
                server.setNewImage(byteArray)

                val responseLiveData : LiveData<Response<String>> = liveData {
                    val response = readingsService!!.getReadings()
                    emit(response)
                }
                responseLiveData.observe(this@MainActivity) {
                    val body = it.body()
                    if (body != null) {
                        val newData = PlantData(0, 1, System.currentTimeMillis(), body.toFloat())
                        viewModel.addPlantData(newData)
                        Log.i("test", body.toString())
                    }
                }
                delay((seconds * 1000).toLong())
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val analyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, ImageAnalyzer(this) {
                    Log.i("test", "asdfasdf")
                    viewModel.setNewImage(it)
                })
            }

        cameraProviderFuture.addListener({
            val cameraProvider : ProcessCameraProvider = cameraProviderFuture.get()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, analyzer)

            }
            catch (e : Exception) {
                Log.e("test", "error happened lmao")
            }

        }, ContextCompat.getMainExecutor(this))
    }
}