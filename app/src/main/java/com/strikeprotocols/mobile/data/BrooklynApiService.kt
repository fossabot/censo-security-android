package com.strikeprotocols.mobile.data

import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.AUTH
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers
import java.util.concurrent.TimeUnit


interface BrooklynApiService {

    companion object {

        const val AUTH = "Authorization"
        const val AUTH_REQUIRED = "$AUTH: "

        private const val TIMEOUT_LENGTH_SECONDS = 60L

        fun create(authProvider: AuthProvider): BrooklynApiService {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authProvider))
                .connectTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT_LENGTH_SECONDS, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BrooklynApiService::class.java)
        }
    }

    //example non auth API call
    @POST("/nonauth")
    suspend fun registerUser(): ResponseBody

    //example auth api call
    @GET("/approvals")
    @Headers(AUTH_REQUIRED)
    suspend fun getApprovals(): ResponseBody
}

class AuthInterceptor(private val authProvider: AuthProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request().newBuilder().build()

        if (request.header(AUTH) != null) {
            try {
                val token = runBlocking { authProvider.retrieveToken() }
                request = chain.request().newBuilder()
                    .removeHeader(AUTH)
                    .addHeader(AUTH, "Bearer $token")
                    .build()
            } catch (e: TokenExpiredException) {
                runBlocking {  authProvider.signOut() }
                authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
            }
        }
        return chain.proceed(request)
    }
}
