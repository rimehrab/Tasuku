package dev.rimehrab.tasuku

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import dev.rimehrab.tasuku.navigation.Trash
import dev.rimehrab.tasuku.screens.AboutScreen
import dev.rimehrab.tasuku.screens.AppearanceScreen
import dev.rimehrab.tasuku.screens.SettingsScreen
import dev.rimehrab.tasuku.screens.TasksScreen
import dev.rimehrab.tasuku.screens.TrashScreen
import dev.rimehrab.tasuku.ui.theme.TasukuTheme
import dev.rimehrab.tasuku.viewmodel.SettingsViewModel
import dev.rimehrab.tasuku.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()

            val darkTheme = when (settingsViewModel.theme) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            DisposableEffect(darkTheme) {
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                onDispose {}
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
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val enterTransition = scaleIn(animationSpec = effectsSpec, initialScale = 0.92f) +
            slideInHorizontally(animationSpec = spatialSpec, initialOffsetX = { it }) +
            fadeIn(animationSpec = effectsSpec)

    val exitTransition = scaleOut(animationSpec = effectsSpec, targetScale = 0.92f) +
            slideOutHorizontally(animationSpec = spatialSpec, targetOffsetX = { -(it * 0.12f).toInt() }) +
            fadeOut(animationSpec = effectsSpec)

    val popEnterTransition = scaleIn(animationSpec = effectsSpec, initialScale = 0.92f) +
            slideInHorizontally(animationSpec = spatialSpec, initialOffsetX = { -(it * 0.12f).toInt() }) +
            fadeIn(animationSpec = effectsSpec)

    val popExitTransition = scaleOut(animationSpec = effectsSpec, targetScale = 0.92f) +
            slideOutHorizontally(animationSpec = spatialSpec, targetOffsetX = { it }) +
            fadeOut(animationSpec = effectsSpec)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { enterTransition togetherWith exitTransition },
        popTransitionSpec = { popEnterTransition togetherWith popExitTransition },
        predictivePopTransitionSpec = { popEnterTransition togetherWith popExitTransition },
        entryProvider = entryProvider {
            entry<Tasks> {
                TasksScreen(
                    taskViewModel = taskViewModel,
                    onSettingsClick = { backStack.add(Settings) },
                    onTrashClick = { backStack.add(Trash) }
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
            entry<Trash> {
                TrashScreen(
                    taskViewModel = taskViewModel,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
