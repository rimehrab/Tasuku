package dev.rimehrab.tasuku.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.rimehrab.tasuku.data.TaskDatabase
import dev.rimehrab.tasuku.reminder.TaskAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = TaskDatabase.getDatabase(context.applicationContext)
                    val notificationDao = database.scheduledNotificationDao()
                    val scheduler = TaskAlarmScheduler(context)

                    val activeNotifications = notificationDao.getPendingNotifications()
                    val now = System.currentTimeMillis()

                    for (notification in activeNotifications) {
                        if (notification.triggerAtMillis > now) {
                            scheduler.schedule(notification)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
