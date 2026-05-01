package com.kayak.yakak.domain.usecase

import com.kayak.yakak.data.Task
import com.kayak.yakak.data.TaskRepository
import jakarta.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.addTask(task)
    }
}
