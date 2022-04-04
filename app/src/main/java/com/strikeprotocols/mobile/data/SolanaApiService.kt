package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.BuildConfig

import com.strikeprotocols.mobile.data.models.RecentBlockHashResponse
import com.strikeprotocols.mobile.presentation.blockhash.BlockHashViewModel.RecentBlockHashBody
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface SolanaApiService {

    companion object {

        private const val TIMEOUT_LENGTH_SECONDS = 60L

        fun create(): SolanaApiService {

            val client = OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.SOLANA_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SolanaApiService::class.java)
        }
    }

    @POST("/")
    suspend fun recentBlockhash(@Body recentBlockHashBody: RecentBlockHashBody): RecentBlockHashResponse
}