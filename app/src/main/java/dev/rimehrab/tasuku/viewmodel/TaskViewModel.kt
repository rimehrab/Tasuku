package dev.rimehrab.tasuku.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.rimehrab.tasuku.data.Task
import dev.rimehrab.tasuku.data.TaskDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

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
            taskDao.insertTask(
                Task(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    dueTimeMinutes = dueTimeMinutes,
                    tag = tag
                )
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
            taskDao.updateTask(
                task.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    dueTimeMinutes = dueTimeMinutes,
                    tag = tag
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun trashTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isTrashed = true))
        }
    }

    fun restoreTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isTrashed = false))
        }
    }

    fun permanentlyDeleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            taskDao.deleteAllTrashed()
        }
    }
}
