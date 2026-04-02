package com.kayak.yakak.ui.tasklist



import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kayak.yakak.data.Task
import com.kayak.yakak.ui.theme.YKShapeDefaults.bottomListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.cardShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.middleListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.topListItemShape
import com.kayak.yakak.utils.getTaskList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


enum class SwipeRevealValue {
    RevealEdit,
    Resting,
    RevealDelete
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    items: Int = 0,
    index: Int = 0,
    onCheck: () -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDoubleClick: () -> Unit = {},
    onDelete: () -> Unit = {},
    onEditSwipe: () -> Unit = {}
) {
    val shape = if (items == 1) cardShape else if (index == items - 1) bottomListItemShape else if (index == 0) topListItemShape else middleListItemShape
    val haptic = LocalHapticFeedback.current
    val motionScheme = motionScheme
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val menuWidth = 80.dp
    val menuWidthPx = with(density) { menuWidth.toPx() }

    var localIsCompleted by remember(task.isCompleted) { mutableStateOf(task.isCompleted) }
    var isWaiting by remember { mutableStateOf(false) }

    val dragState = remember {
        AnchoredDraggableState(
            initialValue = SwipeRevealValue.Resting,
            positionalThreshold = { distance: Float -> distance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = tween(),
            decayAnimationSpec = exponentialDecay()
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    SwipeRevealValue.RevealEdit at menuWidthPx
                    SwipeRevealValue.Resting at 0f
                    SwipeRevealValue.RevealDelete at -menuWidthPx
                }
            )
        }
    }

    val checkScale = remember { Animatable(1f) }

    LaunchedEffect(localIsCompleted) {
        if (localIsCompleted) {
            checkScale.animateTo(1.3f, tween(100))
            checkScale.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium))
        } else {
            checkScale.animateTo(1f, tween(150))
        }
    }

    val containerColor by animateColorAsState(
        targetValue = if (localIsCompleted) colorScheme.surface.copy(0.5f) else colorScheme.surface,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "color"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (localIsCompleted) 0.35f else 1f,
        animationSpec = motionScheme.defaultEffectsSpec(),
        label = "alpha"
    )

    Box(modifier = modifier.clip(shape)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(colorScheme.surfaceVariant)
        ) {
            IconButton(
                onClick = {
                    coroutineScope.launch { dragState.animateTo(SwipeRevealValue.Resting) }
                    onEditSwipe()
                },
                modifier = Modifier.align(Alignment.CenterStart).width(menuWidth)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = colorScheme.primary)
            }

            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.align(Alignment.CenterEnd).width(menuWidth)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = colorScheme.error)
            }
        }

        ListItem(
            headlineContent = {
                Text(
                    task.name,
                    textDecoration = if (localIsCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.alpha(contentAlpha)
                )
            },
            supportingContent = {
                Text(
                    task.description,
                    textDecoration = if (localIsCompleted) TextDecoration.LineThrough else null,
                    modifier = Modifier.alpha(contentAlpha)
                )
            },
            colors = ListItemDefaults.colors(containerColor = containerColor),
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = if (dragState.offset.isNaN()) 0 else dragState.offset.roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(state = dragState, orientation = Orientation.Horizontal)
                .combinedClickable(
                    onClick = {
                        if (!isWaiting) {
                            isWaiting = true
                            localIsCompleted = !localIsCompleted
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)

                            coroutineScope.launch {
                                delay(500)
                                onClick()
                                isWaiting = false
                            }
                        }
                    },
                    onLongClick = {
                        onLongClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDoubleClick = { onDoubleClick() }
                ),
            leadingContent = {
                Box(
                    modifier = Modifier.size(46.dp, 68.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Checkbox(
                        checked = localIsCompleted,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(),
                        modifier = Modifier.scale(checkScale.value)
                    )
                }
            },
            trailingContent = {
                Text(
                    text = task.expirationDate.dayOfMonth.toString() + " " + task.expirationDate.month.toString(),
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        )
    }
}



@Composable
fun TaskListView(navController: NavController,tasks : TaskListVM = viewModel()){
    val state by tasks.uiState.collectAsState()
    val pendingTasks = state.pendingTasks
    val finishedTasks = state.finishedTasks

    var showHiddenItem by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y < -60f) {
                    showHiddenItem = true
                }
                if (available.y > 60f) showHiddenItem = false
                return Offset.Zero
            }
        }
    }

    LazyColumn(
        modifier = Modifier.padding(12.dp)
            .nestedScroll(nestedScrollConnection)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),

    ){
        if(pendingTasks.count() > 0){
            item(-1){
                Text(
                    text = "Pending Tasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp,16.dp,10.dp,6.dp).animateItem()
                )
            }
            itemsIndexed(items = pendingTasks,key = { _, task -> task.id } ){ index, task ->
                TaskItem(
                    task = task,
                    index = index,
                    items = pendingTasks.size,
                    onClick = {tasks.onEvent(TaskEvent.EditState(task,true))},
                    onLongClick = {navController.navigate("edit-task/${task.id}")},
                    onEditSwipe = {navController.navigate("edit-task/${task.id}")},
                    onDelete = {tasks.onEvent(TaskEvent.Delete(task))},
                    modifier = Modifier.animateItem()

                )

            }
        }else{
            item {
                Spacer(Modifier.height(200.dp))
                Box(Modifier.fillMaxWidth().animateItem()){
                        Text(
                            text = "No pending tasks",
                            style = MaterialTheme.typography.displayLargeEmphasized,
                            textAlign = TextAlign.Center
                        )

                    }


            }
        }




        if(finishedTasks.count() > 0 && showHiddenItem){
            item{ Spacer(Modifier.height(10.dp).animateItem()) }
            item(-2){
                Text(
                    text = "Finished Tasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.secondary,
                    modifier = Modifier.padding(10.dp,23.dp,10.dp,6.dp).animateItem()
                )
            }
            itemsIndexed(items = finishedTasks, key = { _, task -> task.id }){ index, task ->
                TaskItem(
                    task = task,
                    index = index,
                    items = finishedTasks.size,
                    onClick = {tasks.onEvent(TaskEvent.EditState(task,false))},
                    onLongClick = {navController.navigate("edit-task/${task.id}")} ,
                    modifier = Modifier.animateItem()
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }

    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview(){
    TaskItem(getTaskList()[0])

}