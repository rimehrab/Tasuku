package dev.rimehrab.tasuku.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dev.rimehrab.tasuku.reminder.data.ScheduledNotification
import dev.rimehrab.tasuku.reminder.receiver.AlarmReceiver

interface AlarmScheduler {
    fun schedule(notification: ScheduledNotification)
    fun cancel(notification: ScheduledNotification)
}

class TaskAlarmScheduler(private val context: Context) : AlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    companion object {
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
    }

    override fun schedule(notification: ScheduledNotification) {
        if (notification.triggerAtMillis <= System.currentTimeMillis() ||
            notification.isSent ||
            notification.isCompleted
        ) {
            return
        }

        val pendingIntent = createPendingIntent(notification)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notification.triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        notification.triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notification.triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notification.triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(notification: ScheduledNotification) {
        val pendingIntent = createPendingIntent(notification)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun createPendingIntent(notification: ScheduledNotification): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notification.id)
        }
        return PendingIntent.getBroadcast(
            context,
            notification.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
