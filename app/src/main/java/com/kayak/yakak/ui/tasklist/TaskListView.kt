package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kayak.yakak.Task
import com.kayak.yakak.ui.AppScreen
import com.kayak.yakak.ui.BottomBar
import com.kayak.yakak.ui.TopBar
import com.kayak.yakak.ui.theme.YKShapeDefaults.bottomListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.middleListItemShape
import com.kayak.yakak.ui.theme.YKShapeDefaults.topListItemShape
import com.kayak.yakak.ui.theme.YakakTheme


@Composable
fun TaskItem(task: Task, modifier : Modifier  = Modifier, items : Int = 0, index : Int = 0, onClick : () -> Unit = {}){
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
            .padding(1.dp),
        leadingContent = {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange =  {onClick()} ,
                    colors = CheckboxDefaults.colors(),
                )
            }

    )
}


@Composable
fun TaskListView(innerPadding : PaddingValues){
    val tasks : TaskListVM = viewModel()
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
                        onClick = {tasks.onEvent(TaskEvent.EditState(task,true))}
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
                        onClick = {tasks.onEvent(TaskEvent.EditState(task,false))}
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