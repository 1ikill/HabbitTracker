package com.example.habbittracker.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.habbittracker.data.datastore.UserPreferences
import com.example.habbittracker.data.repository.AuthRepositoryImpl
import com.example.habbittracker.data.repository.HabitRepositoryImpl
import com.example.habbittracker.data.repository.SettingsRepositoryImpl
import com.example.habbittracker.domain.repository.AuthRepository
import com.example.habbittracker.domain.repository.HabitRepository
import com.example.habbittracker.domain.repository.SettingsRepository
import com.example.habbittracker.domain.usecase.AddHabit
import com.example.habbittracker.domain.usecase.DeleteHabit
import com.example.habbittracker.domain.usecase.GetHabits
import com.example.habbittracker.domain.usecase.GetUserSettings
import com.example.habbittracker.domain.usecase.HabitUseCases
import com.example.habbittracker.domain.usecase.UpdateHabit
import com.example.habbittracker.domain.usecase.UpdateUserSettings
import com.example.habbittracker.domain.usecase.auth.GetCurrentUserUseCase
import com.example.habbittracker.domain.usecase.auth.SignInUseCase
import com.example.habbittracker.domain.usecase.auth.SignOutUseCase
import com.example.habbittracker.domain.usecase.auth.SignUpUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataStore extension for Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideUserPreferences(dataStore: DataStore<Preferences>): UserPreferences {
        return UserPreferences(dataStore)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): AuthRepository {
        return AuthRepositoryImpl(firebaseAuth, firestore)
    }

    @Provides
    @Singleton
    fun provideHabitRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): HabitRepository {
        return HabitRepositoryImpl(firestore, authRepository)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        userPreferences: UserPreferences
    ): SettingsRepository {
        return SettingsRepositoryImpl(userPreferences)
    }

    @Provides
    @Singleton
    fun provideHabitUseCases(
        repository: HabitRepository
    ): HabitUseCases {
        return HabitUseCases(
            addHabit = AddHabit(repository),
            updateHabit = UpdateHabit(repository),
            getHabits = GetHabits(repository),
            deleteHabit = DeleteHabit(repository)
        )
    }

    @Provides
    @Singleton
    fun provideGetUserSettings(
        repository: SettingsRepository
    ): GetUserSettings {
        return GetUserSettings(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserSettings(
        repository: SettingsRepository
    ): UpdateUserSettings {
        return UpdateUserSettings(repository)
    }

    // Authentication Use Cases
    @Provides
    @Singleton
    fun provideSignUpUseCase(
        repository: AuthRepository
    ): SignUpUseCase {
        return SignUpUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSignInUseCase(
        repository: AuthRepository
    ): SignInUseCase {
        return SignInUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSignOutUseCase(
        repository: AuthRepository
    ): SignOutUseCase {
        return SignOutUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserUseCase(
        repository: AuthRepository
    ): GetCurrentUserUseCase {
        return GetCurrentUserUseCase(repository)
    }
}