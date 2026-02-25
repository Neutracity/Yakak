package com.kayak.yakak.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.kayak.yakak.ui.tasklist.TaskListVM
import com.kayak.yakak.ui.tasklist.TaskListView
import com.kayak.yakak.ui.TopBar
import com.kayak.yakak.ui.calendar.CalendarView
import com.kayak.yakak.ui.maps.MapsView
import com.kayak.yakak.ui.theme.YakakTheme
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun AppScreen(initialPage : Int = 1){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = {3})
    val scope = rememberCoroutineScope()

    val title = listOf("Agenda","To-Do List","Maps")
    val subtitle = listOf("","What are you going to do today ?","Where do you need to go ?")


    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.surfaceContainer,
        topBar = {TopBar(scrollBehavior = scrollBehavior,title = title[pagerState.currentPage],subtitle = subtitle[pagerState.currentPage] )},
        bottomBar = { BottomBar( expanded = true,
            onAgendaClick = {scope.launch { pagerState.animateScrollToPage(0)}},
            onTaskListClick = {scope.launch { pagerState.animateScrollToPage(1)}},
            onMapsClick = {scope.launch { pagerState.animateScrollToPage(2)}}
        )}
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding)
        ) { pageIndex ->
            when (pageIndex){
                0-> CalendarView()
                1-> TaskListView(innerPadding)
                2-> MapsView()
            }
        }
    }
}