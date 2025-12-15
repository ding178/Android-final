package com.example.dailymood_best

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val database = MoodDatabase.getDatabase(context)

        // 使用 Coroutine 在背景檢查資料庫
        CoroutineScope(Dispatchers.IO).launch {
            val today = LocalDate.now().toString()
            val existingEntry = database.moodDao().getMoodByDate(today) // 需在 Dao 新增此方法

            // 如果今天沒有紀錄 (null)，發送通知
            if (existingEntry == null) {
                showNotification(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        val channelId = "daily_mood_reminder"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "每日心情提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        // 點擊通知打開 App
        val contentIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            android.app.PendingIntent.getActivity(context, 0, it, android.app.PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_edit) // 可以換成你的 icon
            .setContentTitle("今天過得還好嗎？🐨")
            .setContentText("無尾熊在等你紀錄今天的心情喔！")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}