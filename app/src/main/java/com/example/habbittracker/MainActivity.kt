package com.example.habbittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.habbittracker.presentation.navigation.NavGraph
import com.example.habbittracker.presentation.viewmodel.SettingsViewModel
import com.example.habbittracker.ui.theme.HabitTrackerTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            
            HabitTrackerTheme(darkTheme = isDarkMode) {
                NavGraph()
            }
        }
    }
}