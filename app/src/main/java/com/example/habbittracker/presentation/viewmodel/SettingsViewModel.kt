package com.example.habbittracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbittracker.domain.usecase.GetUserSettings
import com.example.habbittracker.domain.usecase.UpdateUserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserSettings: GetUserSettings,
    private val updateUserSettings: UpdateUserSettings
) : ViewModel() {
    
    // Expose settings as StateFlow for UI
    val isDarkMode = getUserSettings.isDarkMode
    val sortOrder = getUserSettings.sortOrder
    val notificationsEnabled = getUserSettings.notificationsEnabled
    val tutorialCompleted = getUserSettings.tutorialCompleted
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    companion object {
        private const val TAG = "SettingsViewModel"
    }
    
    fun toggleDarkMode() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentMode = isDarkMode.first()
                updateUserSettings.setDarkMode(!currentMode)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling dark mode: ${e.message}")
            }
            _isLoading.value = false
        }
    }
    
    fun setSortOrder(order: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                updateUserSettings.setSortOrder(order)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting sort order: ${e.message}")
            }
            _isLoading.value = false
        }
    }
    
    fun toggleNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSetting = notificationsEnabled.first()
                updateUserSettings.setNotificationsEnabled(!currentSetting)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling notifications: ${e.message}")
            }
            _isLoading.value = false
        }
    }
    
    fun completeTutorial() {
        viewModelScope.launch {
            try {
                updateUserSettings.setTutorialCompleted(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error completing tutorial: ${e.message}")
            }
        }
    }
    
    fun resetTutorial() {
        viewModelScope.launch {
            try {
                updateUserSettings.setTutorialCompleted(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting tutorial: ${e.message}")
            }
        }
    }
}
