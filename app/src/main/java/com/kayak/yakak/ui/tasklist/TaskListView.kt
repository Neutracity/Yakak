package com.kayak.yakak.ui.tasklist

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kayak.yakak.data.RecurrenceFrequency
import com.kayak.yakak.data.Task
import com.kayak.yakak.ui.theme.YKShapeDefaults.bottomListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.cardShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.middleListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.topListItemShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

enum class SwipeRevealValue { RevealEdit, Resting, RevealDelete }

@Composable
fun ScrollDownIndicator(progress: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounceY"
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp).alpha((0.6f + progress * 0.4f).coerceIn(0f, 1f)).scale((1f + progress * 0.25f).coerceIn(0f, 1.5f)).offset(y = (if (progress > 0) progress else bounceY).dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tâches terminées", style = MaterialTheme.typography.labelLarge, color = colorScheme.onSurfaceVariant)
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.rotate(progress * 180f), tint = colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BirthdayItem(task: Task, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).clickable { onClick() },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer.copy(alpha = 0.9f), contentColor = colorScheme.onPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.large).background(colorScheme.surface)) {
                if (!task.profileImageUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = task.profileImageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Cake, contentDescription = null, modifier = Modifier.align(Alignment.Center).size(36.dp), tint = colorScheme.primary)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Anniversaire de ${task.name}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                Text(text = "C'est sa journée spéciale ! \uD83C\uDF82✨", style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Outlined.Celebration, contentDescription = null, modifier = Modifier.size(40.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    items: Int = 0,
    index: Int = 0,
    showDate: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEditSwipe: () -> Unit = {}
) {
    val shape = if (items == 1) cardShape else if (index == items - 1) bottomListItemShape else if (index == 0) topListItemShape else middleListItemShape
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val menuWidthPx = with(density) { 80.dp.toPx() }

    var localIsCompleted by remember(task.isCompleted) { mutableStateOf(task.isCompleted) }
    var isWaiting by remember { mutableStateOf(false) }

    val spatialSpec = motionScheme.defaultSpatialSpec<Float>()
    val effectsSpec = motionScheme.defaultEffectsSpec<Float>()

    val isFirstLaunch = remember { mutableStateOf(true) }

    val dragState = remember(spatialSpec) {
        AnchoredDraggableState(
            initialValue = SwipeRevealValue.Resting,
            positionalThreshold = { it * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spatialSpec,
            decayAnimationSpec = exponentialDecay()
        ).apply {
            updateAnchors(DraggableAnchors {
                SwipeRevealValue.RevealEdit at menuWidthPx
                SwipeRevealValue.Resting at 0f
                SwipeRevealValue.RevealDelete at -menuWidthPx
            })
        }
    }

    val checkScale = remember { Animatable(1f) }
    LaunchedEffect(localIsCompleted) {
        if (isFirstLaunch.value) {
            isFirstLaunch.value = false
            return@LaunchedEffect
        }
        if (localIsCompleted) {
            checkScale.animateTo(2.6f, effectsSpec)
            checkScale.animateTo(1f, effectsSpec)
        }
    }

    val contentAlpha by animateFloatAsState(if (localIsCompleted) 0.35f else 1f, label = "alpha")

    Box(modifier = modifier.clip(shape)) {
        Box(modifier = Modifier.matchParentSize().background(colorScheme.surfaceVariant)) {
            IconButton(onClick = { coroutineScope.launch { dragState.animateTo(SwipeRevealValue.Resting) }; onEditSwipe() }, modifier = Modifier.align(Alignment.CenterStart).width(80.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colorScheme.primary)
            }
            IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.CenterEnd).width(80.dp)) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error)
            }
        }

        ListItem(
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(task.name, textDecoration = if (localIsCompleted) TextDecoration.LineThrough else null, modifier = Modifier.alpha(contentAlpha))
                    if (task.recurrence != RecurrenceFrequency.NONE && !localIsCompleted && task.streakCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                        Text("Streak ${task.streakCount}", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                    }
                }
            },
            supportingContent = { Text(task.description, textDecoration = if (localIsCompleted) TextDecoration.LineThrough else null, modifier = Modifier.alpha(contentAlpha)) },
            colors = ListItemDefaults.colors(containerColor = if (localIsCompleted) colorScheme.surfaceContainer else colorScheme.surface),
            modifier = Modifier.offset { IntOffset(if (dragState.offset.isNaN()) 0 else dragState.offset.roundToInt(), 0) }
                .anchoredDraggable(dragState, Orientation.Horizontal)
                .combinedClickable(
                    onClick = { if (!isWaiting) { isWaiting = true; localIsCompleted = !localIsCompleted; haptic.performHapticFeedback(HapticFeedbackType.ContextClick); coroutineScope.launch { delay(500); onClick(); isWaiting = false } } },
                    onLongClick = { onLongClick(); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                ),
            leadingContent = {
                Box(modifier = Modifier.size(46.dp, 68.dp), contentAlignment = Alignment.Center) {
                    Checkbox(checked = localIsCompleted, onCheckedChange = null, modifier = Modifier.scale(checkScale.value).rotate((checkScale.value - 1) * 30))
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    if (showDate) {
                        Text(text = "${task.expirationDate.dayOfMonth} ${task.expirationDate.month.toString().take(3)}", modifier = Modifier.alpha(contentAlpha))
                    }
                    if (!task.isAllDay) {
                        Text(text = task.expirationDate.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.labelSmall, modifier = Modifier.alpha(contentAlpha * 0.7f))
                    }
                }
            }
        )
    }
}

@Composable
fun TaskListView(
    navController: NavController,
    tasks: TaskListVM = viewModel(),
    onAddClick: () -> Unit = {}
) {
    val state by tasks.uiState.collectAsState()
    val birthdays = state.birthdaysToday
    val mixOfDay = state.mixOfTheDay
    val upcoming = state.upcomingTasks
    val finishedTasks = state.finishedTasks

    var showHiddenItem by remember { mutableStateOf(false) }
    var pullAmount by remember { mutableStateOf(0f) }
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (pullAmount > 0 && available.y > 0) {
                    val consumed = available.y.coerceAtMost(pullAmount)
                    pullAmount -= consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                // Only trigger with UserInput (drag) to prevent fast fling triggers
                if (!showHiddenItem && source == NestedScrollSource.UserInput && available.y < 0 && !listState.canScrollForward && finishedTasks.isNotEmpty()) {
                    val oldAmount = pullAmount
                    pullAmount = (pullAmount - available.y * 0.5f).coerceAtMost(240f)
                    if ((pullAmount / 35f).toInt() > (oldAmount / 35f).toInt()) haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    if (pullAmount >= 180f) { showHiddenItem = true; pullAmount = 0f; haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                    return Offset(0f, available.y)
                }
                if (showHiddenItem && available.y > 60f && !listState.canScrollBackward) { showHiddenItem = false; haptic.performHapticFeedback(HapticFeedbackType.ContextClick) }
                return Offset.Zero
            }
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (!showHiddenItem && pullAmount > 0) {
                    pullAmount = 0f
                }
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (birthdays.isEmpty() && mixOfDay.isEmpty() && upcoming.isEmpty()) {
                item {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !showHiddenItem,
                        enter = androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "☕",
                                style = MaterialTheme.typography.displayLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Rien à faire pour le moment !",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Profitez de votre temps libre ou créez une nouvelle tâche pour rester organisé.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                            )
                            androidx.compose.material3.Button(
                                onClick = onAddClick,
                                shape = MaterialTheme.shapes.extraLarge,
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Créer ma première tâche", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (birthdays.isNotEmpty()) {
                item { Text("Anniversaires", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                itemsIndexed(birthdays, key = { _, task -> "bday_${task.id}" }) { index, task ->
                    BirthdayItem(
                        task = task,
                        onClick = { navController.navigate("edit-birthday/${task.id}") }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (mixOfDay.isNotEmpty()) {
                item { Text("Mix du jour", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                itemsIndexed(mixOfDay, key = { _, task -> "mix_${task.id}" }) { index, task ->
                    TaskItem(
                        task = task,
                        items = mixOfDay.size,
                        index = index,
                        showDate = false,
                        onClick = { tasks.onEvent(TaskEvent.EditState(task, true)) },
                        onLongClick = { navController.navigate("edit-task/${task.id}") },
                        onDelete = { tasks.onEvent(TaskEvent.Delete(task)) },
                        modifier = Modifier.animateItem(),
                        onEditSwipe = { navController.navigate("edit-task/${task.id}") }
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }

            if (upcoming.isNotEmpty()) {
                item { Text("À venir", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
                itemsIndexed(upcoming, key = { _, task -> "up_${task.id}" }) { index, task ->
                    TaskItem(
                        task = task,
                        items = upcoming.size,
                        index = index,
                        onClick = { tasks.onEvent(TaskEvent.EditState(task, true)) },
                        onLongClick = { navController.navigate("edit-task/${task.id}") },
                        onDelete = { tasks.onEvent(TaskEvent.Delete(task)) },
                        modifier = Modifier.animateItem(),
                        onEditSwipe = { navController.navigate("edit-task/${task.id}") }
                    )
                }
            }

            if (finishedTasks.isNotEmpty() && showHiddenItem) {
                // Add space before the "Tâches terminées" header
                item { Spacer(Modifier.height(32.dp)) }
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                        Text("Tâches terminées", style = MaterialTheme.typography.titleMedium, color = colorScheme.onSurfaceVariant)
                        IconButton(onClick = { showHiddenItem = false }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
                    }
                }
                itemsIndexed(finishedTasks, key = { _, task -> "fin_${task.id}" }) { index, task ->
                    TaskItem(
                        task = task,
                        items = finishedTasks.size,
                        index = index,
                        onClick = { tasks.onEvent(TaskEvent.EditState(task, false)) },
                        onLongClick = { navController.navigate("edit-task/${task.id}") },
                        onDelete = { tasks.onEvent(TaskEvent.Delete(task)) },
                        onEditSwipe = { navController.navigate("edit-task/${task.id}") },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
            // Increased spacer height and made it an item that is always present to ensure scrollability
            item { Spacer(Modifier.height(160.dp)) }
        }


        if (!showHiddenItem && finishedTasks.isNotEmpty()) {
            ScrollDownIndicator(
                progress = (pullAmount / 180f).coerceIn(0f, 1f),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 106.dp)
            )
        }
    }
}
