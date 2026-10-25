package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.TempTrackerRepository
import com.example.ui.screens.DailyLogScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.theme.TempTrackerTheme
import com.example.ui.viewmodel.TempTrackerViewModel
import com.example.ui.viewmodel.TempTrackerViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TempTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

enum class Screen {
    DAILY_LOG,
    HISTORY
}

@Composable
fun AppNavigation() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        SignInScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        val uid = currentUser!!.uid
        MainFeatureContainer(
            userId = uid,
            onSignOut = {
                currentUser = null
            }
        )
    }
}

@Composable
fun MainFeatureContainer(
    userId: String,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context, userId) {
        TempTrackerRepository(context)
    }

    val viewModel: TempTrackerViewModel = viewModel(
        factory = TempTrackerViewModelFactory(repository, userId)
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allHistoryLogs by viewModel.allHistoryLogs.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(Screen.DAILY_LOG) }

    when (currentScreen) {
        Screen.DAILY_LOG -> {
            DailyLogScreen(
                viewModel = viewModel,
                uiState = uiState,
                allHistoryLogs = allHistoryLogs,
                onNavigateToHistory = {
                    currentScreen = Screen.HISTORY
                },
                onSignOut = {
                    performSignOut(context, scope, onSignOut)
                }
            )
        }
        Screen.HISTORY -> {
            HistoryScreen(
                logs = allHistoryLogs,
                onSelectDate = { selectedDate ->
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    try {
                        val parsed = sdf.parse(selectedDate)
                        if (parsed != null) {
                            viewModel.setDate(parsed)
                        }
                    } catch (_: Exception) {}
                    currentScreen = Screen.DAILY_LOG
                },
                onBack = {
                    currentScreen = Screen.DAILY_LOG
                }
            )
        }
    }
}

private fun performSignOut(
    context: Context,
    scope: CoroutineScope,
    onComplete: () -> Unit
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
        onComplete()
    }
}
