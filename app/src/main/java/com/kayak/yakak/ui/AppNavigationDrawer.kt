package com.kayak.yakak.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.ModalWideNavigationRail
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppNavigationDrawer(
    state: WideNavigationRailState,
    selectedIndex: Int,
    onPageSelected: (Int) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()

    val items = listOf(
        NavigationItem("Agenda", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, 0),
        NavigationItem("Tasks", Icons.Filled.Checklist, Icons.Outlined.Checklist, 1),
        NavigationItem("Maps", Icons.Filled.Map, Icons.Outlined.Map, 2),
        NavigationItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, -1),
        NavigationItem("About", Icons.Filled.Info, Icons.Outlined.Info, -1)
    )

    Row {
        ModalWideNavigationRail(
            state = state,
            header = { },
            hideOnCollapse = true
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                items.forEach { item ->
                    val isSelected = selectedIndex == item.index && item.index != -1

                    WideNavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            scope.launch {
                                state.collapse()
                                if (item.index != -1) {
                                    onPageSelected(item.index)
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        railExpanded = true,
                        colors = WideNavigationRailItemDefaults.colors(
                            selectedIconColor = colorScheme.onSecondaryContainer,
                            selectedTextColor = colorScheme.onSecondaryContainer,
                            selectedIndicatorColor = colorScheme.secondaryContainer,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

private data class NavigationItem(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val index: Int
)