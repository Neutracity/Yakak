package com.kayak.yakak.ui.maps

import android.content.Context
import android.content.Intent
import com.kayak.yakak.data.Task
import com.kayak.yakak.utils.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProximityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notifiedTasks = mutableSetOf<Int>()

    fun checkProximity(userLocation: GeoPoint, tasks: List<Task>) {
        tasks.forEach { task ->
            if (task.location.latitude != 0.0 && task.location.longitude != 0.0) {
                val taskLocation = GeoPoint(task.location.latitude, task.location.longitude)
                val distance = userLocation.distanceToAsDouble(taskLocation)
                
                if (distance < 300.0 && !notifiedTasks.contains(task.id)) {
                    sendNotification(task)
                    notifiedTasks.add(task.id)
                } else if (distance > 500.0) {
                    notifiedTasks.remove(task.id)
                }
            }
        }
    }

    private fun sendNotification(task: Task) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_NAME", "À proximité : ${task.name}")
            putExtra("TASK_ID", task.id)
            putExtra("TASK_DESC", "Vous êtes à moins de 300m de cet objectif.")
        }
        context.sendBroadcast(intent)
    }
}
