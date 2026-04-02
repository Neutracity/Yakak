package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kayak.yakak.ui.TopBar
import java.time.Instant
import java.time.ZoneId


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditView(
    popBack: () -> Unit = {},
    viewModel: TaskListVM,
    taskId: Int? = 0,
){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val uiState by viewModel.uiState.collectAsState()
    val task = remember(uiState, taskId) {
        uiState.pendingTasks.find { it.id == taskId }
            ?: uiState.finishedTasks.find { it.id == taskId }
    }

    val isDialogOpen = remember { mutableStateOf(false) }


    if(task == null) return

    val nameText = remember(task.id) { mutableStateOf(task.name) }
    val descriptionText = remember(task.id) { mutableStateOf(task.description) }
    val isDatePickerOpen = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colorScheme.surfaceContainer,
        topBar = {TopBar(scrollBehavior = scrollBehavior,title = "Task Name",subtitle = "What do you want to change ?" , onStartClick = popBack)},
        bottomBar = {
            Box(Modifier.fillMaxWidth()){
                ExtendedFloatingActionButton(
                    onClick = popBack,
                    text = { Text("Finish") },
                    icon = {Icon(Icons.Outlined.Edit, contentDescription = "Finish")},
                    modifier = Modifier.padding(36.dp).align(Alignment.BottomEnd),
                )
                FloatingActionButton(
                    onClick = {popBack()
                        viewModel.onEvent(TaskEvent.Delete(task))},
                    content = {Icon(Icons.Outlined.Delete, contentDescription = "Delete")},
                    modifier = Modifier.padding(36.dp).align(Alignment.BottomStart),
                    containerColor = colorScheme.error
                )
            }
            
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center
        ){
            if(isDialogOpen.value){
                DatePickerModal({ dateMillis ->
                    if (dateMillis == null) {
                        isDialogOpen.value = false

                    }else{
                        val newDate =Instant.ofEpochMilli(dateMillis).atZone(ZoneId.systemDefault()).toLocalDate().plusDays(1)
                        viewModel.onEvent(TaskEvent.EditDate(task,newDate))
                    }
                },{
                    isDialogOpen.value = false
                })
            }


            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(26.dp,0.dp),
                        value = nameText.value,
                        onValueChange = {
                            nameText.value = it
                            viewModel.onEvent(TaskEvent.EditTitle(task,it)) },
                        label = { Text("Title") }
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().padding(26.dp,0.dp),
                        value = descriptionText.value,
                        onValueChange = {
                            descriptionText.value = it
                            viewModel.onEvent(TaskEvent.EditDescription(task,it)) },
                        label = { Text("Description") }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "End Date : ${task.expirationDate}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = colorScheme.onBackground,
                        modifier = Modifier.clickable(onClick = {isDialogOpen.value = true})
                    )
                    if(task.isCompleted){
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Finished Date : ${task.finishedDate}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = colorScheme.onBackground,
                            modifier = Modifier.clickable(onClick = {isDialogOpen.value = true})
                        )
                    }



                }
            }
        }

    }
}


@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun Preview2() {
    EditView({}, viewModel(),1)
}
