package com.strikeprotocols.mobile.di

import android.content.Context
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.common.StrikeCountDownTimerImpl
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.UserRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthProvider(
        encryptionManager: EncryptionManager,
        securePreferences: SecurePreferences
    ): AuthProvider {
        return StrikeAuth(encryptionManager, securePreferences)
    }

    @Provides
    @Singleton
    fun provideApiService(authProvider: AuthProvider): BrooklynApiService {
        return BrooklynApiService.create(authProvider)
    }

    @Provides
    @Singleton
    fun provideSolanaService(): SolanaApiService {
        return SolanaApiService.create()
    }

    @Provides
    @Singleton
    fun provideSemVersionService(): SemVersionApiService {
        return SemVersionApiService.create()
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        authProvider: AuthProvider,
        api: BrooklynApiService,
        encryptionManager: EncryptionManager,
        securePreferences: SecurePreferences,
        phraseValidator: PhraseValidator,
        semVersionApiService: SemVersionApiService,
        @ApplicationContext applicationContext: Context
    ): UserRepository {
        return UserRepositoryImpl(
            authProvider = authProvider,
            api = api,
            encryptionManager = encryptionManager,
            securePreferences = securePreferences,
            versionApiService = semVersionApiService,
            phraseValidator = phraseValidator,
            applicationContext = applicationContext
        )
    }

    @Provides
    @Singleton
    fun provideApprovalsRepository(
        api: BrooklynApiService,
        encryptionManager: EncryptionManager,
        userRepository: UserRepository
    ): ApprovalsRepository {
        return ApprovalsRepositoryImpl(api, encryptionManager, userRepository)
    }

    @Provides
    @Singleton
    fun providePushRepository(
        api: BrooklynApiService,
        @ApplicationContext applicationContext: Context
    ): PushRepository {
        return PushRepositoryImpl(api, applicationContext)
    }

    @Provides
    @Singleton
    fun provideSolanaRepository(api: SolanaApiService): SolanaRepository {
        return SolanaRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(securePreferences: SecurePreferences): EncryptionManager {
        return EncryptionManagerImpl(securePreferences)
    }

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext applicationContext: Context): SecurePreferences {
        return SecurePreferencesImpl(applicationContext)
    }

    @Provides
    @Singleton
    fun providePhraseValidator(): PhraseValidator {
        return PhraseValidatorImpl()
    }

    @Provides
    @Singleton
    fun provideStrikeUserData(userRepository: UserRepository): StrikeUserData {
        return StrikeUserDataImpl(userRepository)
    }

    @Provides
    fun provideStrikeCountDownTimer(): StrikeCountDownTimer {
        return StrikeCountDownTimerImpl()
    }

}