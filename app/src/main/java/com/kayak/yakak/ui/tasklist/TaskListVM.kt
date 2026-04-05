package com.kayak.yakak.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.data.Location
import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import com.kayak.yakak.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

sealed class TaskEvent {
    data class Delete(val task: Task) : TaskEvent()
    data class EditState(val task: Task,val newState: Boolean) : TaskEvent()
    data class EditTitle(val task: Task,val newTitle: String) : TaskEvent()
    data class EditDescription(val task: Task,val newDescription: String) : TaskEvent()
    data class EditDate(val task: Task,val newDate: LocalDateTime) : TaskEvent()
    data class EditLocation(val task: Task, val newLocation: Location) : TaskEvent()
    data class NewTask(val task: Task) : TaskEvent()

    data class EditTask(val task: Task) : TaskEvent()

}

data class TaskListUiState(
    val pendingTasks: List<Task> = emptyList(),
    val finishedTasks: List<Task> = emptyList()
)
@HiltViewModel
class TaskListVM @Inject constructor(
    private val taskRepository: TaskRepository,
    private val reminderScheduler: ReminderScheduler
): ViewModel(){
    private val _uiState = MutableStateFlow(TaskListUiState())
    var uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.tasks.collect { allTasks ->
                _uiState.update { currentState ->
                    currentState.copy(
                        pendingTasks = allTasks.filter { !it.isCompleted },
                        finishedTasks = allTasks.filter { it.isCompleted }
                            .sortedWith(
                                compareByDescending<Task> { it.finishedDate }
                                    .thenByDescending { it.id }
                            )
                    )
                }
            }
        }

    }

    fun onEvent(event: TaskEvent){
        viewModelScope.launch {
            when (event){
                is TaskEvent.Delete ->{
                    event.task.reminderList.forEach { time ->
                        reminderScheduler.cancel(event.task, time)
                    }
                    taskRepository.deleteTask(event.task)
                }
                is TaskEvent.EditState ->{
                    if (event.newState) {
                        event.task.reminderList.forEach { time ->
                            reminderScheduler.cancel(event.task, time)
                        }
                        taskRepository.updateTask(event.task.copy(isCompleted = true, finishedDate = LocalDateTime.now()))
                    } else {
                        taskRepository.updateTask(event.task.copy(isCompleted = false, finishedDate = null))
                    }
                }
                is TaskEvent.EditTitle ->{
                    taskRepository.updateTask(event.task.copy(name = event.newTitle))
                }
                is TaskEvent.EditDescription ->{
                    taskRepository.updateTask(event.task.copy(description = event.newDescription))
                }
                is TaskEvent.NewTask ->{
                    event.task.reminderList.forEach { time ->
                        if (time.isAfter(LocalDateTime.now())) {
                            reminderScheduler.schedule(event.task, time)
                        }
                    }
                    taskRepository.addTask(event.task)
                }
                is TaskEvent.EditDate -> {
                    taskRepository.updateTask(event.task.copy(expirationDate = event.newDate))
                }
                is TaskEvent.EditLocation -> {
                    taskRepository.updateTask(event.task.copy(location = event.newLocation))
                }
                is TaskEvent.EditTask -> {
                    event.task.reminderList.forEach { time ->
                        if (time.isAfter(LocalDateTime.now())) {
                            reminderScheduler.schedule(event.task, time)
                        }
                    }
                    taskRepository.updateTask(event.task)
                }

            }
        }

    }


}