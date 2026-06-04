package com.famiq.app

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        NotificationHelper.sendNotification(applicationContext)
        return Result.success()
    }
}