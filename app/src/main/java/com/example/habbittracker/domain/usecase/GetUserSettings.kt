package com.example.habbittracker.domain.usecase

import com.example.habbittracker.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserSettings @Inject constructor(
    private val repository: SettingsRepository
) {
    val isDarkMode: Flow<Boolean> = repository.isDarkMode
    val sortOrder: Flow<String> = repository.sortOrder
    val notificationsEnabled: Flow<Boolean> = repository.notificationsEnabled
    val tutorialCompleted: Flow<Boolean> = repository.tutorialCompleted
}
