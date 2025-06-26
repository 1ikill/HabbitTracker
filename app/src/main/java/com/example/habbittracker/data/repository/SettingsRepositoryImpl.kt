package com.example.habbittracker.data.repository

import com.example.habbittracker.data.datastore.UserPreferences
import com.example.habbittracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : SettingsRepository {
    
    override val isDarkMode: Flow<Boolean> = userPreferences.isDarkMode
    override val sortOrder: Flow<String> = userPreferences.sortOrder
    override val notificationsEnabled: Flow<Boolean> = userPreferences.notificationsEnabled
    override val tutorialCompleted: Flow<Boolean> = userPreferences.tutorialCompleted
    
    override suspend fun setDarkMode(enabled: Boolean) {
        userPreferences.setDarkMode(enabled)
    }
    
    override suspend fun setSortOrder(order: String) {
        userPreferences.setSortOrder(order)
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        userPreferences.setNotificationsEnabled(enabled)
    }
    
    override suspend fun setTutorialCompleted(completed: Boolean) {
        userPreferences.setTutorialCompleted(completed)
    }
}
