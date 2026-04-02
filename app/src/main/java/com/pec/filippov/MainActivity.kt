package com.pec.filippov

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import com.pec.filippov.ui.screens.InfoScreen
import com.pec.filippov.ui.theme.PECQRTheme
import com.pec.filippov.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // ГЛОБАЛЬНЫЙ ПЕРЕХВАТЧИК ОШИБОК ДЛЯ ДИАГНОСТИКИ
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Looper.prepare()
                Toast.makeText(
                    this,
                    "КРАШ: ${throwable.localizedMessage}\n${Log.getStackTraceString(throwable).take(200)}...",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("CRASH_DIAG", "Fatal error", throwable)
                Thread.sleep(3000)
            } catch (e: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        setContent {
            PECQRTheme {
                val viewModel: StudentViewModel = viewModel()
                val navController = rememberNavController()
                
                val student by viewModel.student.collectAsState()
                val isLoading by viewModel.isLoading.collectAsState()
                val error by viewModel.error.collectAsState()

                // Automatic navigation when logged in (Fixed loop)
                LaunchedEffect(student) {
                    if (student != null && navController.currentDestination?.route != "profile") {
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
                                    viewModel = viewModel,
                                    onBack = { 
                                        viewModel.logout()
                                        navController.navigate("login") {
                                            popUpTo("profile") { inclusive = true }
                                        }
                                    },
                                    onLearnMoreClick = {
                                        navController.navigate("info")
                                    }
                                )
                            }
                        }
                        composable("info") {
                            student?.let { currentStudent ->
                                InfoScreen(
                                    student = currentStudent,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}