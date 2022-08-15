package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.BuildConfig
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*
import retrofit2.Response as RetrofitResponse
import java.util.concurrent.TimeUnit


interface AnchorApiService {

    companion object {

        fun create(): AnchorApiService {

            val client = OkHttpClient.Builder()

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.ANCHOR_URL)
                .client(client.build())
                .build()
                .create(AnchorApiService::class.java)
        }
    }

    @POST("email/{userEmail}/reset")
    suspend fun recoverPassword(@Path("userEmail") userEmail: String): RetrofitResponse<Unit>
}