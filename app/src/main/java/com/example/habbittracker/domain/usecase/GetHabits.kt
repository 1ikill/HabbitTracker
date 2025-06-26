package com.example.habbittracker.domain.usecase

import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHabits @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(): Flow<List<Habit>> {
        return repository.getHabits()
    }
}