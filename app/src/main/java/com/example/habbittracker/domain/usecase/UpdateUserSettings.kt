package com.example.habbittracker.domain.usecase

import com.example.habbittracker.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateUserSettings @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend fun setDarkMode(enabled: Boolean) {
        repository.setDarkMode(enabled)
    }
    
    suspend fun setSortOrder(order: String) {
        repository.setSortOrder(order)
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }
    
    suspend fun setTutorialCompleted(completed: Boolean) {
        repository.setTutorialCompleted(completed)
    }
}
