package com.censocustody.android.di

import android.content.Context
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.CensoCountDownTimerImpl
import com.censocustody.android.data.*
import com.censocustody.android.data.api.AnchorApiService
import com.censocustody.android.data.api.BrooklynApiService
import com.censocustody.android.data.api.SemVersionApiService
import com.censocustody.android.data.cryptography.CryptographyManager
import com.censocustody.android.data.cryptography.CryptographyManagerImpl
import com.censocustody.android.data.cryptography.EncryptionManager
import com.censocustody.android.data.cryptography.EncryptionManagerImpl
import com.censocustody.android.data.repository.*
import com.censocustody.android.data.storage.*
import com.censocustody.android.data.validator.AndroidEmailValidator
import com.censocustody.android.data.validator.EmailValidator
import com.censocustody.android.data.validator.PhraseValidator
import com.censocustody.android.data.validator.PhraseValidatorImpl
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
        encryptionManager: EncryptionManager,
        cryptographyManager: CryptographyManager,
        @ApplicationContext applicationContext: Context
    ): UserRepository {
        return UserRepositoryImpl(
            authProvider = authProvider,
            api = api,
            anchorApiService = anchorApiService,
            securePreferences = securePreferences,
            versionApiService = semVersionApiService,
            applicationContext = applicationContext,
            cryptographyManager = cryptographyManager,
            encryptionManager = encryptionManager
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
    fun provideKeyRepository(
        encryptionManager: EncryptionManager,
        securePreferences: SecurePreferences,
        cryptographyManager: CryptographyManager,
        userRepository: UserRepository,
        brooklynApiService: BrooklynApiService,
        keyStorage: KeyStorage
    ): KeyRepository {
        return KeyRepositoryImpl(
            encryptionManager = encryptionManager,
            cryptographyManager = cryptographyManager,
            securePreferences = securePreferences,
            userRepository = userRepository,
            brooklynApiService = brooklynApiService,
            keyStorage = keyStorage
        )
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(
        cryptographyManager: CryptographyManager,
        keyStorage: KeyStorage
    ): EncryptionManager {
        return EncryptionManagerImpl(
            cryptographyManager = cryptographyManager,
            keyStorage = keyStorage
        )
    }

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext applicationContext: Context): SecurePreferences {
        return SecurePreferencesImpl(applicationContext)
    }

    @Provides
    @Singleton
    fun provideKeyStorage(
        cryptographyManager: CryptographyManager,
        securePreferences: SecurePreferences,
    ): KeyStorage {
        return KeyStorageImpl(
            securePreferences = securePreferences,
            cryptographyManager = cryptographyManager,
        )
    }

    @Provides
    @Singleton
    fun providePhraseValidator(): PhraseValidator {
        return PhraseValidatorImpl()
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
    fun provideAnchorService(): AnchorApiService {
        return AnchorApiService.create()
    }

    @Provides
    @Singleton
    fun provideEmailValidator() : EmailValidator {
        return AndroidEmailValidator()
    }

}