package com.kayak.yakak.ui.maps

import com.kayak.yakak.data.Location
import com.kayak.yakak.data.Task

data class MapsUiState(
    val pendingTasks: List<Task> = emptyList(),
    val finishedTasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val lastUserLocation: Location? = null
)

sealed class MapsEvent {
    data class OnNewTask(val task: Task) : MapsEvent()
    data class OnPermissionResult(val granted: Boolean) : MapsEvent()
}
