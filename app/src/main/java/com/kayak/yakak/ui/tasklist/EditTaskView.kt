package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kayak.yakak.Task
import com.kayak.yakak.ui.TopBar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditView(
    popBack: () -> Unit = {},
    viewModel: TaskListVM? = null,
    task: Task? = null,
){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    if(task == null) return
    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.surfaceContainer,
        topBar = {TopBar(scrollBehavior = scrollBehavior,title = "Task Name",subtitle = "What do you want to change ?" , onStartClick = popBack)},
        bottomBar = {
            Box(Modifier.fillMaxWidth()){
                Surface(
                    color = colorScheme.primaryContainer,
                    onClick = {},
                    modifier = Modifier.padding(20.dp).size(58.dp).align(Alignment.BottomEnd),
                    shape = RoundedCornerShape(26.dp),
                    shadowElevation = 2.dp,
                    tonalElevation = 1.dp,

                    ) {
                    Box(contentAlignment = Alignment.Center){
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(32.dp)

                        )
                    }

                }
            }
            
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(26.dp,0.dp),
                    value = task.name,
                    onValueChange = {viewModel?.onEvent(TaskEvent.EditTitle(task,it))},
                    label = { Text("Title") }
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().padding(26.dp,0.dp),
                    value = task.description,
                    onValueChange = {viewModel?.onEvent(TaskEvent.EditDescription(task,it)) },
                    label = { Text("Description") }
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview2() {
    EditView()
}
