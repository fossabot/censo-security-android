package com.censocustody.android.di

import android.content.Context
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.CensoCountDownTimerImpl
import com.censocustody.android.data.*
import com.censocustody.android.data.UserRepository
import com.censocustody.android.data.UserRepositoryImpl
import com.censocustody.android.data.models.CipherRepository
import com.censocustody.android.data.models.CipherRepositoryImpl
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
        return CensoAuth(encryptionManager, securePreferences)
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
        anchorApiService: AnchorApiService,
        securePreferences: SecurePreferences,
        semVersionApiService: SemVersionApiService,
        @ApplicationContext applicationContext: Context
    ): UserRepository {
        return UserRepositoryImpl(
            authProvider = authProvider,
            api = api,
            anchorApiService = anchorApiService,
            securePreferences = securePreferences,
            versionApiService = semVersionApiService,
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
    fun provideKeyRepository(encryptionManager: EncryptionManager, securePreferences: SecurePreferences, userRepository: UserRepository): KeyRepository {
        return KeyRepositoryImpl(encryptionManager = encryptionManager, securePreferences = securePreferences, userRepository =  userRepository)
    }

    @Provides
    @Singleton
    fun provideCipherRepository(encryptionManager: EncryptionManager, securePreferences: SecurePreferences, userRepository: UserRepository): CipherRepository {
        return CipherRepositoryImpl(encryptionManager = encryptionManager, securePreferences = securePreferences, userRepository =  userRepository)
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(securePreferences: SecurePreferences, cryptographyManager: CryptographyManager): EncryptionManager {
        return EncryptionManagerImpl(securePreferences, cryptographyManager)
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
    fun provideUserData(userRepository: UserRepository): CensoUserData {
        return CensoUserDataImpl(userRepository)
    }

    @Provides
    fun provideCountDownTimer(): CensoCountDownTimer {
        return CensoCountDownTimerImpl()
    }

    @Singleton
    @Provides
    fun provideCryptographyManager(): CryptographyManager {
        return CryptographyManagerImpl()
    }

    @Provides
    @Singleton
    fun provideMigrationRepository(
        encryptionManager: EncryptionManager,
        securePreferences: SecurePreferences,
        userRepository: UserRepository,
        api: BrooklynApiService
    ): MigrationRepository {
        return MigrationRepositoryImpl(
            encryptionManager = encryptionManager,
            securePreferences = securePreferences,
            userRepository = userRepository,
            api = api
        )
    }

    @Provides
    @Singleton
    fun provideAnchorService(): AnchorApiService {
        return AnchorApiService.create()
    }

}