package dev.rimehrab.tasuku.viewmodel

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

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}
