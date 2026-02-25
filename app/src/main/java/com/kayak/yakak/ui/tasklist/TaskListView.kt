package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kayak.yakak.Task
import com.kayak.yakak.ui.AppScreen
import com.kayak.yakak.ui.theme.YKShapeDefaults.bottomListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.middleListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.topListItemShape


@Composable
fun TaskItem(task: Task, modifier : Modifier  = Modifier, items : Int = 0, index : Int = 0, onCheck : () -> Unit = {},onClick : () -> Unit = {}){
    val shape = if(index == items-1 ) bottomListItemShape  else if (index == 0) topListItemShape else middleListItemShape
    ListItem(
        headlineContent = {
            Text(task.name,
                textDecoration = if(task.isCompleted)  TextDecoration.LineThrough else null
        )},
        supportingContent = { Text(task.description,
            textDecoration = if(task.isCompleted)  TextDecoration.LineThrough else null
        ) },
        modifier = modifier.clip(shape)
            .padding(1.dp).clickable(
                onClick = onClick
            ),
        leadingContent = {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange =  {onCheck()} ,
                    colors = CheckboxDefaults.colors(),
                )
            }

    )
}


@Composable
fun TaskListView(innerPadding : PaddingValues,navController: NavController,tasks : TaskListVM = viewModel()){
    val pendingTasks = tasks.tasks.value.filter { !it.isCompleted }
    val finishedTasks = tasks.tasks.value.filter { it.isCompleted }
    LazyColumn(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ){
        item{
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Pending Tasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp,16.dp,10.dp,6.dp)
                )

                pendingTasks.forEachIndexed { index, task ->
                    TaskItem(
                        task = task,
                        index = index,
                        items = tasks.tasks.value.size,
                        onCheck = {tasks.onEvent(TaskEvent.EditState(task,true))},
                        onClick = {navController.navigate("edit-task/${task.id}")}

                    )
                }

            }
        }
        item{ Spacer(Modifier.height(10.dp)) }
        item{
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Finished Tasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.secondary,
                    modifier = Modifier.padding(10.dp,23.dp,10.dp,6.dp)
                )
                finishedTasks.forEachIndexed { index, task ->
                    TaskItem(
                        task = task,
                        index = index,
                        items = tasks.tasks.value.size,
                        onCheck = {tasks.onEvent(TaskEvent.EditState(task,false))},
                        onClick = {navController.navigate("edit-task/${task.id}")}
                    )
                }
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