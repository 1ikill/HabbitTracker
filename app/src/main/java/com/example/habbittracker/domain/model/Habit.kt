package com.example.habbittracker.domain.model

data class Habit(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var category: String = "General",
    var icon: String = "General",
    var pinned: Boolean = false,
    var timestamp: Long = System.currentTimeMillis(),
    var completionDates: List<String> = emptyList(), // Format: "YYYY-MM-DD"
    var currentStreak: Int = 0,
    var bestStreak: Int = 0,
    var lastCompletedDate: String = "" // Format: "YYYY-MM-DD"
) {
    // No-argument constructor required by Firestore
    constructor() : this("", "", "", "General", "General", false, 0L, emptyList(), 0, 0, "")
}