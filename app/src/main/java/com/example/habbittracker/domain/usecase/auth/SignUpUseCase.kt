package com.example.habbittracker.domain.usecase.auth

import com.example.habbittracker.domain.model.User
import com.example.habbittracker.domain.repository.AuthRepository
import com.example.habbittracker.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(email: String, password: String, displayName: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())
            val result = authRepository.signUp(email, password, displayName)
            if (result.isSuccess) {
                emit(Resource.Success(result.getOrThrow()))
            } else {
                emit(Resource.Error(result.exceptionOrNull()?.message ?: "Sign up failed"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }
}
