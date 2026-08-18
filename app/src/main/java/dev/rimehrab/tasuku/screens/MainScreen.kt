package dev.rimehrab.tasuku.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.rimehrab.tasuku.R
import dev.rimehrab.tasuku.components.CollapsingTopAppBar
import dev.rimehrab.tasuku.components.FloatingTaskNavBar
import dev.rimehrab.tasuku.components.TaskTab
import dev.rimehrab.tasuku.data.Task
import dev.rimehrab.tasuku.util.formatDueDate
import dev.rimehrab.tasuku.util.formatDueTime
import dev.rimehrab.tasuku.viewmodel.TaskViewModel

private val TagPresets = listOf("Work", "Home", "Personal")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(taskViewModel: TaskViewModel, onSettingsClick: () -> Unit) {
    val context = LocalContext.current
    val pendingTasks by taskViewModel.pendingTasks.collectAsState()
    val completedTasks by taskViewModel.completedTasks.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(TaskTab.PENDING) }
    val tasks = if (selectedTab == TaskTab.PENDING) pendingTasks else completedTasks

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()

    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showEditSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }

    var previousTasksSize by remember { mutableIntStateOf(tasks.size) }
    LaunchedEffect(tasks.size) {
        if (tasks.size > previousTasksSize) {
            listState.animateScrollToItem(0)
        }
        previousTasksSize = tasks.size
    }

    if (showEditSheet && editingTask != null) {
        EditTaskSheet(
            task = editingTask!!,
            onDismiss = {
                showEditSheet = false
                editingTask = null
            },
            onSave = { title, description, dueDate, dueTimeMinutes, tag ->
                taskViewModel.updateTask(editingTask!!, title, description, dueDate, dueTimeMinutes, tag)
                showEditSheet = false
                editingTask = null
            }
        )
    }

    if (showAddSheet) {
        AddTaskSheet(
            onDismiss = { showAddSheet = false },
            onSave = { title, description, dueDate, dueTimeMinutes, tag ->
                taskViewModel.addTask(title, description, dueDate, dueTimeMinutes, tag)
                showAddSheet = false
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {
                CollapsingTopAppBar(
                    title = stringResource(R.string.app_name),
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .consumeWindowInsets(innerPadding),
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 96.dp,
                    top = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                    val isFirst = index == 0
                    val isLast = index == tasks.size - 1

                    Box(modifier = Modifier.animateItem()) {
                        TaskItem(
                            task = task,
                            isFirst = isFirst,
                            isLast = isLast,
                            onToggle = { taskViewModel.toggleTaskCompletion(task) },
                            onDelete = { taskViewModel.deleteTask(task) },
                            onLongClick = {
                                editingTask = task
                                showEditSheet = true
                            }
                        )
                    }
                }
            }
        }

        FloatingTaskNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            onTrashClick = { Toast.makeText(context, "WIP", Toast.LENGTH_SHORT).show() },
            onAddClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueDate: Long?, dueTimeMinutes: Int?, tag: String?) -> Unit
) {
    TaskFormSheet(
        headerText = "New Task",
        initialTitle = "",
        initialDescription = "",
        initialDueDate = null,
        initialDueTimeMinutes = null,
        initialTag = null,
        confirmLabel = "Add Task",
        onDismiss = onDismiss,
        onConfirm = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskSheet(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, dueDate: Long?, dueTimeMinutes: Int?, tag: String?) -> Unit
) {
    TaskFormSheet(
        headerText = "Edit Task",
        initialTitle = task.title,
        initialDescription = task.description,
        initialDueDate = task.dueDate,
        initialDueTimeMinutes = task.dueTimeMinutes,
        initialTag = task.tag,
        confirmLabel = "Save Changes",
        onDismiss = onDismiss,
        onConfirm = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskFormSheet(
    headerText: String,
    initialTitle: String,
    initialDescription: String,
    initialDueDate: Long?,
    initialDueTimeMinutes: Int?,
    initialTag: String?,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, dueDate: Long?, dueTimeMinutes: Int?, tag: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var dueTimeMinutes by remember { mutableStateOf(initialDueTimeMinutes) }
    var tag by remember { mutableStateOf(initialTag) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomTagField by remember { mutableStateOf(false) }
    var customTagText by remember { mutableStateOf("") }

    if (showDatePicker) {
        DueDatePickerDialog(
            initialMillis = dueDate,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                dueDate = it
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        DueTimePickerDialog(
            initialMinutes = dueTimeMinutes,
            onDismiss = { showTimePicker = false },
            onConfirm = {
                dueTimeMinutes = it
                showTimePicker = false
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = headerText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TASK NAME",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What needs to be done?") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(dueDate?.let { formatDueDate(it) } ?: "Set Due Date")
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(dueTimeMinutes?.let { formatDueTime(it) } ?: "Set Time")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DETAILED DESCRIPTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add notes or specific steps here...") },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 3
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TAGS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TagPresets.forEach { preset ->
                        TagChip(
                            label = preset,
                            icon = tagIcon(preset),
                            selected = tag == preset,
                            onClick = {
                                tag = if (tag == preset) null else preset
                                showCustomTagField = false
                            }
                        )
                    }
                }
                if (showCustomTagField) {
                    OutlinedTextField(
                        value = customTagText,
                        onValueChange = { customTagText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Custom tag") },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (customTagText.isNotBlank()) {
                                    tag = customTagText
                                }
                                showCustomTagField = false
                            }
                        )
                    )
                } else {
                    val currentTag = tag
                    val isCustomTag = currentTag != null && currentTag !in TagPresets
                    TagChip(
                        label = if (isCustomTag) currentTag!! else "+ Add Tag",
                        icon = Icons.Default.Label,
                        selected = isCustomTag,
                        onClick = {
                            customTagText = currentTag?.takeIf { it !in TagPresets } ?: ""
                            showCustomTagField = true
                        }
                    )
                }
            }

            Button(
                onClick = { onConfirm(title, description, dueDate, dueTimeMinutes, tag) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    confirmLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun tagIcon(tag: String): ImageVector = when (tag) {
    "Work" -> Icons.Default.Work
    "Home" -> Icons.Default.Home
    "Personal" -> Icons.Default.Person
    else -> Icons.Default.Label
}

@Composable
private fun TagChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        label = "tagChipBackground"
    )
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = if (!selected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(label, color = contentColor, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDatePickerDialog(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let(onConfirm)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueTimePickerDialog(
    initialMinutes: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialMinutes?.div(60) ?: 9,
        initialMinute = initialMinutes?.rem(60) ?: 0
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = { onConfirm(timePickerState.hour * 60 + timePickerState.minute) }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onLongClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (task.isCompleted)
            MaterialTheme.colorScheme.surfaceContainerLow
        else
            MaterialTheme.colorScheme.surfaceBright,
        label = "taskBackground"
    )

    val topRadius by animateDpAsState(
        targetValue = if (isFirst) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "topRadius"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isLast) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "bottomRadius"
    )

    val shape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggle() }
                )
                Text(
                    text = task.title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    color = if (task.isCompleted)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            val hasMetadata = task.dueDate != null || task.dueTimeMinutes != null || task.tag != null
            if (hasMetadata) {
                Row(
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    task.dueDate?.let {
                        MetaChip(icon = Icons.Default.CalendarToday, label = formatDueDate(it))
                    }
                    task.dueTimeMinutes?.let {
                        MetaChip(icon = Icons.Default.AccessTime, label = formatDueTime(it))
                    }
                    task.tag?.let {
                        MetaChip(icon = tagIcon(it), label = it)
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    modifier = Modifier.padding(start = 48.dp, top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun MetaChip(icon: ImageVector, label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(14.dp)
                    .padding(end = 2.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
