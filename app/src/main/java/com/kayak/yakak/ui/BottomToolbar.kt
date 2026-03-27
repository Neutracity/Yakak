package com.kayak.yakak.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 1,
    expanded: Boolean = true,
    onAgendaClick: () -> Unit = {},
    onTaskListClick: () -> Unit = {},
    onMapsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val vibrantColors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    val selectedTint = colorScheme.primary
    val unselectedTint = colorScheme.onSurfaceVariant
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                        0.2f to Color.Transparent,
                        1.0f to colorScheme.surfaceContainer,
                )
            )
            .padding(bottom = 30.dp),
        contentAlignment = Alignment.Center
    ){
        HorizontalFloatingToolbar(
            expanded = expanded,
            content = {
                IconButton(onClick = {
                    onAgendaClick()
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }) {
                    Icon(
                        imageVector = if (selectedIndex == 0) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                        contentDescription = "Agenda",
                        tint = if (selectedIndex == 0) selectedTint else unselectedTint
                    )
                }
                IconButton(onClick = {
                    onTaskListClick()
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }) {
                    Icon(
                        imageVector = if (selectedIndex == 1) Icons.Filled.Checklist else Icons.Outlined.Checklist,
                        contentDescription = "Task List",
                        tint = if (selectedIndex == 1) selectedTint else unselectedTint
                    )
                }
                IconButton(onClick = {
                    onMapsClick()
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                }) {
                    Icon(
                        imageVector = if (selectedIndex == 2) Icons.Filled.Map else Icons.Outlined.Map,
                        contentDescription = "Maps",
                        tint = if (selectedIndex == 2) selectedTint else unselectedTint
                    )
                }
            },
            floatingActionButton = {
                AnimatedVisibility(
                   visible = expanded,
                ) {
                    if (selectedIndex != 2) {
                        FloatingActionButton(
                            onClick = onAddClick,
                            content = {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                        )
                    }else {
                        LargeFloatingActionButton(
                            onClick = onAddClick,
                            content = {
                                Icon(Icons.Default.MyLocation, contentDescription = "Location")
                            }
                        )
                    }
                }


            },
            colors = vibrantColors,
            modifier = modifier
        )

    }

}