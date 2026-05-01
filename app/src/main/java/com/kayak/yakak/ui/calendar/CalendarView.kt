package com.kayak.yakak.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kayak.yakak.data.Task
import com.kayak.yakak.ui.tasklist.BirthdayItem
import com.kayak.yakak.ui.tasklist.TaskItem
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarView(navController: NavController, calendarVM: CalendarVM = viewModel()) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) }
    val endMonth = remember { currentMonth.plusMonths(120) }
    val firstDayOfWeek = remember { DayOfWeek.MONDAY }

    val selectedDayTasks by calendarVM.selectedTasks.collectAsState()
    val selectedDay by calendarVM.selectedDay.collectAsState()
    val selectedDayBirthdays by calendarVM.selectedDayBirthdays.collectAsState()
    val taskCounts by calendarVM.taskCounts.collectAsState()
    val birthdays by calendarVM.birthdayDays.collectAsState()
    val showLowFreq by calendarVM.showLowFrequency.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val state = rememberCalendarState(startMonth, endMonth, currentMonth, firstDayOfWeek)

    Column(modifier = Modifier.fillMaxSize()) {
        // FIXED HEADER SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.minusMonths(1))
                    }
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimatedVisibility(visible = selectedDay != LocalDate.now()) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val today = LocalDate.now()
                                calendarVM.onEvent(CalendarEvent.SelectDay(today))
                                state.animateScrollToMonth(today.yearMonth)
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        modifier = Modifier.height(40.dp)
                    ) { Icon(Icons.Default.Today, null, Modifier.size(18.dp)) }
                }
                IconButton(
                    onClick = { calendarVM.onEvent(CalendarEvent.ToggleShowLowFrequency(!showLowFreq)) },
                    colors = if (showLowFreq) IconButtonDefaults.filledIconButtonColors() else IconButtonDefaults.filledTonalIconButtonColors()
                ) { Icon(Icons.Default.FilterList, null) }
            }

            IconButton(
                onClick = {
                    coroutineScope.launch {
                        state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.plusMonths(1))
                    }
                },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
        }

        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = colorScheme.surfaceContainerLow
        ) {
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = day.date == selectedDay,
                        isToday = day.date == LocalDate.now(),
                        taskCount = taskCounts[day.date] ?: 0,
                        hasBirthday = birthdays.any {
                            it.dayOfMonth == day.date.dayOfMonth && it.month == day.date.month
                        },
                        selectedDayBirthdays = selectedDayBirthdays,
                        onBirthdayHold = { calendarVM.onEvent(CalendarEvent.SelectDay(day.date)) },
                        onClick = {
                            calendarVM.onEvent(CalendarEvent.SelectDay(day.date))
                            if (day.position != DayPosition.MonthDate) {
                                coroutineScope.launch { state.animateScrollToMonth(day.date.yearMonth) }
                            }
                        }
                    )
                },
                monthHeader = { DaysOfWeek() },
                modifier = Modifier.padding(12.dp)
            )
        }

        // SCROLLABLE TASKS SECTION
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                val dateStr = if (selectedDay == LocalDate.now()) "Aujourd'hui"
                else selectedDay.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
                    .replaceFirstChar { it.uppercase() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = dateStr, style = typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    val totalItems = selectedDayTasks.size + selectedDayBirthdays.size
                    if (totalItems > 0) {
                        Badge(containerColor = colorScheme.primaryContainer, modifier = Modifier.scale(1.2f)) {
                            Text("$totalItems", modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            if (selectedDayBirthdays.isNotEmpty()) {
                items(selectedDayBirthdays, key = { it.id }) { birthday ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BirthdayItem(birthday, onClick = { navController.navigate("edit-birthday/${birthday.id}") })
                    }
                }
            }

            if (selectedDayTasks.isEmpty() && selectedDayBirthdays.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("✨", style = typography.displayMedium)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Rien de prévu pour ce jour.",
                            color = colorScheme.onSurfaceVariant.copy(0.6f)
                        )
                    }
                }
            } else {
                itemsIndexed(items = selectedDayTasks, key = { _, task -> task.id }) { index, task ->
                    TaskItem(
                        task = task,
                        index = index,
                        items = selectedDayTasks.size,
                        onClick = { calendarVM.onEvent(CalendarEvent.EditState(task, true)) },
                        onLongClick = { navController.navigate("edit-task/${task.id}") },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
fun BirthdayPopupContent(
    birthdays: List<Task>,
    isVisible: Boolean,
) {
    // Un seul Float qui gère l'état de l'animation de 0f (fermé) à 1f (ouvert)
    val animationProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "popupAnimation"
    )

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        // La Bulle (Card)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier
                .padding(bottom = 28.dp)
                .widthIn(min = 140.dp, max = 220.dp)
                .graphicsLayer {
                    // La bulle grandit et apparait en fonction du progress
                    scaleX = animationProgress
                    scaleY = animationProgress
                    alpha = animationProgress.coerceIn(0f, 1f)
                    // Origine de la transformation : en bas au centre (donc ça grandit depuis le doigt)
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                birthdays.forEach { bday ->
                    Text(
                        text = bday.name,
                        style = typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorScheme.onSecondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "Joyeux anniversaire !",
                    style = typography.labelSmall,
                    color = colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                )
            }
        }

        // Le Gâteau
        Text(
            text = "🎂",
            fontSize = 32.sp,
            modifier = Modifier
                .graphicsLayer {
                    // Le gâteau monte jusqu'à -85dp proportionnellement au progress
                    translationY = (-102.dp.toPx()) * animationProgress
                    // Il grossit légèrement aussi
                    scaleX = 1f + (0.3f * animationProgress)
                    scaleY = 1f + (0.3f * animationProgress)
                    // S'assure que le gâteau du popup n'apparaît que quand la bulle s'ouvre
                    alpha = (animationProgress * 2f).coerceIn(0f, 1f)
                }
                .zIndex(10f)
        )
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    taskCount: Int,
    hasBirthday: Boolean,
    selectedDayBirthdays: List<Task>,
    onBirthdayHold: () -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val viewConfig = LocalViewConfiguration.current
    val scope = rememberCoroutineScope()

    var showPopup by remember { mutableStateOf(false) }
    var animateIn by remember { mutableStateOf(false) }

    val containerColor by animateColorAsState(if (isSelected) colorScheme.primary else Color.Transparent, spring(stiffness = 500f))
    val contentColor by animateColorAsState(if (isSelected) colorScheme.onPrimary else if (day.position != DayPosition.MonthDate) colorScheme.onSurfaceVariant.copy(0.3f) else if (isToday) colorScheme.primary else colorScheme.onSurface)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    val job = scope.launch {
                        delay(viewConfig.longPressTimeoutMillis)
                        if (hasBirthday) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onBirthdayHold()
                            // 1. On attache la popup à la composition
                            showPopup = true
                        }
                    }
                    try {
                        awaitRelease()
                    } finally {
                        job.cancel()
                        if (showPopup) {
                            // 3. Dès qu'on lâche, on déclenche l'animation inverse (vers 0f)
                            animateIn = false
                            scope.launch {
                                // 4. On attend que l'effet élastique se termine avant de détruire la popup
                                delay(300)
                                showPopup = false
                            }
                        }
                    }
                },
                onTap = { onClick() }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .padding(4.dp)
                .clip(CircleShape)
                .then(if (isToday && !isSelected) Modifier.background(colorScheme.primaryContainer.copy(0.4f)) else Modifier)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    style = typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Medium),
                    color = contentColor
                )
                if (taskCount > 0 && !hasBirthday) {
                    Row(modifier = Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(taskCount.coerceAtMost(3)) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) colorScheme.onPrimary else colorScheme.primary))
                        }
                    }
                }
            }
        }

        if (hasBirthday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .zIndex(5f)
            ) {
                // Le gâteau "immobile" sur le calendrier
                Text(
                    text = "🎂",
                    fontSize = 26.sp,
                    modifier = Modifier
                        //.alpha(if (showPopup) 0f else 1f) // Se cache pendant que l'autre saute
                        .scale(if (isSelected) 1.1f else 1f)
                        .wrapContentSize(unbounded = true)
                )

                if (showPopup) {
                    // 2. Dès que la popup est attachée, on passe animateIn à true pour démarrer l'animation (de 0f à 1f)
                    LaunchedEffect(Unit) {
                        animateIn = true
                    }

                    Popup(
                        alignment = Alignment.BottomCenter,
                    ) {
                        BirthdayPopupContent(
                            birthdays = selectedDayBirthdays,
                            isVisible = animateIn,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaysOfWeek() {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
            Text(text = day, style = typography.labelMedium, fontWeight = FontWeight.Bold, color = colorScheme.primary.copy(0.5f), textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
        }
    }
}