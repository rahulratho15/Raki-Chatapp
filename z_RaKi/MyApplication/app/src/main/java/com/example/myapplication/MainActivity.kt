package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import ui.chat.ChatScreen
import ui.personal.ChatDetailScreen
import ui.login.LoginScreen
import ui.signup.SignupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for Google Play Services
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            // Show dialog if Google Play Services is unavailable
            googleApiAvailability.getErrorDialog(this, resultCode, 0)?.show()
        } else {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)

            // Set up the Compose content
            setContent {
                MyApplicationTheme {
                    AppScaffold()
                }
            }
        }
    }
}

@Composable
fun AppScaffold() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun AppNavigation(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = "login", // Set initial screen to "login"
        modifier = modifier.fillMaxSize()
    ) {
        // Login screen route
        composable("login") {
            LoginScreen(navController = navController)
        }

        // Signup screen route
        composable("signup") {
            SignupScreen(navController = navController)
        }

        // Chat screen route
        composable("chat") {
            ChatScreen(navController = navController)
        }

        // Chat detail screen route with a parameter for user ID
        composable("chat_detail/{chatUserId}") { backStackEntry ->
            val chatUserId = backStackEntry.arguments?.getString("chatUserId") ?: ""
            ChatDetailScreen(chatUserId = chatUserId)
        }
    }
}
