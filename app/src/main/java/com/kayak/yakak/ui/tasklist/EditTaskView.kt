package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.kayak.yakak.ui.TopBar
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

enum class PickerContext { NONE, EXPIRATION_DATE, EXPIRATION_TIME, REMINDER_DATE, REMINDER_TIME }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun EditView(
    popBack: () -> Unit = {},
    viewModel: TaskListVM,
    taskId: Int? = 0,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val task = remember(uiState, taskId) {
        uiState.pendingTasks.find { it.id == taskId }
            ?: uiState.finishedTasks.find { it.id == taskId }
    }

    if (task == null) return

    var nameText by remember(task.id) { mutableStateOf(task.name) }
    var descriptionText by remember(task.id) { mutableStateOf(task.description) }
    var pickerContext by remember { mutableStateOf(PickerContext.NONE) }
    var tempReminderDate by remember { mutableStateOf<LocalDate?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                title = "Edit Task",
                subtitle = "Manage your task details",
                onStartClick = popBack
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        popBack()
                        viewModel.onEvent(TaskEvent.Delete(task))
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                }
                ExtendedFloatingActionButton(
                    onClick = {
                        if (nameText.isBlank()) {
                            val randomTask = com.kayak.yakak.utils.getTaskList().random()

                            viewModel.onEvent(
                                TaskEvent.EditTask(
                                    task.copy(
                                        name = randomTask.name,
                                        description = randomTask.description
                                    )
                                )
                            )
                        }

                        popBack()
                    },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                    text = { Text("Save") }
                )
            }
        }
    ) { innerPadding ->

        if (pickerContext == PickerContext.EXPIRATION_DATE || pickerContext == PickerContext.REMINDER_DATE) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    if (dateMillis != null) {
                        val selectedDate = Instant.ofEpochMilli(dateMillis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (pickerContext == PickerContext.EXPIRATION_DATE) {
                            val newDateTime = LocalDateTime.of(selectedDate, task.expirationDate.toLocalTime())
                            viewModel.onEvent(TaskEvent.EditTask(task.copy(expirationDate = newDateTime)))
                            pickerContext = PickerContext.NONE
                        } else {
                            tempReminderDate = selectedDate
                            pickerContext = PickerContext.REMINDER_TIME
                        }
                    } else {
                        pickerContext = PickerContext.NONE
                    }
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        if (pickerContext == PickerContext.EXPIRATION_TIME || pickerContext == PickerContext.REMINDER_TIME) {
            TimePickerModal(
                onTimeSelected = { time ->
                    if (pickerContext == PickerContext.EXPIRATION_TIME) {
                        val newDateTime = LocalDateTime.of(task.expirationDate.toLocalDate(), time)
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(expirationDate = newDateTime)))
                    } else if (pickerContext == PickerContext.REMINDER_TIME && tempReminderDate != null) {
                        val newReminder = LocalDateTime.of(tempReminderDate, time)
                        val updatedReminders = task.reminderList + newReminder
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(reminderList = updatedReminders)))
                    }
                    pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(name = it)))
                    },
                    label = { Text("Task Name") },
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    value = descriptionText,
                    onValueChange = {
                        descriptionText = it
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(description = it)))
                    },
                    label = { Text("Description") },
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Timing", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.EXPIRATION_DATE }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("End Date", style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(task.expirationDate.format(dateFormatter), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.EXPIRATION_TIME }.padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("End Time", style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(task.expirationDate.format(timeFormatter), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Reminders", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { pickerContext = PickerContext.REMINDER_DATE }) {
                                Icon(Icons.Outlined.AddAlert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (task.reminderList.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                task.reminderList.forEach { reminder ->
                                    InputChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text(reminder.format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Outlined.Close,
                                                contentDescription = null,
                                                modifier = Modifier.clickable {
                                                    val updatedReminders = task.reminderList - reminder
                                                    viewModel.onEvent(TaskEvent.EditTask(task.copy(reminderList = updatedReminders)))
                                                }
                                            )
                                        }
                                    )
                                }
                            }
                        } else {
                            Text("No reminders set", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (task.isCompleted) {
                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Column {
                                Text("Completed", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                task.finishedDate?.let {
                                    Text("Finished on: ${it.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) {
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
@Composable
fun TimePickerModal(
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}