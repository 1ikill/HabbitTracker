package com.example.habbittracker.domain.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", false, 0L)
}
