package dev.rimehrab.tasuku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.rimehrab.tasuku.data.TaskDatabase
import dev.rimehrab.tasuku.screens.MainScreen
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

@OptIn(ExperimentalVoyagerApi::class)
@Composable
fun MainNavigation(taskViewModel: TaskViewModel, settingsViewModel: SettingsViewModel) {
    Navigator(MainScreen(taskViewModel, settingsViewModel)) { navigator ->
        SlideTransition(
            navigator = navigator,
            disposeScreenAfterTransitionEnd = true,
            animationSpec = spring(
                stiffness = Spring.StiffnessMedium,
                visibilityThreshold = IntOffset(1, 1)
            )
        )
    }
}
