package dev.rimehrab.tasuku.reminder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_notifications")
data class ScheduledNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Int,
    val title: String,
    val details: String?,
    val triggerAtMillis: Long,
    val isCompleted: Boolean = false,
    val isSent: Boolean = false
)
