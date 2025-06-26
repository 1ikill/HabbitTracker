package com.example.habbittracker.domain.repository

import com.example.habbittracker.domain.model.Habit
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    suspend fun addHabit(habit: Habit)
    suspend fun updateHabit(habit: Habit)
    suspend fun getHabits(): Flow<List<Habit>>
    suspend fun deleteHabit(habitId: String)
}