package com.kayak.yakak.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kayak.yakak.ui.AppScreen
import com.kayak.yakak.ui.tasklist.TaskItem
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarView(navController: NavController, calendarVM: CalendarVM = viewModel()){
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) }
    val endMonth = remember { currentMonth.plusMonths(120) }

    val firstDayOfWeek = remember { DayOfWeek.MONDAY }

    val selectedDayTasks by calendarVM.selectedTasks.collectAsState()
    val selectedDay by calendarVM.selectedDay.collectAsState()

    val taskCounts by calendarVM.taskCounts.collectAsState()

    val coroutineScope = rememberCoroutineScope()



    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ){
        item{
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(day,selectedDay,taskCounts[day.date]?:0) {
                        calendarVM.onEvent(CalendarEvent.SelectDay(day.date))
                        coroutineScope.launch {
                            state.animateScrollToMonth(day.date.yearMonth)
                        }
                    }
                },
                monthHeader = { month ->
                    DaysOfWeek(month)
                },
                modifier = Modifier.padding(12.dp)
            )
        }
        selectedDayTasks.forEachIndexed { index, task->
            item {
                TaskItem(
                    task = task,
                    index = index,
                    items = selectedDayTasks.size,
                    onCheck = {calendarVM.onEvent(CalendarEvent.EditState(task,true))},
                    onClick = {calendarVM.onEvent(CalendarEvent.EditState(task,true))},
                    onLongClick = {navController.navigate("edit-task/${task.id}")},
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                )
            }
        }
    }

}

@Composable
fun DaysOfWeek(month: CalendarMonth){
    Column {
        Text(
            text = month.yearMonth.month.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = colorScheme.onBackground,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            val dayList = listOf("Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi","Dimanche")
            for (day in dayList){
                Text(
                    text = day,
                    color = colorScheme.onBackground.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

}


@Composable
fun Day(day : CalendarDay, selectedDay : LocalDate? = null,taskCount : Int = 0 , onClick : () -> Unit = {}){
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = {
                onClick()
                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
            }),
        contentAlignment = Alignment.Center,

    ){
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
            shape = RoundedCornerShape(10.dp),
            color = if(day.date == selectedDay) colorScheme.onBackground.copy(alpha = 0.1f) else if (day.date.month != selectedDay?.month) colorScheme.background.copy(0.3f) else colorScheme.background
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = if (day.position == DayPosition.MonthDate) colorScheme.onBackground else colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(4.dp)
            )
        }
        if(taskCount > 0){
            Badge(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp),
            ){
                Text(
                    text = taskCount.toString(),
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview() {
    AppScreen(0)
}
