package com.example.habbittracker.domain.usecase

import javax.inject.Inject

data class HabitUseCases @Inject constructor(
    val addHabit: AddHabit,
    val updateHabit: UpdateHabit,
    val getHabits: GetHabits,
    val deleteHabit: DeleteHabit
)