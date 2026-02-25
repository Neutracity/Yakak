package com.kayak.yakak.ui.tasklist

import android.location.Location
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.kayak.yakak.Task
import com.kayak.yakak.utils.getTaskList
import java.io.Console

sealed class TaskEvent {
    data class Delete(val task: Task) : TaskEvent()
    data class EditState(val task: Task,val newState: Boolean) : TaskEvent()
    data class EditTitle(val task: Task,val newTitle: String) : TaskEvent()
    data class EditDescription(val task: Task,val newDescription: String) : TaskEvent()
    data class EditDate(val task: Task,val newDescription: String) : TaskEvent()
    data class EditLocation(val task: Task, val newLocation: Location) : TaskEvent()

}

class TaskListVM : ViewModel(){
    private val _tasks : MutableState<List<Task>> = mutableStateOf(emptyList())
    var tasks : State<List<Task>> = _tasks

    init {
        _tasks.value = getTaskList()
    }

    fun onEvent(event: TaskEvent){
        when (event){
            is TaskEvent.Delete ->{

            }
            is TaskEvent.EditState ->{
                _tasks.value = _tasks.value.map { task ->
                    if(task.id == event.task.id){
                        task.copy(isCompleted = event.newState)
                    }else{
                        task
                    }
                }
                Log.println(Log.INFO,"test","${event.task.isCompleted}")
            }
            else -> {}
        }
    }


}