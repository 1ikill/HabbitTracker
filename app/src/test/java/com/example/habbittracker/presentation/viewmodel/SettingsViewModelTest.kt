package com.example.habbittracker.presentation.viewmodel

import android.util.Log
import com.example.habbittracker.domain.usecase.GetUserSettings
import com.example.habbittracker.domain.usecase.UpdateUserSettings
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockGetUserSettings: GetUserSettings
    private lateinit var mockUpdateUserSettings: UpdateUserSettings
    private lateinit var settingsViewModel: SettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        
        mockGetUserSettings = mockk()
        mockUpdateUserSettings = mockk()
        
        // Setup default mock behaviors
        every { mockGetUserSettings.isDarkMode } returns flowOf(false)
        every { mockGetUserSettings.sortOrder } returns flowOf("newest_first")
        every { mockGetUserSettings.notificationsEnabled } returns flowOf(true)
        every { mockGetUserSettings.tutorialCompleted } returns flowOf(false)
        
        coEvery { mockUpdateUserSettings.setDarkMode(any()) } just Runs
        coEvery { mockUpdateUserSettings.setSortOrder(any()) } just Runs
        coEvery { mockUpdateUserSettings.setNotificationsEnabled(any()) } just Runs
        coEvery { mockUpdateUserSettings.setTutorialCompleted(any()) } just Runs
        
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    @Test
    fun `isDarkMode flow returns correct initial value`() = runTest {
        // Given - dark mode is initially false (set in setup)
        
        // When
        val result = settingsViewModel.isDarkMode.first()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `sortOrder flow returns correct initial value`() = runTest {
        // Given - sort order is initially "newest_first" (set in setup)
        
        // When
        val result = settingsViewModel.sortOrder.first()
        
        // Then
        assertEquals("newest_first", result)
    }

    @Test
    fun `notificationsEnabled flow returns correct initial value`() = runTest {
        // Given - notifications are initially enabled (set in setup)
        
        // When
        val result = settingsViewModel.notificationsEnabled.first()
        
        // Then
        assertTrue(result)
    }

    @Test
    fun `tutorialCompleted flow returns correct initial value`() = runTest {
        // Given - tutorial is initially not completed (set in setup)
        
        // When
        val result = settingsViewModel.tutorialCompleted.first()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `isLoading starts as false`() = runTest {
        // When
        val result = settingsViewModel.isLoading.first()
        
        // Then
        assertFalse(result)
    }

    @Test
    fun `toggleDarkMode calls updateUserSettings with correct value when currently false`() = runTest {
        // Given - dark mode is currently false
        every { mockGetUserSettings.isDarkMode } returns flowOf(false)
        
        // When
        settingsViewModel.toggleDarkMode()
        
        // Then
        coVerify { mockUpdateUserSettings.setDarkMode(true) }
    }

    @Test
    fun `toggleDarkMode calls updateUserSettings with correct value when currently true`() = runTest {
        // Given - dark mode is currently true
        every { mockGetUserSettings.isDarkMode } returns flowOf(true)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleDarkMode()
        
        // Then
        coVerify { mockUpdateUserSettings.setDarkMode(false) }
    }

    @Test
    fun `setSortOrder calls updateUserSettings with correct value`() = runTest {
        // Given
        val newSortOrder = "alphabetical"
        
        // When
        settingsViewModel.setSortOrder(newSortOrder)
        
        // Then
        coVerify { mockUpdateUserSettings.setSortOrder(newSortOrder) }
    }

    @Test
    fun `toggleNotifications calls updateUserSettings with correct value when currently true`() = runTest {
        // Given - notifications are currently enabled
        every { mockGetUserSettings.notificationsEnabled } returns flowOf(true)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleNotifications()
        
        // Then
        coVerify { mockUpdateUserSettings.setNotificationsEnabled(false) }
    }

    @Test
    fun `toggleNotifications calls updateUserSettings with correct value when currently false`() = runTest {
        // Given - notifications are currently disabled
        every { mockGetUserSettings.notificationsEnabled } returns flowOf(false)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleNotifications()
        
        // Then
        coVerify { mockUpdateUserSettings.setNotificationsEnabled(true) }
    }

    @Test
    fun `completeTutorial calls updateUserSettings with true`() = runTest {
        // When
        settingsViewModel.completeTutorial()
        
        // Then
        coVerify { mockUpdateUserSettings.setTutorialCompleted(true) }
    }

    @Test
    fun `resetTutorial calls updateUserSettings with false`() = runTest {
        // When
        settingsViewModel.resetTutorial()
        
        // Then
        coVerify { mockUpdateUserSettings.setTutorialCompleted(false) }
    }

    @Test
    fun `toggleDarkMode sets loading state correctly`() = runTest {
        // Given - setup mock to allow checking loading state
        every { mockGetUserSettings.isDarkMode } returns flowOf(false)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleDarkMode()
        
        // Then - loading should be false after completion
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `setSortOrder sets loading state correctly`() = runTest {
        // When
        settingsViewModel.setSortOrder("alphabetical")
        
        // Then - loading should be false after completion
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `toggleNotifications sets loading state correctly`() = runTest {
        // Given
        every { mockGetUserSettings.notificationsEnabled } returns flowOf(true)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleNotifications()
        
        // Then - loading should be false after completion
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `toggleDarkMode handles exception gracefully`() = runTest {
        // Given - setup mock to throw exception
        every { mockGetUserSettings.isDarkMode } returns flowOf(false)
        coEvery { mockUpdateUserSettings.setDarkMode(any()) } throws RuntimeException("Test exception")
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleDarkMode()
        
        // Then - should not crash and loading should be false
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `setSortOrder handles exception gracefully`() = runTest {
        // Given - setup mock to throw exception
        coEvery { mockUpdateUserSettings.setSortOrder(any()) } throws RuntimeException("Test exception")
        
        // When
        settingsViewModel.setSortOrder("alphabetical")
        
        // Then - should not crash and loading should be false
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `toggleNotifications handles exception gracefully`() = runTest {
        // Given - setup mock to throw exception
        every { mockGetUserSettings.notificationsEnabled } returns flowOf(true)
        coEvery { mockUpdateUserSettings.setNotificationsEnabled(any()) } throws RuntimeException("Test exception")
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When
        settingsViewModel.toggleNotifications()
        
        // Then - should not crash and loading should be false
        assertFalse(settingsViewModel.isLoading.value)
    }

    @Test
    fun `completeTutorial handles exception gracefully`() = runTest {
        // Given - setup mock to throw exception
        coEvery { mockUpdateUserSettings.setTutorialCompleted(any()) } throws RuntimeException("Test exception")
        
        // When
        settingsViewModel.completeTutorial()
        
        // Then - should not crash (no loading state for this method)
        // Just verify it doesn't throw
    }

    @Test
    fun `resetTutorial handles exception gracefully`() = runTest {
        // Given - setup mock to throw exception
        coEvery { mockUpdateUserSettings.setTutorialCompleted(any()) } throws RuntimeException("Test exception")
        
        // When
        settingsViewModel.resetTutorial()
        
        // Then - should not crash (no loading state for this method)
        // Just verify it doesn't throw
    }

    @Test
    fun `multiple calls to toggleDarkMode work correctly`() = runTest {
        // Given - dark mode starts false
        every { mockGetUserSettings.isDarkMode } returns flowOf(false)
        settingsViewModel = SettingsViewModel(mockGetUserSettings, mockUpdateUserSettings)
        
        // When - call toggle multiple times
        settingsViewModel.toggleDarkMode()
        settingsViewModel.toggleDarkMode()
        
        // Then - should be called twice with true (since flow always returns false in this test)
        coVerify(exactly = 2) { mockUpdateUserSettings.setDarkMode(true) }
    }

    @Test
    fun `different sort orders are handled correctly`() = runTest {
        // When - test different sort orders
        settingsViewModel.setSortOrder("oldest_first")
        settingsViewModel.setSortOrder("alphabetical")
        settingsViewModel.setSortOrder("newest_first")
        
        // Then - all calls should be made with correct values
        coVerify { mockUpdateUserSettings.setSortOrder("oldest_first") }
        coVerify { mockUpdateUserSettings.setSortOrder("alphabetical") }
        coVerify { mockUpdateUserSettings.setSortOrder("newest_first") }
    }
}
