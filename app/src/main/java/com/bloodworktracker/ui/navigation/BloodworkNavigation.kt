package com.bloodworktracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.bloodworktracker.ui.screens.dashboard.DashboardScreen
import com.bloodworktracker.ui.screens.tests.TestListScreen
import com.bloodworktracker.ui.screens.tests.AddTestScreen
import com.bloodworktracker.ui.screens.tests.TestDetailScreen
import com.bloodworktracker.ui.screens.values.ValueCategoriesScreen
import com.bloodworktracker.ui.screens.values.ValueDetailScreen
import com.bloodworktracker.ui.screens.analysis.AnalysisScreen
import com.bloodworktracker.ui.screens.settings.SettingsScreen

@Composable
fun BloodworkNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToTests = { navController.navigate(Screen.TestList.route) },
                onNavigateToValues = { navController.navigate(Screen.ValueCategories.route) },
                onNavigateToAddTest = { navController.navigate(Screen.AddTest.route) }
            )
        }
        
        composable(Screen.TestList.route) {
            TestListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTest = { testId -> 
                    navController.navigate("${Screen.TestDetail.route}/$testId") 
                },
                onNavigateToAddTest = { navController.navigate(Screen.AddTest.route) }
            )
        }
        
        composable(Screen.AddTest.route) {
            AddTestScreen(
                onNavigateBack = { navController.popBackStack() },
                onTestSaved = { 
                    navController.popBackStack(Screen.TestList.route, false)
                }
            )
        }
        
        composable(
            "${Screen.TestDetail.route}/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            TestDetailScreen(
                testId = testId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalysis = { 
                    navController.navigate("${Screen.Analysis.route}/$testId") 
                }
            )
        }
        
        composable(Screen.ValueCategories.route) {
            ValueCategoriesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToValue = { valueId -> 
                    navController.navigate("${Screen.ValueDetail.route}/$valueId") 
                }
            )
        }
        
        composable(
            "${Screen.ValueDetail.route}/{valueId}",
            arguments = listOf(navArgument("valueId") { type = NavType.LongType })
        ) { backStackEntry ->
            val valueId = backStackEntry.arguments?.getLong("valueId") ?: 0L
            ValueDetailScreen(
                valueId = valueId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            "${Screen.Analysis.route}/{testId}",
            arguments = listOf(navArgument("testId") { type = NavType.LongType })
        ) { backStackEntry ->
            val testId = backStackEntry.arguments?.getLong("testId") ?: 0L
            AnalysisScreen(
                testId = testId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object TestList : Screen("test_list")
    object AddTest : Screen("add_test")
    object TestDetail : Screen("test_detail")
    object ValueCategories : Screen("value_categories")
    object ValueDetail : Screen("value_detail")
    object Analysis : Screen("analysis")
    object Settings : Screen("settings")
}