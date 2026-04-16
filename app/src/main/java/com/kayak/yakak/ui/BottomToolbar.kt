package com.kayak.yakak.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 1,
    expanded: Boolean = true,
    onAgendaClick: () -> Unit = {},
    onTaskListClick: () -> Unit = {},
    onMapsClick: () -> Unit = {},
    onAddNormalTask: () -> Unit = {},
    onAddRecurringTask: () -> Unit = {},
    onAddBirthday: () -> Unit = {},
    onZoomClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    var fabMenuExpanded by remember { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim
        AnimatedVisibility(
            visible = fabMenuExpanded,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { fabMenuExpanded = false }
            )
        }

        Box(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.2f to Color.Transparent,
                        1.0f to colorScheme.surfaceContainer
                    )
                )
                .padding(bottom = 30.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                //verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
                //horizontalArrangement = Arrangement.Center
            ) {
                // Navigation Bar
                HorizontalFloatingToolbar(
                    expanded = expanded && !fabMenuExpanded,
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                    modifier = Modifier.zIndex(1f).align(Alignment.BottomStart).padding(start = 64.dp)
                ) {
                    val items = listOf(
                        Triple("Agenda", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        Triple("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
                        Triple("Maps", Icons.Filled.Map, Icons.Outlined.Map)
                    )
                    items.forEachIndexed { index, item ->
                        ToggleButton(
                            checked = selectedIndex == index,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                when (index) {
                                    0 -> onAgendaClick()
                                    1 -> onTaskListClick()
                                    2 -> onMapsClick()
                                }
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                checkedContainerColor = colorScheme.primary,
                                checkedContentColor = colorScheme.onPrimary
                            ),
                            shapes = ToggleButtonDefaults.shapes(CircleShape, CircleShape, CircleShape),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Crossfade(targetState = selectedIndex == index, label = "icon") { isSelected ->
                                    Icon(if (isSelected) item.second else item.third, item.first)
                                }
                                AnimatedVisibility(
                                    visible = selectedIndex == index,
                                    enter = fadeIn() + expandHorizontally(),
                                    exit = fadeOut() + shrinkHorizontally()
                                ) {
                                    Text(
                                        text = item.first,
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(start = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Official FloatingActionButtonMenu
                FloatingActionButtonMenu(
                    modifier = Modifier.zIndex(2f).align(Alignment.BottomEnd).offset(y = 5.dp),
                    expanded = fabMenuExpanded,
                    button = {
                        ToggleFloatingActionButton(
                            modifier = Modifier.semantics { traversalIndex = -1f },
                            checked = fabMenuExpanded,
                            onCheckedChange = { 
                                fabMenuExpanded = it 
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            },
                        ) {
                            val imageVector by remember {
                                derivedStateOf {
                                    if (checkedProgress > 0.5f) Icons.Filled.Add else Icons.Filled.Add
                                }
                            }
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = checkedProgress * 45f
                                },
                            )
                        }
                    }
                ) {
                    FloatingActionButtonMenuItem(
                        onClick = { onAddBirthday(); fabMenuExpanded = false },
                        icon = { Icon(Icons.Default.Cake, contentDescription = null) },
                        text = { Text(text = "Anniversaire") },
                    )
                    FloatingActionButtonMenuItem(
                        onClick = { onAddNormalTask(); fabMenuExpanded = false },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text(text = "Tâche") },
                    )
                    FloatingActionButtonMenuItem(
                        onClick = { onAddRecurringTask(); fabMenuExpanded = false },
                        icon = { Icon(Icons.Default.Repeat, contentDescription = null) },
                        text = { Text(text = "Récurrente") },
                    )
                }
            }
        }
    }
}
