package com.example.habbittracker.domain.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object StreakRecovery {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    data class StreakStatus(
        val isAtRisk: Boolean,
        val daysSinceLastCompletion: Long,
        val message: String,
        val severity: RecoverySeverity
    )
    
    enum class RecoverySeverity {
        NONE,      // No action needed
        GENTLE,    // Gentle reminder
        MODERATE,  // Moderate encouragement
        URGENT     // Urgent recovery needed
    }
    
    fun getStreakStatus(habit: com.example.habbittracker.domain.model.Habit): StreakStatus {
        val today = LocalDate.now()
        val lastCompletedDate = if (habit.lastCompletedDate.isNotEmpty()) {
            LocalDate.parse(habit.lastCompletedDate, dateFormatter)
        } else {
            return StreakStatus(
                isAtRisk = false,
                daysSinceLastCompletion = 0,
                message = "Start your streak today!",
                severity = RecoverySeverity.GENTLE
            )
        }
        
        val daysSinceLastCompletion = ChronoUnit.DAYS.between(lastCompletedDate, today)
        val isCompletedToday = StreakCalculator.isCompletedToday(habit.completionDates)
        
        return when {
            isCompletedToday -> StreakStatus(
                isAtRisk = false,
                daysSinceLastCompletion = 0,
                message = "Great job! Keep the streak alive!",
                severity = RecoverySeverity.NONE
            )
            
            daysSinceLastCompletion == 0L -> StreakStatus(
                isAtRisk = false,
                daysSinceLastCompletion = 0,
                message = "Don't forget to complete your habit today!",
                severity = RecoverySeverity.GENTLE
            )
            
            daysSinceLastCompletion == 1L -> StreakStatus(
                isAtRisk = true,
                daysSinceLastCompletion = daysSinceLastCompletion,
                message = "Your streak is at risk! Complete your habit to keep it going",
                severity = RecoverySeverity.MODERATE
            )
            
            daysSinceLastCompletion == 2L -> StreakStatus(
                isAtRisk = true,
                daysSinceLastCompletion = daysSinceLastCompletion,
                message = "Don't let your ${habit.currentStreak}-day streak end! Get back on track today",
                severity = RecoverySeverity.URGENT
            )
            
            else -> StreakStatus(
                isAtRisk = true,
                daysSinceLastCompletion = daysSinceLastCompletion,
                message = "Time for a fresh start! Begin a new streak today",
                severity = RecoverySeverity.GENTLE
            )
        }
    }
    
    fun getMotivationalMessage(habit: com.example.habbittracker.domain.model.Habit): String {
        return when {
            habit.currentStreak >= 30 -> "Amazing! You're a habit master!"
            habit.currentStreak >= 21 -> "Incredible! You've built a strong habit!"
            habit.currentStreak >= 14 -> "Two weeks strong! Keep going!"
            habit.currentStreak >= 7 -> "One week streak! You're on fire!"
            habit.currentStreak >= 3 -> "Great start! Building momentum!"
            habit.currentStreak >= 1 -> "Good job! Every day counts!"
            else -> "Ready to start your streak?"
        }
    }
    
    fun getRecoveryTips(habit: com.example.habbittracker.domain.model.Habit): List<String> {
        return listOf(
            "Tip: Start small - even 5 minutes counts!",
            "Tip: Set a consistent time for your habit",
            "🔔 Use reminders to stay on track",
            "🎯 Focus on progress, not perfection",
            "🤝 Share your goal with a friend for accountability",
            "📅 Plan ahead for busy days",
            "🎉 Celebrate small wins along the way!"
        )
    }
    
    fun shouldShowRecoveryPrompt(habit: com.example.habbittracker.domain.model.Habit): Boolean {
        val status = getStreakStatus(habit)
        return status.isAtRisk && status.daysSinceLastCompletion <= 2
    }
}
