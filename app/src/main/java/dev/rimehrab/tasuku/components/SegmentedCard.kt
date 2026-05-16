package dev.rimehrab.tasuku.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedCard(
    isFirst: Boolean,
    isLast: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    val topRadius = if (isFirst) 28.dp else 4.dp
    val bottomRadius = if (isLast) 28.dp else 4.dp

    Surface(
        shape = RoundedCornerShape(
            topStart = topRadius,
            topEnd = topRadius,
            bottomStart = bottomRadius,
            bottomEnd = bottomRadius
        ),
        color = MaterialTheme.colorScheme.surfaceBright,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}