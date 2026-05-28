package com.kayak.yakak.ui.tasklist

import android.annotation.SuppressLint
import android.content.ContentUris
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kayak.yakak.R
import com.kayak.yakak.data.RecurrenceFrequency
import com.kayak.yakak.data.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBirthdayView(
    popBack: () -> Unit = {},
    viewModel: TaskListVM,
    taskId: Int? = null,
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val uiState by viewModel.uiState.collectAsState()
    
    val initialTask = remember(uiState, taskId) {
        if (taskId != null) {
            uiState.pendingTasks.find { it.id == taskId } ?: uiState.finishedTasks.find { it.id == taskId }
        } else {
            Task(name = "", isBirthday = true, recurrence = RecurrenceFrequency.YEARLY, isAllDay = true)
        }
    }

    if (initialTask == null && taskId != null) return

    var nameText by remember(initialTask?.id) { mutableStateOf(initialTask?.name ?: "") }
    var expirationDate by remember(initialTask?.id) { mutableStateOf(initialTask?.expirationDate ?: LocalDateTime.now()) }
    var reminderList by remember(initialTask?.id) { 
        mutableStateOf(initialTask?.reminderList ?: emptyList()) 
    }
    
    // Fix: Pre-fill reminders if it's a new birthday or if they are missing
    remember(expirationDate) {
        if (reminderList.isEmpty()) {
             reminderList = listOf(
                expirationDate.minusWeeks(1).withHour(9).withMinute(0),
                expirationDate.minusDays(2).withHour(9).withMinute(0),
                expirationDate.withHour(9).withMinute(0)
            ).filter { it.isAfter(LocalDateTime.now()) }
        }
    }

    var profileImageUri by remember(initialTask?.id) { mutableStateOf(initialTask?.profileImageUri) }
    var pickerContext by remember { mutableStateOf(PickerContext.NONE) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { 
                try {
                    context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                profileImageUri = it.toString() 
            }
        }
    )

    val joyfulColors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer
    )

    ModalBottomSheet(
        onDismissRequest = {
            if(nameText.isEmpty()){
                initialTask?.let{ viewModel.onEvent(TaskEvent.Delete(initialTask))}
            }
            popBack()

        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxSize()
    ) {
        if (pickerContext == PickerContext.EXPIRATION_DATE) {
            DatePickerModal(
                onDateSelected = { dateMillis ->
                    if (dateMillis != null) {
                        val selectedDate = java.time.Instant.ofEpochMilli(dateMillis).atZone(java.time.ZoneOffset.UTC).toLocalDate()
                        expirationDate = LocalDateTime.of(selectedDate, expirationDate.toLocalTime())
                    }
                    pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE }
            )
        }

        if (pickerContext == PickerContext.REMINDER_OPTIONS) {
            PredefinedRemindersModal(
                onOptionSelected = { time ->
                    if (time != null) {
                        reminderList = (reminderList + time).distinct().sorted()
                    }
                    pickerContext = PickerContext.NONE
                },
                onDismiss = { pickerContext = PickerContext.NONE },
                taskExpirationDate = expirationDate
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            // Header with Bold Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = popBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                }
                Text(
                    text = if (nameText.isBlank()) stringResource(R.string.edit_birthday_default_name) else nameText,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-2).sp,
                        lineHeight = 44.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Photo Picker
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(Brush.linearGradient(joyfulColors))
                                .clickable { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (profileImageUri == null) {
                                Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            } else {
                                AsyncImage(
                                    model = profileImageUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text(stringResource(R.string.edit_birthday_person_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent)
                    )
                }

                item {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { pickerContext = PickerContext.EXPIRATION_DATE }) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(stringResource(R.string.edit_birthday_date), style = MaterialTheme.typography.labelMedium)
                                    Text(expirationDate.format(DateTimeFormatter.ofPattern("dd MMMM")), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Outlined.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(stringResource(R.string.edit_task_recurrence), style = MaterialTheme.typography.labelMedium)
                                    Text(stringResource(R.string.edit_birthday_every_year), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.edit_task_reminders), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { pickerContext = PickerContext.REMINDER_OPTIONS }) {
                                Icon(Icons.Outlined.AddAlert, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        
                        if (reminderList.isEmpty()) {
                            Text(stringResource(R.string.edit_birthday_no_reminders), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val dateAt = stringResource(R.string.date_at)
                            reminderList.forEach { reminder ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Text(reminder.format(DateTimeFormatter.ofPattern("dd MMM '$dateAt' HH:mm")))
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = { reminderList = reminderList - reminder }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().padding(24.dp)) {
                if (taskId != null) {
                    FloatingActionButton(
                        onClick = { popBack(); initialTask?.let { viewModel.onEvent(TaskEvent.Delete(it)) } },
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) { Icon(Icons.Outlined.Delete, contentDescription = null) }
                }
                
                ExtendedFloatingActionButton(
                    onClick = {
                        if (nameText.isNotBlank()) {
                            val taskToSave = (initialTask ?: Task()).copy(
                                name = nameText,
                                expirationDate = expirationDate,
                                reminderList = reminderList,
                                isBirthday = true,
                                recurrence = RecurrenceFrequency.YEARLY,
                                isAllDay = true,
                                profileImageUri = profileImageUri
                            )
                            if (taskId == null) {
                                viewModel.onEvent(TaskEvent.NewTask(taskToSave))
                            } else {
                                viewModel.onEvent(TaskEvent.EditTask(taskToSave))
                            }
                            popBack()
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd),
                    containerColor = if (nameText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (nameText.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    icon = { Icon(Icons.Outlined.Celebration, contentDescription = null) },
                    text = { Text(stringResource(R.string.edit_birthday_party), fontWeight = FontWeight.Bold) }
                )
            }
        }
    }
}
