package com.example.medilife

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MediLifeApp(
    viewModel: AppViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentMode by viewModel.currentMode.collectAsState()
    val appearanceMode by viewModel.appearanceMode.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != "auth"
    val showSettingsShortcut = currentRoute != null && currentRoute != "auth" && currentRoute != "settings"

    MediLifeTheme(
        darkTheme = when (appearanceMode) {
            AppearanceMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            AppearanceMode.LIGHT -> false
            AppearanceMode.DARK -> true
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    Column {
                        if (showSettingsShortcut) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                ExtendedFloatingActionButton(
                                    onClick = { navController.navigate("settings") },
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.Settings,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Settings", color = MaterialTheme.colorScheme.onSurface)
                                    Icon(
                                        Icons.Outlined.ChevronRight,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        MediLifeBottomNavigation(navController = navController, currentMode = currentMode)
                    }
                }
            }
        ) { innerPadding ->
            MediLifeNavGraph(
                navController = navController,
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
