package com.example.habbittracker.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.repository.AuthRepository
import com.example.habbittracker.domain.usecase.GetUserSettings
import com.example.habbittracker.domain.usecase.HabitUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class HabitViewModel @Inject constructor(
    private val useCases: HabitUseCases,
    private val getUserSettings: GetUserSettings,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _habits = MutableStateFlow<List<Habit>>(emptyList())
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    companion object {
        private const val TAG = "HabitViewModel"
    }

    init {
        // Load habits only when user is authenticated
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                if (user != null) {
                    Log.d(TAG, "User authenticated, loading habits for: ${user.email}")
                    loadHabits()
                } else {
                    Log.d(TAG, "User not authenticated, clearing habits")
                    _habits.value = emptyList()
                    habitsJob?.cancel()
                }
            }
        }
    }

    private var habitsJob: Job? = null

    fun loadHabits() {
        // Cancel previous job if exists
        habitsJob?.cancel()
        
        // Only load habits if user is authenticated
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            Log.w(TAG, "Cannot load habits - user not authenticated")
            _habits.value = emptyList()
            _isLoading.value = false
            return
        }
        
        Log.d(TAG, "Loading habits for user: ${currentUser.email}")
        
        habitsJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Combine habits data with sort preferences
                combine(
                    useCases.getHabits(),
                    getUserSettings.sortOrder
                ) { habitsList, sortOrder ->
                    val sortedList = when (sortOrder) {
                        "oldest_first" -> habitsList.sortedBy { it.timestamp }
                        "alphabetical" -> habitsList.sortedBy { it.title.lowercase() }
                        else -> habitsList.sortedByDescending { it.timestamp } // newest_first (default)
                    }
                    
                    // Separate pinned and unpinned habits, maintaining sort order within each group
                    val pinnedHabits = sortedList.filter { it.pinned }
                    val unpinnedHabits = sortedList.filter { !it.pinned }
                    
                    // Return pinned habits first, then unpinned habits
                    pinnedHabits + unpinnedHabits
                }.collect { sortedHabits ->
                    _habits.value = sortedHabits
                    _isLoading.value = false
                    Log.d(TAG, "Loaded ${sortedHabits.size} habits for user ${currentUser.email}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading habits: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Unexpected error"
                _isLoading.value = false
            }
        }
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Received habit for processing")
                Log.d(TAG, "ID='${habit.id}', Title='${habit.title}', Description='${habit.description}', Timestamp=${habit.timestamp}")
                
                if (habit.id.isEmpty()) {
                    Log.w(TAG, "Habit ID is empty! Creating new UUID...")
                    habit.id = java.util.UUID.randomUUID().toString()
                    Log.d(TAG, "Generated new ID: ${habit.id}")
                }
                
                Log.d(TAG, "Adding habit ${habit.title} with ID: ${habit.id}")
                useCases.addHabit(habit)
                Log.d(TAG, "Habit added successfully")
                // No need to call loadHabits() - the Flow will automatically update
            } catch (e: Exception) {
                Log.e(TAG, "Error adding habit: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Error adding habit"
            }
            _isLoading.value = false
        }
    }

    fun deleteHabit(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                useCases.deleteHabit(habitId)
                // No need to call loadHabits() - the Flow will automatically update
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Error deleting habit"
            }
            _isLoading.value = false
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "Updating habit ${habit.title} with ID: ${habit.id}")
                useCases.updateHabit(habit)
                Log.d(TAG, "Habit updated successfully")
                // No need to call loadHabits() - the Flow will automatically update
            } catch (e: Exception) {
                Log.e(TAG, "Error updating habit: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Error updating habit"
            }
            _isLoading.value = false
        }
    }

    fun togglePinHabit(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentHabits = _habits.value
                val habitToPin = currentHabits.find { it.id == habitId }
                
                if (habitToPin != null) {
                    val pinnedCount = currentHabits.count { it.pinned }
                    
                    // Check if trying to pin more than 5 habits
                    if (!habitToPin.pinned && pinnedCount >= 5) {
                        _errorMessage.value = "You can only pin up to 5 habits"
                        _isLoading.value = false
                        return@launch
                    }
                    
                    val updatedHabit = habitToPin.copy(pinned = !habitToPin.pinned)
                    Log.d(TAG, "Toggling pin for habit ${updatedHabit.title} to ${updatedHabit.pinned}")
                    useCases.updateHabit(updatedHabit)
                    Log.d(TAG, "Habit pin status updated successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling pin: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Error updating habit pin status"
            }
            _isLoading.value = false
        }
    }

    fun toggleHabitCompletion(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentHabits = _habits.value
                val habit = currentHabits.find { it.id == habitId }
                
                if (habit != null) {
                    val today = com.example.habbittracker.domain.util.StreakCalculator.getTodayDateString()
                    val isCompletedToday = com.example.habbittracker.domain.util.StreakCalculator.isCompletedToday(habit.completionDates)
                    
                    val updatedCompletionDates = if (isCompletedToday) {
                        // Remove today's completion
                        com.example.habbittracker.domain.util.StreakCalculator.removeTodayCompletion(habit.completionDates)
                    } else {
                        // Add today's completion
                        com.example.habbittracker.domain.util.StreakCalculator.addTodayCompletion(habit.completionDates)
                    }
                    
                    // Calculate new streaks
                    val newCurrentStreak = com.example.habbittracker.domain.util.StreakCalculator.calculateCurrentStreak(updatedCompletionDates)
                    val newBestStreak = maxOf(habit.bestStreak, com.example.habbittracker.domain.util.StreakCalculator.calculateBestStreak(updatedCompletionDates))
                    
                    val updatedHabit = habit.copy(
                        completionDates = updatedCompletionDates,
                        currentStreak = newCurrentStreak,
                        bestStreak = newBestStreak,
                        lastCompletedDate = if (!isCompletedToday) today else habit.lastCompletedDate
                    )
                    
                    Log.d(TAG, "Toggling completion for habit ${updatedHabit.title}")
                    Log.d(TAG, "Current streak: ${newCurrentStreak}, Best streak: ${newBestStreak}")
                    useCases.updateHabit(updatedHabit)
                    Log.d(TAG, "Habit completion updated successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling completion: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Error updating habit completion"
            }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        habitsJob?.cancel()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Test function to add sample completion dates for calendar testing
    fun addTestCompletionDates(habitId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentHabits = _habits.value
                val habit = currentHabits.find { it.id == habitId }
                
                if (habit != null) {
                    // Generate test completion dates for the past 2 weeks with some gaps
                    val today = java.time.LocalDate.now()
                    val testDates = mutableListOf<String>()
                    
                    // Add some completed days (simulating a pattern with some gaps)
                    val daysToComplete = listOf(0, 1, 2, 4, 5, 7, 8, 10, 12, 14, 16, 18, 20) // Skip some days to show gaps
                    
                    daysToComplete.forEach { daysBack ->
                        val date = today.minusDays(daysBack.toLong())
                        testDates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                    
                    // Calculate streaks based on test data
                    val newCurrentStreak = com.example.habbittracker.domain.util.StreakCalculator.calculateCurrentStreak(testDates)
                    val newBestStreak = com.example.habbittracker.domain.util.StreakCalculator.calculateBestStreak(testDates)
                    
                    val updatedHabit = habit.copy(
                        completionDates = testDates.sorted(),
                        currentStreak = newCurrentStreak,
                        bestStreak = maxOf(habit.bestStreak, newBestStreak),
                        lastCompletedDate = testDates.maxOrNull() ?: ""
                    )
                    
                    Log.d(TAG, "Adding test completion dates for habit ${updatedHabit.title}")
                    Log.d(TAG, "Test dates: ${testDates.sorted()}")
                    Log.d(TAG, "Current streak: ${newCurrentStreak}, Best streak: ${newBestStreak}")
                    useCases.updateHabit(updatedHabit)
                    Log.d(TAG, "Test completion dates added successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding test completion dates: ${e.message}")
                _errorMessage.value = e.localizedMessage ?: "Error adding test completion dates"
            }
            _isLoading.value = false
        }
    }
}