package com.example.habbittracker.domain.usecase

import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.repository.HabitRepository
import javax.inject.Inject

class AddHabit @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit) {
        repository.addHabit(habit)
    }
}