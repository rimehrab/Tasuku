package dev.rimehrab.tasuku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rimehrab.tasuku.data.Task
import dev.rimehrab.tasuku.data.TaskDao
import dev.rimehrab.tasuku.reminder.AlarmScheduler
import dev.rimehrab.tasuku.reminder.data.ScheduledNotification
import dev.rimehrab.tasuku.reminder.data.ScheduledNotificationDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class TaskViewModel(
    private val taskDao: TaskDao,
    private val notificationDao: ScheduledNotificationDao,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pendingTasks: StateFlow<List<Task>> = tasks
        .map { list -> list.filter { !it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedTasks: StateFlow<List<Task>> = tasks
        .map { list -> list.filter { it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashedTasks: StateFlow<List<Task>> = taskDao.getTrashedTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form / Bottom Sheet UI State preserved across configuration changes (rotation)
    var isAddSheetOpen by mutableStateOf(false)
        private set

    var editingTask by mutableStateOf<Task?>(null)
        private set

    val isSheetOpen: Boolean
        get() = isAddSheetOpen || editingTask != null

    var formTitle by mutableStateOf("")
    var formDescription by mutableStateOf("")
    var formDueDate by mutableStateOf<Long?>(null)
    var formDueTimeMinutes by mutableStateOf<Int?>(null)
    var formTag by mutableStateOf<String?>(null)
    var formCustomTagText by mutableStateOf("")
    var formShowCustomTagField by mutableStateOf(false)
    var formShowDatePicker by mutableStateOf(false)
    var formShowTimePicker by mutableStateOf(false)

    fun openAddSheet() {
        editingTask = null
        formTitle = ""
        formDescription = ""
        formDueDate = null
        formDueTimeMinutes = null
        formTag = null
        formCustomTagText = ""
        formShowCustomTagField = false
        formShowDatePicker = false
        formShowTimePicker = false
        isAddSheetOpen = true
    }

    fun openEditSheet(task: Task) {
        isAddSheetOpen = false
        editingTask = task
        formTitle = task.title
        formDescription = task.description
        formDueDate = task.dueDate
        formDueTimeMinutes = task.dueTimeMinutes
        formTag = task.tag
        val presets = listOf("Work", "Home", "Personal")
        formCustomTagText = if (task.tag != null && task.tag !in presets) task.tag else ""
        formShowCustomTagField = false
        formShowDatePicker = false
        formShowTimePicker = false
    }

    fun closeSheet() {
        isAddSheetOpen = false
        editingTask = null
        formShowDatePicker = false
        formShowTimePicker = false
    }

    fun saveCurrentForm() {
        val currentEdit = editingTask
        if (currentEdit != null) {
            updateTask(
                task = currentEdit,
                title = formTitle,
                description = formDescription,
                dueDate = formDueDate,
                dueTimeMinutes = formDueTimeMinutes,
                tag = formTag
            )
        } else if (isAddSheetOpen) {
            addTask(
                title = formTitle,
                description = formDescription,
                dueDate = formDueDate,
                dueTimeMinutes = formDueTimeMinutes,
                tag = formTag
            )
        }
        closeSheet()
    }

    fun addTask(
        title: String,
        description: String = "",
        dueDate: Long? = null,
        dueTimeMinutes: Int? = null,
        tag: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val insertedId = taskDao.insertTask(
                Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    dueTimeMinutes = dueTimeMinutes,
                    tag = tag
                )
            )
            syncTaskReminder(
                taskId = insertedId.toInt(),
                title = title,
                description = description,
                dueDate = dueDate,
                dueTimeMinutes = dueTimeMinutes,
                isCompleted = false,
                isTrashed = false
            )
        }
    }

    fun updateTask(
        task: Task,
        title: String,
        description: String = "",
        dueDate: Long? = null,
        dueTimeMinutes: Int? = null,
        tag: String? = null
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val updated = task.copy(
                title = title,
                description = description,
                dueDate = dueDate,
                dueTimeMinutes = dueTimeMinutes,
                tag = tag
            )
            taskDao.updateTask(updated)
            syncTaskReminder(
                taskId = task.id,
                title = title,
                description = description,
                dueDate = dueDate,
                dueTimeMinutes = dueTimeMinutes,
                isCompleted = task.isCompleted,
                isTrashed = task.isTrashed
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val newStatus = !task.isCompleted
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = newStatus))
            syncTaskReminder(
                taskId = task.id,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTimeMinutes = task.dueTimeMinutes,
                isCompleted = newStatus,
                isTrashed = task.isTrashed
            )
        }
    }

    fun trashTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isTrashed = true))
            val existing = notificationDao.getByTaskId(task.id)
            if (existing != null) {
                alarmScheduler.cancel(existing)
                notificationDao.deleteByTaskId(task.id)
            }
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isTrashed = false))
            syncTaskReminder(
                taskId = task.id,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                dueTimeMinutes = task.dueTimeMinutes,
                isCompleted = task.isCompleted,
                isTrashed = false
            )
        }
    }

    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
            val existing = notificationDao.getByTaskId(task.id)
            if (existing != null) {
                alarmScheduler.cancel(existing)
                notificationDao.deleteByTaskId(task.id)
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            taskDao.deleteAllTrashed()
        }
    }

    private suspend fun syncTaskReminder(
        taskId: Int,
        title: String,
        description: String,
        dueDate: Long?,
        dueTimeMinutes: Int?,
        isCompleted: Boolean,
        isTrashed: Boolean
    ) {
        val existing = notificationDao.getByTaskId(taskId)
        if (existing != null) {
            alarmScheduler.cancel(existing)
            notificationDao.deleteByTaskId(taskId)
        }

        if (isCompleted || isTrashed || dueDate == null) return

        val triggerMillis = calculateTriggerMillis(dueDate, dueTimeMinutes) ?: return
        if (triggerMillis <= System.currentTimeMillis()) return

        val notification = ScheduledNotification(
            taskId = taskId,
            title = title,
            details = description.takeIf { it.isNotBlank() },
            triggerAtMillis = triggerMillis,
            isCompleted = false,
            isSent = false
        )
        val id = notificationDao.insert(notification)
        alarmScheduler.schedule(notification.copy(id = id))
    }

    private fun calculateTriggerMillis(dueDate: Long?, dueTimeMinutes: Int?): Long? {
        if (dueDate == null) return null
        return try {
            val localDate = Instant.ofEpochMilli(dueDate).atZone(ZoneId.of("UTC")).toLocalDate()
            val hour = dueTimeMinutes?.div(60) ?: 9
            val minute = dueTimeMinutes?.rem(60) ?: 0
            val localTime = LocalTime.of(hour, minute)
            localDate.atTime(localTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }
    }
}
