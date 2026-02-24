package com.kayak.yakak.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBar(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onAgendaClick: () -> Unit = {},
    onTaskListClick: () -> Unit = {},
    onMapsClick: () -> Unit = {}
) {
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    Box(
        modifier = modifier.fillMaxWidth().padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ){
        HorizontalFloatingToolbar(
            expanded = true,
            content = {
                IconButton(onClick = onAgendaClick) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Agenda")
                }
                IconButton(onClick = onTaskListClick) {
                    Icon(Icons.Default.Checklist, contentDescription = "Task List")
                }
                IconButton(onClick = onMapsClick) {
                    Icon(Icons.Default.Map, contentDescription = "Maps")
                }
            },
            floatingActionButton = {
                FloatingToolbarDefaults.VibrantFloatingActionButton(
                    onClick = {  },
                    content = {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                )
            },
            colors = vibrantColors,
            modifier = modifier
        )

    }

}