package com.kayak.yakak.ui.tasklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kayak.yakak.Task
import com.kayak.yakak.ui.AppScreen
import com.kayak.yakak.ui.theme.YKShapeDefaults.bottomListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.cardShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.middleListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.topListItemShape


@Composable
fun TaskItem(
    task: Task,
    modifier: Modifier = Modifier,
    items: Int = 0,
    index: Int = 0,
    onCheck: () -> Unit = {},
    onClick: () -> Unit = {},
    //onLongClick: () -> Unit = {},
    //onDoubleClick: () -> Unit = {},
){
    val shape = if (items == 1) cardShape else if(index == items-1 ) bottomListItemShape  else if (index == 0) topListItemShape else middleListItemShape
    val haptic = LocalHapticFeedback.current

    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true 
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = expandVertically(animationSpec = tween(400)) + fadeIn(),
        exit = shrinkVertically() + fadeOut()

    ) {
    ListItem(
        headlineContent = {
            Text(task.name,
                textDecoration = if(task.isCompleted)  TextDecoration.LineThrough else null
        )},
        supportingContent = { Text(task.description,
            textDecoration = if(task.isCompleted)  TextDecoration.LineThrough else null
        ) },
        modifier = modifier.clip(shape)
            .padding(1.dp).combinedClickable(
                onClick = {
                    onClick()
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }
            ),
        leadingContent = {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange =  {
                    onCheck()
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm) },
                colors = CheckboxDefaults.colors(),
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

    LazyColumn(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ){
        item{
            Text(
                text = "Pending Tasks",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(10.dp,16.dp,10.dp,6.dp)
            )
        }
        itemsIndexed(items = pendingTasks, key = null){ index, task ->
            Box(
                modifier = Modifier.animateItem()
            ){
                TaskItem(
                    task = task,
                    index = index,
                    items = pendingTasks.size,
                    onCheck = {tasks.onEvent(TaskEvent.EditState(task,true))},
                    onClick = {navController.navigate("edit-task/${task.id}")}

                )
            }

        }
        item{ Spacer(Modifier.height(10.dp)) }
        item{
            Text(
                text = "Finished Tasks",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.secondary,
                modifier = Modifier.padding(10.dp,23.dp,10.dp,6.dp)
            )
        }
        itemsIndexed(items = finishedTasks, key = null){ index, task ->
            Box(
                modifier = Modifier.animateItem()
            ){
            TaskItem(
                task = task,
                index = index,
                items = pendingTasks.size,
                onCheck = {tasks.onEvent(TaskEvent.EditState(task,false))},
                onClick = {navController.navigate("edit-task/${task.id}")}

            )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview() {
    AppScreen(1)
}