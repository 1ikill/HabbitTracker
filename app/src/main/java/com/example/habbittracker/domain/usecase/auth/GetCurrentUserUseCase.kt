package com.example.habbittracker.domain.usecase.auth

import com.example.habbittracker.domain.model.User
import com.example.habbittracker.domain.repository.AuthRepository
import com.example.habbittracker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<User?>> {
        return authRepository.currentUser.map { user ->
            Resource.Success(user)
        }
    }
    
    fun getCurrentUserSync(): User? {
        return authRepository.getCurrentUser()
    }
}
