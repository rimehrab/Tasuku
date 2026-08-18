package dev.rimehrab.tasuku.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class TaskTab {
    PENDING,
    COMPLETED
}

private val NavTrackColor = Color(0xFF1C1B1F)
private val NavContentColor = Color(0xFFF5F0F7)

@Composable
fun FloatingTaskNavBar(
    selectedTab: TaskTab,
    onTabSelected: (TaskTab) -> Unit,
    onTrashClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = NavTrackColor,
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavSegment(
                    label = "Pending",
                    icon = Icons.Default.Schedule,
                    selected = selectedTab == TaskTab.PENDING,
                    onClick = { onTabSelected(TaskTab.PENDING) }
                )
                NavSegment(
                    label = "Completed",
                    icon = Icons.Default.CheckCircle,
                    selected = selectedTab == TaskTab.COMPLETED,
                    onClick = { onTabSelected(TaskTab.COMPLETED) }
                )
            }
        }

        NavCircleButton(
            icon = Icons.Default.Delete,
            contentDescription = "Trash",
            onClick = onTrashClick
        )

        NavCircleButton(
            icon = Icons.Default.Add,
            contentDescription = "Add Task",
            onClick = onAddClick
        )
    }
}

@Composable
private fun NavCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = NavTrackColor,
        modifier = Modifier
            .padding(start = 8.dp)
            .size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = NavContentColor
            )
        }
    }
}

@Composable
private fun NavSegment(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) NavContentColor else Color.Transparent,
        label = "navSegmentBackground"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) NavTrackColor else NavContentColor,
        label = "navSegmentContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        modifier = Modifier.height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
