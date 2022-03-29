package com.strikeprotocols.mobile.di

import android.content.Context
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
    fun provideAuthProvider(@ApplicationContext applicationContext: Context): AuthProvider {
        return OktaAuth(applicationContext)
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
        securePreferences: SecurePreferences
    ): UserRepository {
        return UserRepositoryImpl(authProvider, api, encryptionManager, securePreferences)
    }

    @Provides
    @Singleton
    fun provideApprovalsRepository(api: BrooklynApiService, solanaApiService: SolanaApiService): ApprovalsRepository {
        return ApprovalsRepositoryImpl(api, solanaApiService)
    }

    @Provides
    @Singleton
    fun providePushRepository(api: BrooklynApiService): PushRepository {
        return PushRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideEncryptionManager(): EncryptionManager {
        return EncryptionManagerImpl()
    }

    @Provides
    @Singleton
    fun provideSecurePrefs(@ApplicationContext applicationContext: Context): SecurePreferences {
        return StrikeEncryptedSharedPreferences(applicationContext)
    }
}