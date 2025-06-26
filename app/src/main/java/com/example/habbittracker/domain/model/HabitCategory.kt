package com.example.habbittracker.domain.model

data class HabitCategory(
    val name: String,
    val icon: String,
    val color: String
)

object HabitCategories {
    val HEALTH = HabitCategory("Health", "🏥", "#4CAF50")
    val FITNESS = HabitCategory("Fitness", "💪", "#FF5722")
    val LEARNING = HabitCategory("Learning", "📚", "#2196F3")
    val PRODUCTIVITY = HabitCategory("Productivity", "⚡", "#FF9800")
    val MINDFULNESS = HabitCategory("Mindfulness", "🧘", "#9C27B0")
    val NUTRITION = HabitCategory("Nutrition", "🥗", "#8BC34A")
    val SOCIAL = HabitCategory("Social", "👥", "#E91E63")
    val CREATIVITY = HabitCategory("Creativity", "🎨", "#673AB7")
    val FINANCE = HabitCategory("Finance", "💰", "#FFC107")
    val GENERAL = HabitCategory("General", "📋", "#607D8B")
    
    val ALL_CATEGORIES = listOf(
        HEALTH, FITNESS, LEARNING, PRODUCTIVITY, MINDFULNESS,
        NUTRITION, SOCIAL, CREATIVITY, FINANCE, GENERAL
    )
    
    fun getCategoryByName(name: String): HabitCategory {
        return ALL_CATEGORIES.find { it.name == name } ?: GENERAL
    }
    
    fun getCategoryNames(): List<String> {
        return ALL_CATEGORIES.map { it.name }
    }
    
    fun getCategoryIcons(): List<String> {
        return ALL_CATEGORIES.map { it.icon }
    }
}
