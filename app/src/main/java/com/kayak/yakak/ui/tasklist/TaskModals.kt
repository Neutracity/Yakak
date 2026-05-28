package com.kayak.yakak.ui.tasklist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kayak.yakak.R
import com.kayak.yakak.data.RecurrenceFrequency
import java.time.*

enum class PickerContext { NONE, EXPIRATION_DATE, EXPIRATION_TIME, REMINDER_OPTIONS, REMINDER_DATE, REMINDER_TIME, RECURRENCE }

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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
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
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

@Composable
fun PredefinedRemindersModal(
    onOptionSelected: (LocalDateTime?) -> Unit,
    onDismiss: () -> Unit,
    taskExpirationDate: LocalDateTime
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_reminder)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val options = listOf(
                    stringResource(R.string.reminder_30m_before) to taskExpirationDate.minusMinutes(30),
                    stringResource(R.string.reminder_1h_before) to taskExpirationDate.minusHours(1),
                    stringResource(R.string.reminder_1d_before) to taskExpirationDate.minusDays(1).withHour(12).withMinute(0),
                    stringResource(R.string.reminder_1w_before) to taskExpirationDate.minusWeeks(1)
                )

                options.forEach { (label, time) ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOptionSelected(time) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Text(label)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOptionSelected(null) }
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                        Text(stringResource(R.string.reminder_custom))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun RecurrencePickerModal(
    current: RecurrenceFrequency,
    onSelected: (RecurrenceFrequency) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.recurrence_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RecurrenceFrequency.entries.forEach { freq ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onSelected(freq) },
                        colors = if (current == freq) ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ButtonDefaults.textButtonColors()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            Text(
                                when(freq) {
                                    RecurrenceFrequency.NONE -> stringResource(R.string.recurrence_none)
                                    RecurrenceFrequency.DAILY -> stringResource(R.string.recurrence_daily)
                                    RecurrenceFrequency.WEEKLY -> stringResource(R.string.recurrence_weekly)
                                    RecurrenceFrequency.MONTHLY -> stringResource(R.string.recurrence_monthly)
                                    RecurrenceFrequency.YEARLY -> stringResource(R.string.recurrence_yearly)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
