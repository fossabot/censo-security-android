package com.strikeprotocols.mobile.di

import android.content.Context
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.UserRepositoryImpl
import com.strikeprotocols.mobile.service.MessagingService
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
    fun provideAuthProvider(@ApplicationContext applicationContext: Context,
                            encryptionManager: EncryptionManager,
                            securePreferences: SecurePreferences): AuthProvider {
        return OktaAuth(applicationContext, encryptionManager, securePreferences)
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
    fun provideUserRepository(
        authProvider: AuthProvider,
        api: BrooklynApiService,
        encryptionManager: EncryptionManager,
        securePreferences: SecurePreferences,
        phraseValidator: PhraseValidator
    ): UserRepository {
        return UserRepositoryImpl(
            authProvider = authProvider,
            api = api,
            encryptionManager = encryptionManager,
            securePreferences = securePreferences,
            phraseValidator = phraseValidator
        )
    }

    @Provides
    @Singleton
    fun provideApprovalsRepository(api: BrooklynApiService, encryptionManager: EncryptionManager, userRepository: UserRepository): ApprovalsRepository {
        return ApprovalsRepositoryImpl(api, encryptionManager, userRepository)
    }

    @Provides
    @Singleton
    fun providePushRepository(api: BrooklynApiService, @ApplicationContext applicationContext: Context): PushRepository {
        return PushRepositoryImpl(api, applicationContext)
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(securePreferences: SecurePreferences): EncryptionManager {
        return EncryptionManagerImpl(securePreferences)
    }

    @Provides
    @Singleton
    fun provideSecurePrefs(): SecurePreferences {
        return SecurePreferencesImpl()
    }

    @Provides
    @Singleton
    fun providePhraseValidator() : PhraseValidator {
        return PhraseValidatorImpl()
    }

    @Provides
    @Singleton
    fun provideStrikeUserData(): StrikeUserData {
        return StrikeUserDataImpl
    }

}