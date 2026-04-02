package com.kayak.yakak.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBar(    modifier: Modifier = Modifier,
                  selectedIndex: Int = 1,
                  expanded: Boolean = true,
                  onAgendaClick: () -> Unit = {},
                  onTaskListClick: () -> Unit = {},
                  onMapsClick: () -> Unit = {},
                  onAddClick: () -> Unit = {},
                  onZoomClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val motionScheme = motionScheme

    val items = listOf(
        Triple("Agenda", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
        Triple("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist),
        Triple("Maps", Icons.Filled.Map, Icons.Outlined.Map)
    )

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
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = selectedIndex == 2,
                enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                        slideInVertically(motionScheme.defaultSpatialSpec()) { it / 2 },
                exit = fadeOut(motionScheme.defaultEffectsSpec()) +
                        slideOutVertically(motionScheme.defaultSpatialSpec()) { it / 2 }
            ) {
                FloatingActionButton(
                    onClick = onZoomClick,
                    modifier = Modifier.padding(bottom = 12.dp),
                    content = { Icon(Icons.Default.MyLocation, contentDescription = "Location") }
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                HorizontalFloatingToolbar(
                    expanded = expanded,
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                    modifier = Modifier.zIndex(1f),
                    content = {
                        items.forEachIndexed { index, item ->
                            val selected = selectedIndex == index
                            ToggleButton(
                                checked = selected,
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
                                    Crossfade(
                                        targetState = selected,
                                        animationSpec = motionScheme.defaultEffectsSpec(),
                                        label = "icon_fade"
                                    ) { isSelected ->
                                        Icon(
                                            imageVector = if (isSelected) item.second else item.third,
                                            contentDescription = item.first
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                                                expandHorizontally(motionScheme.defaultSpatialSpec()),
                                        exit = fadeOut(motionScheme.defaultEffectsSpec()) +
                                                shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                    ) {
                                        Text(
                                            text = item.first,
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                AnimatedVisibility(
                    visible = selectedIndex != 2,
                    enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                            expandHorizontally(motionScheme.defaultSpatialSpec(), expandFrom = Alignment.Start),
                    exit = fadeOut(motionScheme.defaultEffectsSpec()) +
                            shrinkHorizontally(motionScheme.defaultSpatialSpec(), shrinkTowards = Alignment.Start)
                ) {
                    FloatingActionButton(
                        onClick = onAddClick,
                        modifier = Modifier.padding(start = 8.dp),
                        content = { Icon(Icons.Default.Add, contentDescription = "Add") }
                    )
                }
            }
        }
    }
}