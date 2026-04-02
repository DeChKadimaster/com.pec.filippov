package com.pec.filippov

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pec.filippov.ui.screens.LoginScreen
import com.pec.filippov.ui.screens.ProfileScreen
import com.pec.filippov.ui.theme.PECQRTheme
import com.pec.filippov.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PECQRTheme {
                val viewModel: StudentViewModel = viewModel()
                val navController = rememberNavController()
                
                val student by viewModel.student.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val error by viewModel.error.collectAsState()

                // Automatic navigation when logged in
                LaunchedEffect(student) {
                    if (student != null) {
                        navController.navigate("profile") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginClick = { code -> viewModel.login(code) },
                                isLoading = isLoading,
                                errorMessage = error
                            )
                        }
                        composable("profile") {
                            student?.let { currentStudent ->
                                ProfileScreen(
                                    student = currentStudent,
                                    onBack = { 
                                        viewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    },
                                    onChangeAvatar = {
                                        // TODO: Implement Avatar Picker
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}