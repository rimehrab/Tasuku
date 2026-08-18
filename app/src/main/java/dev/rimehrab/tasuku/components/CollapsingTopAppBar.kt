package dev.rimehrab.tasuku.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp as lerpDp

private val ExpandedHeight = 152.dp
private val CollapsedHeight = 56.dp
private val ExpandedTitleStart = 24.dp
private val ExpandedTitleBottom = 32.dp
private val CollapsedTitleStartWithIcon = 72.dp
private val CollapsedTitleBottom = 14.dp
private val IconStart = 16.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    val density = LocalDensity.current
    val expandedHeightPx = with(density) { ExpandedHeight.toPx() }
    val collapsedHeightPx = with(density) { CollapsedHeight.toPx() }

    SideEffect {
        val limit = collapsedHeightPx - expandedHeightPx
        if (scrollBehavior.state.heightOffsetLimit != limit) {
            scrollBehavior.state.heightOffsetLimit = limit
        }
    }

    val fraction = scrollBehavior.state.collapsedFraction
    val barHeight = with(density) { (expandedHeightPx + scrollBehavior.state.heightOffset).toDp() }

    val collapsedTitleStart = if (navigationIcon != null) CollapsedTitleStartWithIcon else ExpandedTitleStart
    val titleStart = lerpDp(ExpandedTitleStart, collapsedTitleStart, fraction)
    val titleBottom = lerpDp(ExpandedTitleBottom, CollapsedTitleBottom, fraction)

    val expandedFontSizeSp = MaterialTheme.typography.headlineLarge.fontSize.value
    val collapsedFontSizeSp = MaterialTheme.typography.titleLarge.fontSize.value
    val targetScale = collapsedFontSizeSp / expandedFontSizeSp
    val scale = 1f - (1f - targetScale) * fraction

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(barHeight)
    ) {
        if (navigationIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .height(CollapsedHeight)
                    .padding(start = IconStart),
                contentAlignment = Alignment.CenterStart
            ) {
                navigationIcon()
            }
        }

        if (actions != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .height(CollapsedHeight)
                    .padding(end = IconStart),
                contentAlignment = Alignment.CenterEnd
            ) {
                actions()
            }
        }

        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = titleStart, bottom = titleBottom)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    transformOrigin = TransformOrigin(0f, 1f)
                ),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.headlineLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
