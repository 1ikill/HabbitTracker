package com.example.habbittracker.data.repository

import android.util.Log
import com.example.habbittracker.domain.model.Habit
import com.example.habbittracker.domain.repository.AuthRepository
import com.example.habbittracker.domain.repository.HabitRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HabitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) : HabitRepository {

    companion object {
        private const val TAG = "HabitRepository"
    }

    private fun getUserHabitsCollection() = 
        authRepository.getCurrentUser()?.uid?.let { uid ->
            Log.d(TAG, "Using habits collection for user: $uid")
            firestore.collection("users").document(uid).collection("habits")
        } ?: throw IllegalStateException("User not authenticated - cannot access habits")

    override suspend fun addHabit(habit: Habit) {
        try {
            val currentUser = authRepository.getCurrentUser()
                ?: throw IllegalStateException("User not authenticated - cannot add habit")
            
            Log.d(TAG, "Received habit for saving for user: ${currentUser.email}")
            Log.d(TAG, "ID='${habit.id}', Title='${habit.title}', Description='${habit.description}', Timestamp=${habit.timestamp}")
            
            if (habit.id.isEmpty()) {
                throw IllegalArgumentException("Habit ID cannot be empty")
            }
            
            val userHabitsCollection = getUserHabitsCollection()
            Log.d(TAG, "Attempting to save to user's document: ${habit.id}")
            userHabitsCollection.document(habit.id).set(habit).await()
            Log.d(TAG, "Habit saved successfully for user ${currentUser.email}!")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving habit: ${e.message}")
            throw e
        }
    }

    override suspend fun getHabits(): Flow<List<Habit>> = callbackFlow {
        try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                Log.w(TAG, "No authenticated user, returning empty habits list")
                trySend(emptyList())
                close()
                return@callbackFlow
            }
            
            Log.d(TAG, "Setting up habits listener for user: ${currentUser.email} (${currentUser.uid})")
            val userHabitsCollection = getUserHabitsCollection()
            val listener = userHabitsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to user habits: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }

                    val habits = snapshot?.toObjects(Habit::class.java) ?: emptyList()
                    Log.d(TAG, "Received ${habits.size} habits from user's Firestore collection")
                    trySend(habits)
                }

            awaitClose { 
                Log.d(TAG, "Removing user habits listener for ${currentUser.email}")
                listener.remove() 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up user habits listener: ${e.message}")
            trySend(emptyList())
            close(e)
        }
    }

    override suspend fun updateHabit(habit: Habit) {
        try {
            val currentUser = authRepository.getCurrentUser()
                ?: throw IllegalStateException("User not authenticated - cannot update habit")
            
            Log.d(TAG, "Updating habit for user: ${currentUser.email}")
            Log.d(TAG, "ID='${habit.id}', Title='${habit.title}', Description='${habit.description}', Category='${habit.category}', Icon='${habit.icon}'")
            
            if (habit.id.isEmpty()) {
                throw IllegalArgumentException("Habit ID cannot be empty for update")
            }
            
            val userHabitsCollection = getUserHabitsCollection()
            Log.d(TAG, "Attempting to update user's document: ${habit.id}")
            userHabitsCollection.document(habit.id).set(habit).await()
            Log.d(TAG, "Habit updated successfully for user ${currentUser.email}!")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating habit: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteHabit(habitId: String) {
        try {
            val currentUser = authRepository.getCurrentUser()
                ?: throw IllegalStateException("User not authenticated - cannot delete habit")
            
            Log.d(TAG, "Deleting habit $habitId for user: ${currentUser.email}")
            val userHabitsCollection = getUserHabitsCollection()
            userHabitsCollection.document(habitId).delete().await()
            Log.d(TAG, "Habit deleted successfully for user ${currentUser.email}!")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting habit: ${e.message}")
            throw e
        }
    }
}