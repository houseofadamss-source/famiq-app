package com.famiq.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.famiq.app.data.local.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAll(context)
            return
        }

        val requestCode = intent.getIntExtra("request_code", -1)
        if (requestCode == -1) return

        val channelId = "famiq_reminders"

        val (title, message) = when (requestCode) {
            ReminderScheduler.REQ_MORNING -> 
                "Semangat Pagi! ☀️" to "Sudah sarapan? Jangan lupa catat pengeluaran pagi ini ya."
            ReminderScheduler.REQ_AFTERNOON -> 
                "Sore yang Produktif! ☕" to "Yuk istirahat sejenak dan catat jajan siang/sore tadi."
            ReminderScheduler.REQ_EVENING -> 
                "Review Hari Ini 🌙" to "Pastikan semua pengeluaran hari ini sudah tercatat rapi di Famiq."
            else -> 
                "Waktunya Catat Keuangan! 📝" to "Yuk masukkan pengeluaran hari ini agar tetap terpantau."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(requestCode, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // Reschedule for next day
        rescheduleAll(context)
    }

    private fun rescheduleAll(context: Context) {
        val pendingResult = goAsync()
        val prefs = UserPreferences(context)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (prefs.notifMorningAktif.first()) {
                    ReminderScheduler.schedule(context, ReminderScheduler.REQ_MORNING, prefs.jamMorning.first())
                }
                if (prefs.notifAfternoonAktif.first()) {
                    ReminderScheduler.schedule(context, ReminderScheduler.REQ_AFTERNOON, prefs.jamAfternoon.first())
                }
                if (prefs.notifEveningAktif.first()) {
                    ReminderScheduler.schedule(context, ReminderScheduler.REQ_EVENING, prefs.jamEvening.first())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
