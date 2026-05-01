package com.kayak.yakak.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.data.Location
import com.kayak.yakak.data.RecurrenceFrequency
import com.kayak.yakak.data.Task
import com.kayak.yakak.domain.usecase.AddTaskUseCase
import com.kayak.yakak.domain.usecase.DeleteTaskUseCase
import com.kayak.yakak.domain.usecase.GetTasksUseCase
import com.kayak.yakak.domain.usecase.UpdateTaskUseCase
import com.kayak.yakak.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

sealed class TaskEvent {
    data class Delete(val task: Task) : TaskEvent()
    data class EditState(val task: Task, val newState: Boolean) : TaskEvent()
    data class EditTitle(val task: Task, val newTitle: String) : TaskEvent()
    data class EditDescription(val task: Task, val newDescription: String) : TaskEvent()
    data class EditDate(val task: Task, val newDate: LocalDateTime) : TaskEvent()
    data class EditLocation(val task: Task, val newLocation: Location) : TaskEvent()
    data class NewTask(val task: Task) : TaskEvent()
    data class EditTask(val task: Task) : TaskEvent()
}

data class TaskListUiState(
    val birthdaysToday: List<Task> = emptyList(),
    val mixOfTheDay: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val finishedTasks: List<Task> = emptyList(),
    val pendingTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TaskListVM @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskListUiState())
    var uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1200)
            getTasksUseCase().collect { allTasks ->
                val now = LocalDate.now()
                val birthdaysToday = allTasks.filter { 
                    it.isBirthday && it.expirationDate.toLocalDate().isEqual(now) 
                }
                val mixOfTheDay = allTasks.filter { task ->
                    !task.isCompleted && !task.isBirthday && (
                        task.expirationDate.toLocalDate().isEqual(now) ||
                        (task.recurrence != RecurrenceFrequency.NONE && isScheduledForToday(task, now))
                    )
                }.sortedBy { it.expirationDate }

                val upcomingTasks = allTasks.filter { task ->
                    !task.isCompleted && !task.isBirthday && 
                    task.expirationDate.toLocalDate().isAfter(now) &&
                    task.recurrence == RecurrenceFrequency.NONE
                }.sortedBy { it.expirationDate }

                val finishedTasks = allTasks.filter { it.isCompleted }
                    .sortedWith(
                        compareByDescending<Task> { it.finishedDate }
                            .thenByDescending { it.id }
                    )

                _uiState.update { currentState ->
                    currentState.copy(
                        birthdaysToday = birthdaysToday,
                        mixOfTheDay = mixOfTheDay,
                        upcomingTasks = upcomingTasks,
                        finishedTasks = finishedTasks,
                        pendingTasks = allTasks.filter { !it.isCompleted },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun isScheduledForToday(task: Task, today: LocalDate): Boolean {
        if (task.recurrence == RecurrenceFrequency.NONE) return task.expirationDate.toLocalDate().isEqual(today)
        val startDate = task.creationDate.toLocalDate()
        if (today.isBefore(startDate)) return false
        return when (task.recurrence) {
            RecurrenceFrequency.DAILY -> true
            RecurrenceFrequency.WEEKLY -> startDate.dayOfWeek == today.dayOfWeek
            RecurrenceFrequency.MONTHLY -> startDate.dayOfMonth == today.dayOfMonth
            RecurrenceFrequency.YEARLY -> startDate.dayOfMonth == today.dayOfMonth && startDate.month == today.month
            else -> false
        }
    }

    fun onEvent(event: TaskEvent) {
        viewModelScope.launch {
            when (event) {
                is TaskEvent.Delete -> {
                    event.task.reminderList.forEach { time ->
                        reminderScheduler.cancel(event.task, time)
                    }
                    deleteTaskUseCase(event.task)
                }
                is TaskEvent.EditState -> {
                    if (event.newState) {
                        var updatedTask = event.task.copy(isCompleted = true, finishedDate = LocalDateTime.now())
                        if (event.task.recurrence != RecurrenceFrequency.NONE) {
                            val today = LocalDate.now()
                            val yesterday = today.minusDays(1)
                            val lastCompleted = event.task.lastCompletedDate?.toLocalDate()
                            val newStreak = if (lastCompleted == yesterday || (lastCompleted == null && event.task.creationDate.toLocalDate() == today)) {
                                event.task.streakCount + 1
                            } else if (lastCompleted == today) {
                                event.task.streakCount
                            } else {
                                1
                            }
                            updatedTask = updatedTask.copy(streakCount = newStreak, lastCompletedDate = LocalDateTime.now())
                        }
                        event.task.reminderList.forEach { time ->
                            reminderScheduler.cancel(event.task, time)
                        }
                        updateTaskUseCase(updatedTask)
                    } else {
                        updateTaskUseCase(event.task.copy(isCompleted = false, finishedDate = null))
                    }
                }
                is TaskEvent.EditTitle -> updateTaskUseCase(event.task.copy(name = event.newTitle))
                is TaskEvent.EditDescription -> updateTaskUseCase(event.task.copy(description = event.newDescription))
                is TaskEvent.NewTask -> {
                    var taskToSave = event.task
                    if (event.task.isBirthday) {
                        val birthday = event.task.expirationDate
                        val reminders = listOf(
                            birthday.minusWeeks(1).withHour(9).withMinute(0),
                            birthday.minusDays(2).withHour(9).withMinute(0),
                            birthday.withHour(9).withMinute(0)
                        ).filter { it.isAfter(LocalDateTime.now()) }
                        taskToSave = event.task.copy(reminderList = reminders, recurrence = RecurrenceFrequency.YEARLY, isAllDay = true)
                    }
                    taskToSave.reminderList.forEach { time ->
                        if (time.isAfter(LocalDateTime.now())) {
                            reminderScheduler.schedule(taskToSave, time)
                        }
                    }
                    addTaskUseCase(taskToSave)
                }
                is TaskEvent.EditDate -> updateTaskUseCase(event.task.copy(expirationDate = event.newDate))
                is TaskEvent.EditLocation -> updateTaskUseCase(event.task.copy(location = event.newLocation))
                is TaskEvent.EditTask -> {
                    event.task.reminderList.forEach { time ->
                        if (time.isAfter(LocalDateTime.now())) {
                            reminderScheduler.schedule(event.task, time)
                        }
                    }
                    updateTaskUseCase(event.task)
                }
            }
        }
    }
}