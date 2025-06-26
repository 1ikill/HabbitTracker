package com.example.habbittracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habbittracker.presentation.screen.AddHabitScreen
import com.example.habbittracker.presentation.screen.EditHabitScreen
import com.example.habbittracker.presentation.screen.HabitCalendarScreen
import com.example.habbittracker.presentation.screen.HabitListScreen
import com.example.habbittracker.presentation.screen.SettingsScreen
import com.example.habbittracker.presentation.screen.SignInScreen
import com.example.habbittracker.presentation.screen.SignUpScreen
import com.example.habbittracker.presentation.screen.TutorialScreen
import com.example.habbittracker.presentation.viewmodel.AuthViewModel
import com.example.habbittracker.presentation.viewmodel.HabitViewModel
import com.example.habbittracker.presentation.viewmodel.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val habitViewModel: HabitViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val tutorialCompleted by settingsViewModel.tutorialCompleted.collectAsState(initial = false)
    val authState by authViewModel.authState.collectAsState()
    
    // Determine start destination based on auth state and tutorial completion
    val startDestination = when {
        !authState.isSignedIn -> "sign_in"
        !tutorialCompleted -> "tutorial"
        else -> "habit_list"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        // Authentication screens
        composable("sign_in") {
            SignInScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("sign_up") {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }
        
        // App screens (require authentication)
        composable("tutorial") {
            TutorialScreen(
                settingsViewModel = settingsViewModel,
                navController = navController
            )
        }
        composable("habit_list") {
            HabitListScreen(viewModel = habitViewModel, navController = navController)
        }
        composable("add_habit") {
            AddHabitScreen(viewModel = habitViewModel, navController = navController)
        }
        composable(
            route = "edit_habit/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            EditHabitScreen(
                habitId = habitId,
                viewModel = habitViewModel, 
                navController = navController
            )
        }
        composable(
            route = "habit_calendar/{habitId}",
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HabitCalendarScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}