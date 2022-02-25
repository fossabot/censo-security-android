package com.strikeprotocols.mobile.di

import android.content.Context
import com.strikeprotocols.mobile.data.*
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
    fun provideUserRepository(authProvider: AuthProvider, api: BrooklynApiService): UserRepository {
        return UserRepositoryImpl(authProvider, api)
    }

    @Provides
    @Singleton
    fun provideApprovalsRepository(api: BrooklynApiService): ApprovalsRepository {
        return ApprovalsRepositoryImpl(api)
    }
}