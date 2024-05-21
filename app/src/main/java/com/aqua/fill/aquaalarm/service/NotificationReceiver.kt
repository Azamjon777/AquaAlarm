package com.aqua.fill.aquaalarm.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aqua.fill.aquaalarm.MainActivity
import com.aqua.fill.aquaalarm.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val channelId = "channel1"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = "Time to drink water"
        val time = intent.getLongExtra("time", System.currentTimeMillis())

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeString = dateFormat.format(Date(time))

        val fullMessage = "$message at $timeString"
        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText(fullMessage)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(message.hashCode(), notification)
    }
}