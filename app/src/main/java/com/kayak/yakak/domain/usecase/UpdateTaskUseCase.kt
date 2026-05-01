package com.kayak.yakak.domain.usecase

import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import jakarta.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        require(task.name.isNotBlank()) { "Task name cannot be empty" }

        /*val today = LocalDateTime.now()
        require(!task.expirationDate.isBefore(today)) {
            "Cannot update task to a date in the past"
        }*/

        repository.updateTask(task)
    }

}
