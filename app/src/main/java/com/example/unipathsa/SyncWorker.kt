package com.example.unipathsa

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ok = CacheRepository(applicationContext).refreshAll()
        return if (ok) Result.success() else Result.retry()
    }
}