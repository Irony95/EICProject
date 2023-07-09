package com.example.eicproject

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.lang.Exception
import java.util.concurrent.TimeUnit

class UpdateWorker(context : Context, param : WorkerParameters) : Worker(context, param) {
    override fun doWork(): Result {
        try {
            Log.i("test", "worker is running")
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//            val shortPeriodicWorkRequest = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
//                .setConstraints(constraints)
//                .setInputData(inputData)
//                .setInitialDelay(1, TimeUnit.SECONDS)
//                .build()
//
//            WorkManager.getInstance(applicationContext).enqueue(shortPeriodicWorkRequest)
            return Result.success()
        }
        catch (e : Exception) {
            return Result.failure()
        }

    }

}