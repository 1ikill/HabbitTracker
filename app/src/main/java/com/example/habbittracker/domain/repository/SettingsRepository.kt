package com.example.habbittracker.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val isDarkMode: Flow<Boolean>
    val sortOrder: Flow<String>
    val notificationsEnabled: Flow<Boolean>
    val tutorialCompleted: Flow<Boolean>
    
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setSortOrder(order: String)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setTutorialCompleted(completed: Boolean)
}
