package com.bloodworktracker.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bloodworktracker.ui.navigation.BloodworkNavigation
import com.bloodworktracker.ui.navigation.Screen
import com.bloodworktracker.ui.theme.BloodworkTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BloodworkTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BloodworkApp()
                }
            }
        }
    }
}

@Composable
fun BloodworkApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Define bottom navigation items
    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.Dashboard.route,
            icon = Icons.Default.Home,
            label = "Dashboard"
        ),
        BottomNavItem(
            route = Screen.TestList.route,
            icon = Icons.Default.List,
            label = "Tests"
        ),
        BottomNavItem(
            route = Screen.ValueCategories.route,
            icon = Icons.Default.Favorite,
            label = "Blutwerte"
        ),
        BottomNavItem(
            route = Screen.Settings.route,
            icon = Icons.Default.Settings,
            label = "Einstellungen"
        )
    )
    
    // Determine if bottom navigation should be shown
    val shouldShowBottomBar = currentDestination?.route in bottomNavItems.map { it.route }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        BloodworkNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Preview(showBackground = true)
@Composable
fun BloodworkAppPreview() {
    BloodworkTrackerTheme {
        BloodworkApp()
    }
}