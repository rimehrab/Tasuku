package dev.rimehrab.tasuku.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import dev.rimehrab.tasuku.data.TaskDatabase
import dev.rimehrab.tasuku.reminder.TaskAlarmScheduler
import dev.rimehrab.tasuku.reminder.TaskNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_COMPLETED = "dev.rimehrab.tasuku.ACTION_MARK_COMPLETED"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = TaskDatabase.getDatabase(context.applicationContext)
                val taskDao = database.taskDao()
                val notificationDao = database.scheduledNotificationDao()

                when (intent.action) {
                    ACTION_MARK_COMPLETED -> {
                        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
                        val notificationId = intent.getLongExtra(
                            TaskAlarmScheduler.EXTRA_NOTIFICATION_ID,
                            -1L
                        )

                        if (taskId != -1) {
                            taskDao.setTaskCompleted(taskId, true)
                        }

                        if (notificationId != -1L) {
                            NotificationManagerCompat.from(context).cancel(notificationId.toInt())
                            notificationDao.markCompleted(notificationId)
                        }
                    }

                    else -> {
                        val notificationId = intent.getLongExtra(
                            TaskAlarmScheduler.EXTRA_NOTIFICATION_ID,
                            -1L
                        )
                        if (notificationId == -1L) return@launch

                        val notification = notificationDao.getById(notificationId) ?: return@launch
                        if (notification.isSent || notification.isCompleted) return@launch

                        TaskNotifier(context).showNotification(
                            notificationId = notificationId,
                            taskId = notification.taskId,
                            title = notification.title,
                            content = notification.details
                        )

                        notificationDao.markSent(notificationId)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
