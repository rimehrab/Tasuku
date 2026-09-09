package dev.rimehrab.tasuku.screens

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.rimehrab.tasuku.R
import dev.rimehrab.tasuku.components.CollapsingTopAppBar
import dev.rimehrab.tasuku.data.Task
import dev.rimehrab.tasuku.util.formatDueDate
import dev.rimehrab.tasuku.util.formatDueTime
import dev.rimehrab.tasuku.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrashScreen(
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val trashedTasks by taskViewModel.trashedTasks.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showEmptyTrashDialog by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.empty_trash),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.empty_trash_confirmation),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        taskViewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.empty_trash_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CollapsingTopAppBar(
                title = stringResource(R.string.menu_trash),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (trashedTasks.isNotEmpty()) {
                        FilledTonalIconButton(onClick = { showEmptyTrashDialog = true }) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = stringResource(R.string.empty_trash),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (trashedTasks.isEmpty()) {
                item(key = "trash_empty_state") {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(bottom = if (isLandscape) 32.dp else 64.dp)
                            .animateItem(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(if (isLandscape) 16.dp else 32.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(if (isLandscape) 18.dp else 24.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(if (isLandscape) 56.dp else 72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(if (isLandscape) 28.dp else 36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 16.dp))
                            Text(
                                text = stringResource(R.string.trash_empty),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.trash_empty_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = trashedTasks,
                    key = { _, task -> task.id }
                ) { index, task ->
                    val isFirst = index == 0
                    val isLast = index == trashedTasks.size - 1

                    Box(modifier = Modifier.animateItem()) {
                        TrashedTaskItem(
                            task = task,
                            isFirst = isFirst,
                            isLast = isLast,
                            onRestore = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                taskViewModel.restoreTask(task)
                            },
                            onDeleteForever = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                taskViewModel.permanentlyDeleteTask(task)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrashedTaskItem(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit
) {
    val topRadius by animateDpAsState(
        targetValue = if (isFirst) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "trashTopRadius"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isLast) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "trashBottomRadius"
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
            .clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRestore) {
                    Icon(
                        Icons.Default.Restore,
                        contentDescription = stringResource(R.string.restore_task),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteForever) {
                    Icon(
                        Icons.Default.DeleteForever,
                        contentDescription = stringResource(R.string.delete_forever),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            val hasMetadata = task.dueDate != null || task.dueTimeMinutes != null || task.tag != null
            if (hasMetadata) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
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
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun tagIcon(tag: String): ImageVector = when (tag) {
    "Work" -> Icons.Default.Work
    "Home" -> Icons.Default.Home
    "Personal" -> Icons.Default.Person
    else -> Icons.AutoMirrored.Filled.Label
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
