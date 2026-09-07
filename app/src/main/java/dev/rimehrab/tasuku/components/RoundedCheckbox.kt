package dev.rimehrab.tasuku.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedCheckbox(
    checked: Boolean,
    onValueChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    enabled: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val transition = updateTransition(targetState = checked, label = "checkbox")

    val checkboxColor by transition.animateColor(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(durationMillis = 150)
            } else {
                tween(durationMillis = 180, delayMillis = 40)
            }
        },
        label = "checkboxColor"
    ) { isChecked ->
        if (isChecked) MaterialTheme.colorScheme.primary else Color.Transparent
    }

    val borderColor by transition.animateColor(
        transitionSpec = { tween(durationMillis = 180) },
        label = "borderColor"
    ) { isChecked ->
        if (isChecked) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline
    }

    val checkColor = MaterialTheme.colorScheme.onPrimary

    val checkDrawFraction by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                tween(durationMillis = 200, easing = FastOutSlowInEasing)
            } else {
                tween(durationMillis = 150, easing = FastOutSlowInEasing)
            }
        },
        label = "checkDrawFraction"
    ) { isChecked ->
        if (isChecked) 1f else 0f
    }

    val strokeWidthPx = with(LocalDensity.current) { 2.dp.toPx() }
    val checkCache = remember { CheckDrawingCache() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(checkboxColor)
            .border(2.dp, borderColor, CircleShape)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = { newValue ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onValueChange(newValue)
                }
            )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            drawCheck(
                checkColor = checkColor,
                checkFraction = checkDrawFraction,
                stroke = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                drawingCache = checkCache
            )
        }
    }
}

private fun DrawScope.drawCheck(
    checkColor: Color,
    checkFraction: Float,
    stroke: Stroke,
    drawingCache: CheckDrawingCache,
) {
    if (checkFraction <= 0f) return
    val width = size.width

    val checkCrossX = 0.42f
    val checkCrossY = 0.72f
    val leftX = 0.22f
    val leftY = 0.52f
    val rightX = 0.78f
    val rightY = 0.28f

    with(drawingCache) {
        checkPath.rewind()
        checkPath.moveTo(x = width * leftX, y = width * leftY)
        checkPath.lineTo(x = width * checkCrossX, y = width * checkCrossY)
        checkPath.lineTo(x = width * rightX, y = width * rightY)

        pathMeasure.setPath(checkPath, forceClosed = false)
        pathToDraw.rewind()
        pathMeasure.getSegment(
            startDistance = 0f,
            stopDistance = pathMeasure.length * checkFraction,
            destination = pathToDraw,
            startWithMoveTo = true
        )
    }

    drawPath(drawingCache.pathToDraw, checkColor, style = stroke)
}

@Immutable
private class CheckDrawingCache(
    val checkPath: Path = Path(),
    val pathMeasure: PathMeasure = PathMeasure(),
    val pathToDraw: Path = Path(),
)
