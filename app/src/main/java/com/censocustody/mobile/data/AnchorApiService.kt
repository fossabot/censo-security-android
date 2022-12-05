package com.censocustody.mobile.data

import com.censocustody.mobile.BuildConfig
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface AnchorApiService {

    companion object {

        private const val TIMEOUT_LENGTH_SECONDS = 60L

        fun create(): AnchorApiService {

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
                .baseUrl(BuildConfig.ANCHOR_URL)
                .client(client.build())
                .build()
                .create(AnchorApiService::class.java)
        }
    }

    @POST("email/{userEmail}/password-reset")
    suspend fun recoverPassword(@Path("userEmail") userEmail: String): ResponseBody
}