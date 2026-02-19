package com.kayak.yakak

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kayak.yakak.ui.theme.YakakTheme


enum class TaskPos {
    Top,
    Mid,
    Bottom
}
@Composable
fun TaskItem( task: Task,modifier : Modifier  = Modifier, pos : TaskPos = TaskPos.Mid){
        val topTaskCorner = if (pos == TaskPos.Top) 16.dp else 2.dp
        val bottomTaskCorner = if (pos == TaskPos.Bottom) 16.dp else 2.dp
        Surface(
            modifier = Modifier.fillMaxSize().padding(top =1.dp, bottom=1.dp),
            shape = RoundedCornerShape(topStart = topTaskCorner, topEnd = topTaskCorner, bottomEnd = bottomTaskCorner,bottomStart = bottomTaskCorner),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(8.dp)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(),
                    modifier = Modifier.align(Alignment.CenterVertically).padding(10.dp)
                )

                Column(
                    modifier = Modifier.padding(3.dp)
                ) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall
                    )

                }

            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(modifier: Modifier = Modifier){
    TopAppBar(
        title = {
            Text(
                text = "To-do List"
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {},
                colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )

                ) { Icon(Icons.Outlined.Menu,contentDescription = "Menu") }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )

    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    YakakTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            topBar = {TopBar()}
        ) { innerPadding ->
            val tasks = mutableListOf<Task>()
            for (i in 1..4 ){
                tasks.add(Task("Task Number $i", "This is the description of the task", false))
            }
            LazyColumn(
                modifier = Modifier.padding(12.dp),
                contentPadding = innerPadding ,

                ){
                items(tasks.size) { taskIndex ->
                    TaskItem(
                        tasks[taskIndex],
                        pos = if (taskIndex == 0) TaskPos.Top else if (taskIndex == tasks.size-1) TaskPos.Bottom else TaskPos.Mid
                        )
                }
            }
        }
    }
}