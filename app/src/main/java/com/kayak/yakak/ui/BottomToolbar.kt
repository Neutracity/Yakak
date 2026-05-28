package com.kayak.yakak.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kayak.yakak.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int = 1,
    expanded: Boolean = true,
    fabMenuExpanded: Boolean = false,
    onFabMenuToggle: (Boolean) -> Unit = {},
    onAgendaClick: () -> Unit = {},
    onTaskListClick: () -> Unit = {},
    onMapsClick: () -> Unit = {},
    onAddNormalTask: () -> Unit = {},
    onAddRecurringTask: () -> Unit = {},
    onAddBirthday: () -> Unit = {},
    onZoomClick: () -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current
    val isMapPage = selectedIndex == 2

    BackHandler(fabMenuExpanded) { onFabMenuToggle(false) }

    Box(
        modifier = if (fabMenuExpanded) Modifier.fillMaxSize() else modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        // Full screen scrim when expanded
        if (fabMenuExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onFabMenuToggle(false) }
                    .zIndex(10f)
            )
        }

        // Navigation Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.2f to Color.Transparent,
                        1.0f to colorScheme.surfaceContainer
                    )
                )
                .padding(bottom = 30.dp)
                .zIndex(if (fabMenuExpanded) 5f else 1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                HorizontalFloatingToolbar(
                    expanded = expanded && !fabMenuExpanded,
                    colors = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                    modifier = Modifier.zIndex(1f).align(Alignment.BottomStart).padding(start = 64.dp)
                ) {
                    val items = listOf(
                        Triple(stringResource(R.string.nav_agenda), Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
                        Triple(stringResource(R.string.nav_tasks), Icons.Filled.Checklist, Icons.Outlined.Checklist),
                        Triple(stringResource(R.string.nav_maps), Icons.Filled.Map, Icons.Outlined.Map)
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
            }
        }

        // FAB Menu - Higher zIndex than Scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp, end = 16.dp, bottom = 30.dp,
                )
                .zIndex(20f),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButtonMenu(
                modifier = Modifier.offset(y = 5.dp).testTag("FABMENU"),
                expanded = fabMenuExpanded && !isMapPage,
                button = {
                    ToggleFloatingActionButton(
                        modifier = Modifier.semantics { traversalIndex = -1f }.zIndex(21f).testTag("FAB"),
                        checked = fabMenuExpanded && !isMapPage,
                        onCheckedChange = { 
                            if (isMapPage) {
                                onZoomClick()
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            } else {
                                onFabMenuToggle(it) 
                                haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            }
                        },
                    ) {
                        Crossfade(targetState = isMapPage, label = "fab_icon") { targetIsMap ->
                            val imageVector = if (targetIsMap) Icons.Filled.MyLocation else Icons.Filled.Add
                            Icon(
                                painter = rememberVectorPainter(imageVector),
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer {
                                    rotationZ = if (!targetIsMap) checkedProgress * 45f else 0f
                                },
                            )
                        }
                    }
                }
            ) {
                FloatingActionButtonMenuItem(
                    onClick = { onAddBirthday(); onFabMenuToggle(false) },
                    icon = { Icon(Icons.Default.Cake, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.fab_birthday)) },
                    modifier = Modifier.zIndex(22f)
                )
                FloatingActionButtonMenuItem(
                    onClick = { onAddNormalTask(); onFabMenuToggle(false) },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.fab_task)) },
                    modifier = Modifier.zIndex(22f).testTag("ADDTASK")
                )
                FloatingActionButtonMenuItem(
                    onClick = { onAddRecurringTask(); onFabMenuToggle(false) },
                    icon = { Icon(Icons.Default.Repeat, contentDescription = null) },
                    text = { Text(text = stringResource(R.string.fab_recurring)) },
                    modifier = Modifier.zIndex(22f)
                )
            }
        }
    }
}
