package com.runtracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.runtracker.ui.screens.auth.AuthScreen
import com.runtracker.ui.screens.auth.AuthViewModel
import com.runtracker.ui.screens.dashboard.DashboardScreen
import com.runtracker.ui.screens.logrun.LogRunScreen
import com.runtracker.ui.screens.motivation.MotivationScreen
import com.runtracker.ui.screens.myruns.MyRunsScreen
import com.runtracker.ui.screens.routes.RoutesScreen
import com.runtracker.ui.theme.*

private sealed class Tab(val route: String, val icon: ImageVector, val label: String) {
    object Dashboard : Tab("dashboard", Icons.Default.Dashboard,    "Dashboard")
    object LogRun    : Tab("log_run",   Icons.Default.AddCircle,    "Log Run")
    object MyRuns    : Tab("my_runs",   Icons.Default.List,         "My Runs")
    object Routes    : Tab("routes",    Icons.Default.Map,          "Routes")
    object Motivation: Tab("motivation",Icons.Default.Star,         "Motivate")
}

private val tabs = listOf(Tab.Dashboard, Tab.LogRun, Tab.MyRuns, Tab.Routes, Tab.Motivation)

@Composable
fun RunTrackerNavGraph() {
    val authVm: AuthViewModel = hiltViewModel()
    val isLoggedIn by authVm.isLoggedIn.collectAsStateWithLifecycle()
    val rootNav = rememberNavController()

    // React to auth state changes
    LaunchedEffect(isLoggedIn) {
        when (isLoggedIn) {
            true  -> rootNav.navigate("main")  { popUpTo(0) { inclusive = true } }
            false -> rootNav.navigate("auth")  { popUpTo(0) { inclusive = true } }
            null  -> Unit
        }
    }

    NavHost(navController = rootNav, startDestination = "loading") {
        composable("loading") {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.then(androidx.compose.ui.Modifier.fillMaxSize()),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) { CircularProgressIndicator(color = GreenPrimary) }
        }
        composable("auth") {
            AuthScreen(viewModel = authVm)
        }
        composable("main") {
            MainScreen(authVm = authVm)
        }
    }
}

@Composable
private fun MainScreen(authVm: AuthViewModel) {
    val bottomNav = rememberNavController()
    val backStack by bottomNav.currentBackStackEntryAsState()
    val current = backStack?.destination

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(containerColor = DarkSurface, tonalElevation = 0.dp) {
                tabs.forEach { tab ->
                    val selected = current?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            bottomNav.navigate(tab.route) {
                                popUpTo(bottomNav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = GreenPrimary,
                            selectedTextColor   = GreenPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor      = GreenDim
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNav,
            startDestination = Tab.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Tab.Dashboard.route)  { DashboardScreen(authVm = authVm) }
            composable(Tab.LogRun.route)     { LogRunScreen(onSaved = { bottomNav.navigate(Tab.MyRuns.route) }) }
            composable(Tab.MyRuns.route)     { MyRunsScreen() }
            composable(Tab.Routes.route)     { RoutesScreen() }
            composable(Tab.Motivation.route) { MotivationScreen() }
        }
    }
}
