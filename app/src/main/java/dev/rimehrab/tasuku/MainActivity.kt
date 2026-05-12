package dev.rimehrab.tasuku

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.rimehrab.tasuku.data.Task
import dev.rimehrab.tasuku.data.TaskDatabase
import dev.rimehrab.tasuku.ui.theme.TasukuTheme
import dev.rimehrab.tasuku.viewmodel.SettingsViewModel
import dev.rimehrab.tasuku.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

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

val CloverShape = GenericShape { size, _ ->
    val scaleX = size.width / 320f
    val scaleY = size.height / 320f
    moveTo(308.58f * scaleX, 160f * scaleY)
    cubicTo(334.43f * scaleX, 208.16f * scaleY, 314.9f * scaleX, 251.77f * scaleY, 265.07f * scaleX, 265.06f * scaleY)
    cubicTo(251.77f * scaleX, 314.9f * scaleY, 208.16f * scaleX, 334.42f * scaleY, 160f * scaleX, 308.58f * scaleY)
    cubicTo(111.83f * scaleX, 334.43f * scaleY, 68.22f * scaleX, 314.9f * scaleY, 54.93f * scaleX, 265.07f * scaleY)
    cubicTo(5.1f * scaleX, 251.77f * scaleY, -14.43f * scaleX, 208.16f * scaleY, 11.42f * scaleX, 160f * scaleY)
    cubicTo(-14.43f * scaleX, 111.83f * scaleY, 5.1f * scaleX, 68.22f * scaleY, 54.93f * scaleX, 54.93f * scaleY)
    cubicTo(68.22f * scaleX, 5.1f * scaleY, 111.83f * scaleX, -14.43f * scaleX, 160f * scaleX, 11.42f * scaleY)
    cubicTo(208.16f * scaleX, -14.43f * scaleX, 251.77f * scaleX, 5.1f * scaleY, 265.06f * scaleX, 54.93f * scaleY)
    cubicTo(314.9f * scaleX, 68.22f * scaleY, 334.42f * scaleX, 111.83f * scaleY, 308.58f * scaleX, 160f * scaleY)
    close()
}

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Tasks : Screen("tasks", R.string.menu_tasks, Icons.AutoMirrored.Filled.List)
    object SettingsMain : Screen("settings_main", R.string.menu_settings, Icons.Default.Settings)
    object SettingsAppearance : Screen("settings_appearance", R.string.settings_appearance, Icons.Default.Palette)
    object About : Screen("about", R.string.menu_about, Icons.Default.Info)
}

@Composable
fun MainNavigation(taskViewModel: TaskViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp)
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 12.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(Screen.Tasks.titleRes), fontWeight = FontWeight.Medium) },
                    selected = currentRoute == Screen.Tasks.route,
                    onClick = {
                        navController.navigate(Screen.Tasks.route) {
                            popUpTo(Screen.Tasks.route) { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(if (currentRoute == Screen.Tasks.route) Icons.AutoMirrored.Filled.List else Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    shape = RoundedCornerShape(28.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
                NavigationDrawerItem(
                    label = { Text(stringResource(Screen.SettingsMain.titleRes), fontWeight = FontWeight.Medium) },
                    selected = currentRoute == Screen.SettingsMain.route || 
                               currentRoute == Screen.SettingsAppearance.route || 
                               currentRoute == Screen.About.route,
                    onClick = {
                        navController.navigate(Screen.SettingsMain.route) {
                            popUpTo(Screen.Tasks.route)
                        }
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(if (currentRoute == Screen.SettingsMain.route) Icons.Default.Settings else Icons.Default.Settings, contentDescription = null) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    shape = RoundedCornerShape(28.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    ) {
        NavHost(
            navController = navController, 
            startDestination = Screen.Tasks.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Tasks.route) {
                TodoScreen(taskViewModel) { scope.launch { drawerState.open() } }
            }
            composable(Screen.SettingsMain.route) {
                SettingsMainScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToAppearance = { navController.navigate(Screen.SettingsAppearance.route) },
                    onNavigateToAbout = { navController.navigate(Screen.About.route) }
                )
            }
            composable(Screen.SettingsAppearance.route) {
                SettingsAppearanceScreen(settingsViewModel) { navController.popBackStack() }
            }
            composable(Screen.About.route) {
                AboutScreen { navController.popBackStack() }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodoScreen(viewModel: TaskViewModel, onMenuClick: () -> Unit) {
    val tasks by viewModel.tasks.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    var newTaskTitle by rememberSaveable { mutableStateOf("") }

    var previousTasksSize by remember { mutableIntStateOf(tasks.size) }
    LaunchedEffect(tasks.size) {
        if (tasks.size > previousTasksSize) {
            listState.animateScrollToItem(0)
        }
        previousTasksSize = tasks.size
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .consumeWindowInsets(innerPadding),
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp, 
                end = 16.dp, 
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                top = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            placeholder = { Text("What needs to be done?") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newTaskTitle.isNotBlank()) {
                                        viewModel.addTask(newTaskTitle)
                                        newTaskTitle = ""
                                    }
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTask(newTaskTitle)
                                    newTaskTitle = ""
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    }
                }
            }

            itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                val isFirst = index == 0
                val isLast = index == tasks.size - 1
                
                Box(modifier = Modifier.animateItemPlacement()) {
                    TaskItem(
                        task = task,
                        isFirst = isFirst,
                        isLast = isLast,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskItem(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (task.isCompleted) 
            MaterialTheme.colorScheme.surfaceContainerLow 
        else 
            MaterialTheme.colorScheme.surfaceBright,
        label = "taskBackground"
    )

    val topRadius by animateDpAsState(
        targetValue = if (isFirst) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "topRadius"
    )
    val bottomRadius by animateDpAsState(
        targetValue = if (isLast) 28.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "bottomRadius"
    )

    val shape = RoundedCornerShape(
        topStart = topRadius,
        topEnd = topRadius,
        bottomStart = bottomRadius,
        bottomEnd = bottomRadius
    )

    Card(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Text(
                text = task.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                color = if (task.isCompleted) 
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    onBack: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.menu_settings),
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SegmentedCard(isFirst = true, isLast = false) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_appearance), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(stringResource(R.string.settings_appearance_summary)) },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToAppearance() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            item {
                SegmentedCard(isFirst = false, isLast = true) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.menu_about), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(stringResource(R.string.settings_about_summary)) },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToAbout() },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppearanceScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    var showThemeDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings_appearance),
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SegmentedCard(isFirst = true, isLast = false) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_theme), fontWeight = FontWeight.Medium) },
                        supportingContent = { 
                            Text(when(viewModel.theme) {
                                "light" -> stringResource(R.string.theme_light)
                                "dark" -> stringResource(R.string.theme_dark)
                                else -> stringResource(R.string.theme_system)
                            })
                        },
                        leadingContent = { Icon(Icons.Default.Brightness6, contentDescription = null) },
                        modifier = Modifier.clickable { showThemeDialog = true },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item {
                SegmentedCard(isFirst = false, isLast = true) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_dynamic_color), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(stringResource(R.string.settings_dynamic_color_summary)) },
                        leadingContent = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = viewModel.dynamicColor,
                                onCheckedChange = { viewModel.setDynamicColor(it) }
                            )
                        },
                        modifier = Modifier.clickable { viewModel.setDynamicColor(!viewModel.dynamicColor) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { 
                Text(
                    stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                ) 
            },
            text = {
                val radioOptions = listOf("system", "light", "dark")
                val labels = listOf(
                    stringResource(R.string.theme_system),
                    stringResource(R.string.theme_light),
                    stringResource(R.string.theme_dark)
                )
                
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(vertical = 8.dp)
                ) {
                    radioOptions.forEachIndexed { index, option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (option == viewModel.theme),
                                    onClick = { 
                                        viewModel.setTheme(option)
                                        showThemeDialog = false 
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (option == viewModel.theme),
                                onClick = null
                            )
                            Text(
                                text = labels[index],
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.menu_about),
                        fontWeight = FontWeight.Medium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                SegmentedCard(isFirst = true, isLast = false) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about_source_code), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(stringResource(R.string.about_source_code_summary)) },
                        leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rimehrab/Tasuku"))
                            context.startActivity(intent)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            
            item {
                SegmentedCard(isFirst = false, isLast = false) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about_contact_developer), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(stringResource(R.string.about_contact_developer_summary)) },
                        leadingContent = { Icon(Icons.Default.SupportAgent, contentDescription = null) },
                        modifier = Modifier.clickable {
                            val email = context.getString(R.string.developer_email)
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email")
                                putExtra(Intent.EXTRA_SUBJECT, "Tasuku Feedback")
                            }
                            context.startActivity(intent)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item {
                SegmentedCard(isFirst = false, isLast = true) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.about_version), fontWeight = FontWeight.Medium) },
                        supportingContent = { Text(BuildConfig.VERSION_NAME) },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }
}
