package dev.rimehrab.tasuku.reminder.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ScheduledNotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: ScheduledNotification): Long

    @Query("UPDATE scheduled_notifications SET isSent = 1 WHERE id = :id")
    suspend fun markSent(id: Long)

    @Query("UPDATE scheduled_notifications SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Delete
    suspend fun delete(notification: ScheduledNotification): Int

    @Query("SELECT * FROM scheduled_notifications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduledNotification?

    @Query("SELECT * FROM scheduled_notifications WHERE taskId = :taskId LIMIT 1")
    suspend fun getByTaskId(taskId: Int): ScheduledNotification?

    @Query("DELETE FROM scheduled_notifications WHERE taskId = :taskId")
    suspend fun deleteByTaskId(taskId: Int)

    @Query("SELECT * FROM scheduled_notifications WHERE isSent = 0 AND isCompleted = 0")
    suspend fun getPendingNotifications(): List<ScheduledNotification>
}
