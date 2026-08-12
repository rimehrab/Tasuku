package dev.rimehrab.tasuku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.rimehrab.tasuku.data.TaskDatabase
import dev.rimehrab.tasuku.navigation.About
import dev.rimehrab.tasuku.navigation.Appearance
import dev.rimehrab.tasuku.navigation.Settings
import dev.rimehrab.tasuku.navigation.Tasks
import dev.rimehrab.tasuku.screens.AboutScreen
import dev.rimehrab.tasuku.screens.AppearanceScreen
import dev.rimehrab.tasuku.screens.SettingsScreen
import dev.rimehrab.tasuku.screens.TasksScreen
import dev.rimehrab.tasuku.ui.theme.TasukuTheme
import dev.rimehrab.tasuku.viewmodel.SettingsViewModel
import dev.rimehrab.tasuku.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()

            val darkTheme = when (settingsViewModel.theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            TasukuTheme(
                darkTheme = darkTheme,
                dynamicColor = settingsViewModel.dynamicColor
            ) {
                val database = TaskDatabase.getDatabase(applicationContext)
                val taskViewModel: TaskViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return TaskViewModel(database.taskDao()) as T
                        }
                    }
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    MainNavigation(taskViewModel, settingsViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainNavigation(taskViewModel: TaskViewModel, settingsViewModel: SettingsViewModel) {
    val backStack = rememberNavBackStack(Tasks)
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(spatialSpec) { it } togetherWith fadeOut(tween(220))
        },
        popTransitionSpec = {
            fadeIn(tween(220)) togetherWith slideOutHorizontally(spatialSpec) { it }
        },
        predictivePopTransitionSpec = {
            fadeIn(tween(220)) togetherWith slideOutHorizontally(spatialSpec) { it }
        },
        entryProvider = entryProvider {
            entry<Tasks> {
                TasksScreen(
                    taskViewModel = taskViewModel,
                    onSettingsClick = { backStack.add(Settings) }
                )
            }
            entry<Settings> {
                SettingsScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToAppearance = { backStack.add(Appearance) },
                    onNavigateToAbout = { backStack.add(About) }
                )
            }
            entry<Appearance> {
                AppearanceScreen(
                    viewModel = settingsViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<About> {
                AboutScreen(onBack = { backStack.removeLastOrNull() })
            }
        }
    )
}
