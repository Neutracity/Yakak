package com.kayak.yakak.ui.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kayak.yakak.data.Location
import com.kayak.yakak.data.Task
import com.kayak.yakak.domain.usecase.AddTaskUseCase
import com.kayak.yakak.domain.usecase.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapsVM @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val proximityManager: ProximityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getTasksUseCase().collect { allTasks ->
                _uiState.update { it.copy(
                    pendingTasks = allTasks.filter { !it.isCompleted },
                    finishedTasks = allTasks.filter { it.isCompleted },
                    isLoading = false
                ) }
            }
        }
    }

    fun onEvent(event: MapsEvent) {
        viewModelScope.launch {
            when (event) {
                is MapsEvent.OnMapLongClick -> {
                    val newTask = Task(
                        name = "Nouveau repère",
                        location = Location(event.latitude, event.longitude)
                    )
                    addTaskUseCase(newTask)
                }
                is MapsEvent.OnPermissionResult -> {
                    // Handle permission status if needed
                }
            }
        }
    }

    fun onLocationUpdate(latitude: Double, longitude: Double) {
        _uiState.update { it.copy(lastUserLocation = Location(latitude, longitude)) }
        proximityManager.checkProximity(
            org.osmdroid.util.GeoPoint(latitude, longitude),
            _uiState.value.pendingTasks
        )
    }
}
