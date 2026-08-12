package dev.rimehrab.tasuku.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Tasks : NavKey

@Serializable
data object Settings : NavKey

@Serializable
data object Appearance : NavKey

@Serializable
data object About : NavKey
