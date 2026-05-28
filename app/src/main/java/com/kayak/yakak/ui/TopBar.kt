package com.kayak.yakak.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.res.stringResource
import com.kayak.yakak.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String = "",
    subtitle: String = "",
    navigationIcon: ImageVector = Icons.Outlined.Menu,
    onStartClick: () -> Unit = {}
){
    TopAppBar(
        title = { Text(title)},
        navigationIcon = {
            IconButton(
                onClick = onStartClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = colorScheme.surface,
                    contentColor = colorScheme.onSurface
                )

            ) { Icon(navigationIcon,contentDescription = stringResource(R.string.navigation_icon)) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surfaceContainer,
            titleContentColor = colorScheme.onSurface
        ),
        scrollBehavior = scrollBehavior,
        modifier = modifier

    )

}