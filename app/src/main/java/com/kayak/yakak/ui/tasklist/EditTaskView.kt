package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kayak.yakak.ui.TopBar
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditView(
    popBack: () -> Unit = {},
    viewModel: TaskListVM,
    taskId: Int? = 0,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    val task = remember(uiState, taskId) {
        uiState.pendingTasks.find { it.id == taskId } ?: uiState.finishedTasks.find { it.id == taskId }
    }

    if (task == null) return

    var nameText by remember(task.id) { mutableStateOf(task.name) }
    var descriptionText by remember(task.id) { mutableStateOf(task.description) }
    var pickerContext by remember { mutableStateOf(PickerContext.NONE) }
    var tempReminderDate by remember { mutableStateOf<LocalDate?>(null) }

    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    ModalBottomSheet(
        onDismissRequest = {
            if (nameText.isNotBlank()) {
                viewModel.onEvent(TaskEvent.EditTask(task.copy(name = nameText, description = descriptionText)))
            }
            popBack()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxSize().testTag("EDITSHEET")
    ) {
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
                    } else pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        if (pickerContext == PickerContext.EXPIRATION_TIME || pickerContext == PickerContext.REMINDER_TIME) {
            TimePickerModal(
                onTimeSelected = { time ->
                    if (pickerContext == PickerContext.EXPIRATION_TIME) {
                        val newDateTime = LocalDateTime.of(task.expirationDate.toLocalDate(), time)
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(expirationDate = newDateTime, isAllDay = false)))
                    } else if (pickerContext == PickerContext.REMINDER_TIME && tempReminderDate != null) {
                        val newReminder = LocalDateTime.of(tempReminderDate, time)
                        val updatedReminders = (task.reminderList + newReminder).distinct().sorted()
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(reminderList = updatedReminders)))
                    }
                    pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        if (pickerContext == PickerContext.REMINDER_OPTIONS) {
            PredefinedRemindersModal(
                onOptionSelected = { reminderTime ->
                    if (reminderTime != null) {
                        val updatedReminders = (task.reminderList + reminderTime).distinct().sorted()
                        viewModel.onEvent(TaskEvent.EditTask(task.copy(reminderList = updatedReminders)))
                        pickerContext = PickerContext.NONE
                    } else pickerContext = PickerContext.REMINDER_DATE
                },
                onDismiss = { pickerContext = PickerContext.NONE },
                taskExpirationDate = task.expirationDate
            )
        }
        
        if (pickerContext == PickerContext.RECURRENCE) {
            RecurrencePickerModal(
                current = task.recurrence,
                onSelected = { 
                    viewModel.onEvent(TaskEvent.EditTask(task.copy(recurrence = it)))
                    pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            TopBar(
                scrollBehavior = scrollBehavior,
                title = "Éditer la tâche",
                subtitle = "Personnalisez votre moment",
                navigationIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                onStartClick = popBack
            )

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Nom de la tâche") },
                        textStyle = MaterialTheme.typography.headlineSmall,
                        shape = MaterialTheme.shapes.extraLarge,
                    )
                }

                item {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                        value = descriptionText,
                        onValueChange = { descriptionText = it },
                        label = { Text("Description") },
                        shape = MaterialTheme.shapes.large,
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.RECURRENCE }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Outlined.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Récurrence", style = MaterialTheme.typography.bodyLarge)
                                }
                                Text(task.recurrence.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Timing & Rappels", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Toute la journée", style = MaterialTheme.typography.bodyLarge)
                                }
                                Switch(
                                    checked = task.isAllDay,
                                    onCheckedChange = { viewModel.onEvent(TaskEvent.EditTask(task.copy(isAllDay = it))) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.EXPIRATION_DATE }.padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Date", style = MaterialTheme.typography.bodyLarge)
                                }
                                Text(task.expirationDate.format(dateFormatter), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (!task.isAllDay) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.EXPIRATION_TIME }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("Heure", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text(task.expirationDate.format(timeFormatter), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Rappels", style = MaterialTheme.typography.bodyLarge)
                                }
                                IconButton(onClick = { pickerContext = PickerContext.REMINDER_OPTIONS }) {
                                    Icon(Icons.Outlined.AddAlert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                FloatingActionButton(
                    onClick = { popBack(); viewModel.onEvent(TaskEvent.Delete(task)) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.align(Alignment.BottomStart)
                ) { Icon(Icons.Outlined.Delete, contentDescription = null) }
                
                ExtendedFloatingActionButton(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            viewModel.onEvent(TaskEvent.EditTask(task.copy(name = nameText, description = descriptionText)))
                            popBack()
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    containerColor = if (nameText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (nameText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    icon = { Icon(Icons.Outlined.Check, contentDescription = null) },
                    text = { Text("Enregistrer", fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}
