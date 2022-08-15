package com.strikeprotocols.mobile.data

import com.google.gson.GsonBuilder
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.models.SemanticVersionResponse
import okhttp3.OkHttpClient
import retrofit2.Response as RetrofitResponse
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface SemVersionApiService {

    companion object {

        fun create(): SemVersionApiService {

            val clientBuilder = OkHttpClient.Builder()

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                clientBuilder.addInterceptor(logger)
            }

            val gson = GsonBuilder()
                .setLenient()
                .create()

            return Retrofit.Builder()
                .baseUrl("https://strike-public-assets.s3.amazonaws.com/")
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(SemVersionApiService::class.java)
        }
    }

    @GET(BuildConfig.MIN_VERSION_ENDPOINT)
    suspend fun getMinimumVersion(): RetrofitResponse<SemanticVersionResponse>
}