package com.kayak.yakak.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kayak.yakak.Task
import com.kayak.yakak.ui.calendar.CalendarVM
import com.kayak.yakak.ui.calendar.CalendarView
import com.kayak.yakak.ui.maps.MapsView
import com.kayak.yakak.ui.tasklist.EditView
import com.kayak.yakak.ui.tasklist.TaskEvent
import com.kayak.yakak.ui.tasklist.TaskListVM
import com.kayak.yakak.ui.tasklist.TaskListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(
    scrollBehavior: TopAppBarScrollBehavior,
    pagerState: PagerState,
    scope: CoroutineScope,
    navController: NavHostController,
    taskListVM: TaskListVM,
    calendarVM: CalendarVM
){

    val title = listOf("Agenda","To-Do List","Maps")
    val subtitle = listOf("","What are you going to do today ?","Where do you need to go ?")
    val selectedDay by calendarVM.selectedDay.collectAsState()
    val taskCounts by calendarVM.taskCounts.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.surfaceContainer,
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                title = if (pagerState.targetPage == 0) selectedDay.month.toString() else title[pagerState.targetPage],
                subtitle = if (pagerState.targetPage == 0 && taskCounts[selectedDay] != null) taskCounts[selectedDay].toString() + " tasks to do this month"  else subtitle[pagerState.targetPage]
            )},
        bottomBar = { BottomBar( expanded = true,
            selectedIndex = pagerState.targetPage,
            onAgendaClick = {scope.launch { pagerState.animateScrollToPage(0)}},
            onTaskListClick = {scope.launch { pagerState.animateScrollToPage(1)}},
            onMapsClick = {scope.launch { pagerState.animateScrollToPage(2)}},
            onAddClick = {
                val task = Task()
                taskListVM.onEvent(TaskEvent.NewTask(task))
                if(pagerState.currentPage == 0){
                    taskListVM.onEvent(TaskEvent.EditDate(task,selectedDay))
                }
                navController.navigate("edit-task/${task.id}")
            }

        )}
    ) { innerPadding ->
        HorizontalPager(
            beyondViewportPageCount = 2,
            state = pagerState,
            modifier = Modifier.padding(innerPadding),
            userScrollEnabled = false
        ) { pageIndex ->
            when (pageIndex){
                0-> CalendarView(navController, calendarVM)
                1-> TaskListView(navController,taskListVM)
                2-> MapsView()
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun AppScreen(initialPage : Int = 1){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = {3})
    val scope = rememberCoroutineScope()
    val taskListVM : TaskListVM = viewModel()
    val calendarVM : CalendarVM = viewModel()



    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it / 4 })},
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()},
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })
        },

        ){
        composable("main"){
            MainView(scrollBehavior,pagerState,scope,navController,taskListVM,calendarVM)
        }
        composable(
            route = "edit-task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            EditView(
                popBack = { navController.popBackStack() },
                viewModel = taskListVM,
                taskId = taskId
            )
        }


    }



}