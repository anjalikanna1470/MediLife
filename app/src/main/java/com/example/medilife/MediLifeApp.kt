package com.example.medilife

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MediLifeApp(
    viewModel: AppViewModel = viewModel()
) {
    val appearanceMode by viewModel.appearanceMode.collectAsState()
    val currentMode by viewModel.currentMode.collectAsState()

    val useDarkTheme = when (appearanceMode) {
        AppearanceMode.SYSTEM -> isSystemInDarkTheme()
        AppearanceMode.LIGHT -> false
        AppearanceMode.DARK -> true
    }

    MediLifeTheme(darkTheme = useDarkTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Routes that display the bottom navigation bar
        val bottomNavRoutes = setOf(
            NavItem.Home.route,
            NavItem.Records.route,
            NavItem.Medicines.route,
            NavItem.AICopilot.route,
            NavItem.Profile.route,
            NavItem.Dashboard.route,
            NavItem.Patients.route,
            NavItem.Appointments.route,
            NavItem.ClinicalAI.route
        )

        val showBottomBar = currentRoute in bottomNavRoutes

        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        MediLifeBottomNavigation(
                            navController = navController,
                            currentMode = currentMode
                        )
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
}
