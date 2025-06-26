package com.example.habbittracker.domain.usecase

import com.example.habbittracker.domain.repository.HabitRepository
import javax.inject.Inject

class DeleteHabit @Inject constructor(
    private val repository: HabitRepository
) {
    suspend operator fun invoke(habitId: String) {
        repository.deleteHabit(habitId)
    }
}