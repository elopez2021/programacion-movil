package com.example.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.login.data.ApiClient
import com.example.login.data.User
import com.example.login.ui.HomeScreen
import com.example.login.ui.Language
import com.example.login.ui.LoginScreen
import com.example.login.ui.theme.LoginTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LoginTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentUser by remember { mutableStateOf<User?>(null) }
                    var currentLanguage by remember { mutableStateOf(Language.ES) }

                    Crossfade(
                        targetState = currentUser,
                        label = "AuthTransition"
                    ) { user ->
                        if (user == null) {
                            LoginScreen(
                                currentLanguage = currentLanguage,
                                onLanguageChange = { newLang ->
                                    currentLanguage = newLang
                                },
                                onLoginSuccess = { loggedInUser ->
                                    currentUser = loggedInUser
                                },
                                apiClient = ApiClient
                            )
                        } else {
                            HomeScreen(
                                user = user,
                                currentLanguage = currentLanguage,
                                onLanguageChange = { newLang ->
                                    currentLanguage = newLang
                                },
                                onUserUpdated = { updatedUser ->
                                    currentUser = updatedUser
                                },
                                onSignOut = {
                                    currentUser = null
                                },
                                apiClient = ApiClient
                            )
                        }
                    }
                }
            }
        }
    }
}
