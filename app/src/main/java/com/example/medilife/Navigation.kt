package com.example.medilife

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {

    object Home :
        NavItem(
            "home",
            "Home",
            Icons.Outlined.Home,
            Icons.Default.Home
        )

    object Records :
        NavItem(
            "records",
            "Records",
            Icons.Outlined.Folder,
            Icons.Default.Folder
        )

    object Medicines :
        NavItem(
            "medicines",
            "Medicines",
            Icons.Outlined.Medication,
            Icons.Default.Medication
        )

    object AICopilot :
        NavItem(
            "ai_copilot",
            "CareMate",
            Icons.Outlined.SmartToy,
            Icons.Default.SmartToy
        )

    object Profile :
        NavItem(
            "profile",
            "Profile",
            Icons.Outlined.Person,
            Icons.Default.Person
        )

    object Dashboard :
        NavItem(
            "doctor_dashboard",
            "Dashboard",
            Icons.Outlined.Dashboard,
            Icons.Default.Dashboard
        )

    object Patients :
        NavItem(
            "doctor_patients",
            "Patients",
            Icons.Outlined.People,
            Icons.Default.People
        )

    object Appointments :
        NavItem(
            "appointments",
            "Appointments",
            Icons.Outlined.CalendarMonth,
            Icons.Default.CalendarMonth
        )

    object ClinicalAI :
        NavItem(
            "doctor_clinical_ai",
            "Clinical AI",
            Icons.Outlined.MedicalServices,
            Icons.Default.MedicalServices
        )
}

@Composable
fun MediLifeBottomNavigation(
    navController: NavHostController,
    currentMode: UserMode
) {

    val items =
        if (currentMode == UserMode.PATIENT) {

            listOf(
                NavItem.Home,
                NavItem.Records,
                NavItem.Medicines,
                NavItem.AICopilot,
                NavItem.Profile
            )

        } else {

            listOf(
                NavItem.Dashboard,
                NavItem.Patients,
                NavItem.Appointments,
                NavItem.ClinicalAI,
                NavItem.Profile
            )
        }

    val navBackStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor =
            MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {

        items.forEach { item ->

            val selected =
                currentRoute == item.route

            NavigationBarItem(

                icon = {

                    Icon(
                        imageVector =
                            if (selected) {
                                item.selectedIcon
                            } else {
                                item.icon
                            },
                        contentDescription =
                            item.title
                    )
                },

                label = {

                    Text(
                        text = item.title,
                        style =
                            MaterialTheme.typography.labelMedium,
                        fontSize = 10.sp,
                        fontWeight =
                            if (selected) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                    )
                },

                selected = selected,

                onClick = {

                    if (currentRoute != item.route) {

                        navController.navigate(
                            item.route
                        ) {

                            popUpTo(
                                navController.graph.startDestinationId
                            ) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MediLifeNavGraph(
    navController: NavHostController,
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = modifier
    ) {

        // =====================================================
        // AUTH
        // =====================================================

        composable("auth") {

            AuthScreen(
                viewModel = viewModel,

                onLoginSuccess = { mode ->
                    val startRoute =
                        if (mode == UserMode.PATIENT) {
                            "home"
                        } else {
                            "doctor_dashboard"
                        }

                    navController.navigate(startRoute) {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },

                onNavigateToSignup = {
                    navController.navigate("signup")
                }
            )
        }

        composable("signup") {
            SignupScreen(
                viewModel = viewModel,

                onPatientOnboarding = {
                    navController.navigate("patient_onboarding")
                },

                onDoctorOnboarding = {
                    navController.navigate("doctor_onboarding")
                },

                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("patient_onboarding") {
            PatientOnboardingScreen(
                viewModel = viewModel,

                onComplete = {
                    navController.navigate("home") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },

                onCancel = {
                    viewModel.clearPendingSignup()
                    navController.navigate("auth") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("doctor_onboarding") {
            DoctorOnboardingScreen(
                viewModel = viewModel,

                onComplete = {
                    navController.navigate("doctor_dashboard") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                },

                onCancel = {
                    viewModel.clearPendingSignup()
                    navController.navigate("auth") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // =====================================================
        // PATIENT ROUTES
        // =====================================================

        composable("home") {

            PatientHomeScreen(
                viewModel = viewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("records") {

            RecordsScreen(
                viewModel = viewModel
            )
        }

        composable("medicines") {

            MedicinesScreen(
                viewModel = viewModel
            )
        }

        composable("ai_copilot") {

            AICopilotScreen(
                viewModel = viewModel
            )
        }

        composable("profile") {

            ProfileScreen(
                viewModel = viewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("emergency") {

            EmergencyScreen(
                viewModel = viewModel
            )
        }

        composable("settings") {

            SettingsScreen(
                viewModel = viewModel,

                onNavigateTo = { route ->
                    navController.navigate(route)
                },

                onSignOut = {

                    viewModel.signOut()

                    navController.navigate("auth") {

                        popUpTo(
                            navController.graph.startDestinationId
                        ) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("appointments") {

            AppointmentsScreen(
                viewModel = viewModel
            )
        }

        composable("consent") {

            ConsentScreen(
                viewModel = viewModel
            )
        }

        // =====================================================
        // DOCTOR ROUTES
        // =====================================================

        composable("doctor_dashboard") {

            DoctorDashboardScreen(
                viewModel = viewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("doctor_patients") {

            DoctorPatientsScreen(
                viewModel = viewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("doctor_patient_workspace") {

            DoctorPatientWorkspaceScreen(
                viewModel = viewModel,
                onNavigateTo = { route ->
                    navController.navigate(route)
                }
            )
        }

        composable("doctor_clinical_ai") {

            DoctorClinicalAIScreen(
                viewModel = viewModel
            )
        }
    }
}