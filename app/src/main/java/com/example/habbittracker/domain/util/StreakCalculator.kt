package com.example.habbittracker.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakCalculator {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    fun calculateCurrentStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0
        
        val today = LocalDate.now()
        val sortedDates = completionDates.sorted().map { LocalDate.parse(it, dateFormatter) }
        
        // Check if completed today or yesterday (to allow for different timezones)
        val lastCompletionDate = sortedDates.lastOrNull()
        if (lastCompletionDate == null) return 0
        
        val daysSinceLastCompletion = today.toEpochDay() - lastCompletionDate.toEpochDay()
        if (daysSinceLastCompletion > 1) return 0 // Streak broken
        
        // Count consecutive days working backwards
        var streak = 0
        var currentDate = if (daysSinceLastCompletion == 0L) today else today.minusDays(1)
        
        for (i in sortedDates.indices.reversed()) {
            if (sortedDates[i] == currentDate) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else if (sortedDates[i] < currentDate) {
                // Gap found, streak broken
                break
            }
        }
        
        return streak
    }
    
    fun calculateBestStreak(completionDates: List<String>): Int {
        if (completionDates.isEmpty()) return 0
        
        val sortedDates = completionDates.sorted().map { LocalDate.parse(it, dateFormatter) }
        var maxStreak = 0
        var currentStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val daysDiff = sortedDates[i].toEpochDay() - sortedDates[i - 1].toEpochDay()
            if (daysDiff == 1L) {
                // Consecutive day
                currentStreak++
            } else {
                // Gap found, reset streak
                maxStreak = maxOf(maxStreak, currentStreak)
                currentStreak = 1
            }
        }
        
        return maxOf(maxStreak, currentStreak)
    }
    
    fun isCompletedToday(completionDates: List<String>): Boolean {
        val today = LocalDate.now().format(dateFormatter)
        return completionDates.contains(today)
    }
    
    fun getTodayDateString(): String {
        return LocalDate.now().format(dateFormatter)
    }
    
    fun addTodayCompletion(completionDates: List<String>): List<String> {
        val today = getTodayDateString()
        return if (completionDates.contains(today)) {
            completionDates
        } else {
            completionDates + today
        }
    }
    
    fun removeTodayCompletion(completionDates: List<String>): List<String> {
        val today = getTodayDateString()
        return completionDates.filter { it != today }
    }
}
