package com.example.diary.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diary.ui.screens.EditDiaryScreen
import com.example.diary.ui.screens.HomeScreen
import com.example.diary.ui.screens.ViewDiaryScreen
import com.example.diary.viewmodel.DiaryViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Edit : Screen("edit/{entryId}") {
        fun createRoute(entryId: Long = -1L) = "edit/$entryId"
    }
    data object View : Screen("view/{entryId}") {
        fun createRoute(entryId: Long) = "view/$entryId"
    }
}

@Composable
fun AppNavigation(navController: NavHostController, viewModel: DiaryViewModel = viewModel()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onEntryClick = { entry ->
                    navController.navigate(Screen.View.createRoute(entry.id))
                },
                onAddClick = {
                    viewModel.startNewEntry()
                    navController.navigate(Screen.Edit.createRoute())
                }
            )
        }
        composable(
            route = Screen.Edit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
            EditDiaryScreen(
                viewModel = viewModel,
                entryId = if (entryId > 0) entryId else null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.View.route,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
            ViewDiaryScreen(
                viewModel = viewModel,
                entryId = entryId,
                onBack = { navController.popBackStack() },
                onEdit = {
                    viewModel.startEditEntry(entryId)
                    navController.navigate(Screen.Edit.createRoute(entryId))
                }
            )
        }
    }
}
