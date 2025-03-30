package com.mvvmexample.firebasecomposemvvmclean.presentation.ui.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mvvmexample.firebasecomposemvvmclean.presentation.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AuthViewModel = hiltViewModel()

    val startDestination = if (viewModel.isUserAuthenticated()) {
        "profile"
    } else {
        "auth"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            AuthScreen(
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }
    }
}