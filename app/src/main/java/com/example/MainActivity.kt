package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.LauncherViewModel
import com.example.ui.NexaViewLauncherApp

class MainActivity : ComponentActivity() {

    private val launcherViewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexaViewLauncherApp(viewModel = launcherViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        launcherViewModel.connectivityObserver.refreshStatus()
    }
}
