package com.kayak.yakak.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kayak.yakak.R
import com.kayak.yakak.data.RecurrenceFrequency
import com.kayak.yakak.data.Task
import com.kayak.yakak.ui.calc.CalcView
import com.kayak.yakak.ui.calendar.CalendarVM
import com.kayak.yakak.ui.calendar.CalendarView
import com.kayak.yakak.ui.maps.MapsVM
import com.kayak.yakak.ui.maps.MapsView
import com.kayak.yakak.ui.settings.SettingsView
import com.kayak.yakak.ui.settings.permissions.PermissionsView
import com.kayak.yakak.ui.tasklist.EditBirthdayView
import com.kayak.yakak.ui.tasklist.EditView
import com.kayak.yakak.ui.tasklist.TaskEvent
import com.kayak.yakak.ui.tasklist.TaskListVM
import com.kayak.yakak.ui.tasklist.TaskListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.time.LocalDateTime
import java.time.LocalTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    scrollBehavior: TopAppBarScrollBehavior,
    pagerState: PagerState,
    scope: CoroutineScope,
    navController: NavHostController,
    taskListVM: TaskListVM,
    calendarVM: CalendarVM,
    mapsVM: MapsVM,
    onTopBarClick: () -> Unit = {},
){

    val titles = listOf(
        stringResource(R.string.nav_agenda),
        stringResource(R.string.nav_tasks),
        stringResource(R.string.nav_maps)
    )
    val subtitles = listOf(
        "",
        stringResource(R.string.sub_todo),
        stringResource(R.string.sub_maps)
    )
    val selectedDay by calendarVM.selectedDay.collectAsState()
    val taskCounts by calendarVM.taskCounts.collectAsState()

    // Optimisation : Utiliser derivedStateOf pour éviter des recompositions inutiles de la TopBar lors du scroll
    val currentTitle by remember(selectedDay, titles, pagerState.targetPage) {
        derivedStateOf {
            if (pagerState.targetPage == 0) selectedDay.month.toString()
            else titles[pagerState.targetPage]
        }
    }

    val taskCountText = stringResource(R.string.task_count_month, taskCounts[selectedDay] ?: 0)
    val currentSubtitle by remember(selectedDay, taskCounts, subtitles, pagerState.targetPage, taskCountText) {
        derivedStateOf {
            if (pagerState.targetPage == 0 && taskCounts[selectedDay] != null)
                taskCountText
            else subtitles[pagerState.targetPage]
        }
    }

    var mapRef by remember { mutableStateOf<MapView?>(null) }
    var mapLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var fabMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = colorScheme.surfaceContainer,
            topBar = {
                TopBar(
                    scrollBehavior = scrollBehavior,
                    title = currentTitle,
                    subtitle = currentSubtitle,
                    onStartClick = onTopBarClick,
                )
            },
            bottomBar = { },
            floatingActionButton = {
            }
        ) { innerPadding ->
            HorizontalPager(
                beyondViewportPageCount = 0,
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                userScrollEnabled = false
            ) { pageIndex ->
                when (pageIndex) {
                    0 -> CalendarView(navController, calendarVM)
                    1 -> TaskListView(
                        navController = navController,
                        tasks = taskListVM,
                        onAddClick = { fabMenuExpanded = true }
                    )
                    2 -> MapsView(
                        navController = navController,
                        mapsVM = mapsVM,
                        onMapReady = { map, overlay ->
                            mapRef = map
                            mapLocationOverlay = overlay
                        }
                    )
                }
            }
        }
        val recurringTaskName = stringResource(R.string.recurring_task_default)
        BottomBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            expanded = true,
            selectedIndex = pagerState.targetPage,
            fabMenuExpanded = fabMenuExpanded,
            onFabMenuToggle = { fabMenuExpanded = it },
            onAgendaClick = { scope.launch { pagerState.animateScrollToPage(0) } },
            onTaskListClick = { scope.launch { pagerState.animateScrollToPage(1) } },
            onMapsClick = { scope.launch { pagerState.animateScrollToPage(2) } },
            onAddNormalTask = {
                val task = Task()
                taskListVM.onEvent(TaskEvent.NewTask(task))
                val newday = LocalDateTime.of(selectedDay, LocalTime.NOON)
                if (pagerState.currentPage == 0) {
                    taskListVM.onEvent(TaskEvent.EditDate(task, newday))
                }
                navController.navigate("edit-task/${task.id}")
            },
            onAddRecurringTask = {
                val newTask = Task(
                    name = recurringTaskName,
                    recurrence = RecurrenceFrequency.DAILY,
                    expirationDate = LocalDateTime.now()
                )
                taskListVM.onEvent(TaskEvent.NewTask(newTask))
                navController.navigate("edit-task/${newTask.id}")
            },
            onAddBirthday = {
                val task = Task(isBirthday = true)
                taskListVM.onEvent(TaskEvent.NewTask(task))
                val newday = LocalDateTime.of(selectedDay, LocalTime.NOON)
                if (pagerState.currentPage == 0) {
                    taskListVM.onEvent(TaskEvent.EditDate(task, newday))
                }
                navController.navigate("edit-birthday/${task.id}")
            },
            onZoomClick = {
                mapLocationOverlay?.let { overlay ->
                    overlay.enableFollowLocation()

                    mapRef?.let { map ->
                        val location = overlay.myLocation
                        if (location != null) {
                            map.controller.zoomTo(18.0, 500L)
                            overlay.enableFollowLocation()
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Cercle pulsant en arrière-plan pour renforcer l'aspect expressif
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer {
                    scaleX = scale /2
                    scaleY = scale /2
                    alpha = 0.15f
                }
                .background(colorScheme.primaryContainer, CircleShape)
        )

        // Indicateur de chargement Material 3 avec formes morphing (Expressive)
        LoadingIndicator(
            modifier = Modifier.size(72.dp),
            color = colorScheme.primary
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview(showBackground = true)
fun AppScreen(initialPage : Int = 1){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = {3})
    val scope = rememberCoroutineScope()
    val taskListVM : TaskListVM = hiltViewModel()
    val calendarVM : CalendarVM = hiltViewModel()
    val mapsVM : MapsVM = hiltViewModel()

    val uiState by taskListVM.uiState.collectAsState()

    val drawerState = rememberWideNavigationRailState()
    val navController = rememberNavController()

    val isTest = try {
        Class.forName("androidx.test.platform.app.InstrumentationRegistry")
        true
    } catch (e: Exception) {
        false
    }

    var isContentVisible by remember { mutableStateOf(true) }

    if (!isTest) {
        NotificationPermissionRequest()
        isContentVisible = false
    }


    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            delay(100)
            isContentVisible = true
        }
    }

    if (isContentVisible) {
        AppNavigationDrawer(
            state = drawerState,
            selectedIndex = pagerState.currentPage,
            onPageSelected = { index ->
                scope.launch { pagerState.animateScrollToPage(index) }
            },
            onAboutClick = {
                navController.navigate("about")
            },
            onSettingsClick = {
                navController.navigate("settings")
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = "main",
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background),
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it })
                },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 4 }) },
                popEnterTransition = {
                    slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                },
                popExitTransition = {
                    slideOutHorizontally(targetOffsetX = { it })
                },

                ) {
                composable("main") {
                    MainView(
                        scrollBehavior,
                        pagerState,
                        scope,
                        navController,
                        taskListVM,
                        calendarVM,
                        mapsVM,
                        ({ scope.launch { drawerState.toggle() } })
                    )
                }
                composable("settings") {
                    SettingsView(navController = navController)
                }
                composable("settings/permissions") {
                    PermissionsView(navController = navController)
                }
                composable("about") {
                    AboutView(onBack = { navController.popBackStack() }, navController)
                }
                composable("about/secret"){
                    CalcView()
                }
                dialog(
                    route = "edit-task/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
                    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId")
                    EditView(
                        popBack = { navController.popBackStack() },
                        viewModel = taskListVM,
                        taskId = taskId
                    )
                }
                dialog(
                    route = "edit-birthday",
                    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    EditBirthdayView(
                        popBack = { navController.popBackStack() },
                        viewModel = taskListVM
                    )
                }
                dialog(
                    route = "edit-birthday/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType }),
                    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId")
                    EditBirthdayView(
                        popBack = { navController.popBackStack() },
                        viewModel = taskListVM,
                        taskId = taskId
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(colorScheme.surface))
    }
}
