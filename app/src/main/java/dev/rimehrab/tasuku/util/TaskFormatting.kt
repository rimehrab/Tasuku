package dev.rimehrab.tasuku.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDueDate(millis: Long): String {
    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))
}

fun formatDueTime(minutes: Int): String {
    val time = LocalTime.of(minutes / 60, minutes % 60)
    return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
}
