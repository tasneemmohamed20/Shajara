package com.example.moodlegovapp.data.offline.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType as WorkNetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Background worker that runs [SyncEngine.syncNow] automatically.
 *
 * Per the Moodle offline doc: "Automatic: runs regularly, every 10 minutes."
 * WorkManager's minimum guaranteed periodic interval is 15 minutes on stock
 * Android — the platform does not allow more frequent guaranteed background
 * work — so this is the closest compliant approximation. [SyncEngine.syncNow]
 * is also triggered opportunistically on app foreground/reconnect (see
 * MainActivity / ConnectivityObserver wiring) for tighter parity with the
 * documented 10-minute behaviour while the app is actually in use.
 */
class PeriodicSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            SyncEngine.getInstance(applicationContext).syncNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "moodle_offline_periodic_sync"

        /** Schedules (or re-schedules) the recurring sync job. Safe to call multiple times. */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(WorkNetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PeriodicSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
