package com.strikeprotocols.mobile.data

import com.google.gson.*
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.AUTH
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.AUTH_REQUIRED
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.X_STRIKE_HEADER
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.X_STRIKE_REQUIRED
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.ApprovalDispositionRequest
import com.strikeprotocols.mobile.data.models.approval.InitiationRequest
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.data.models.approval.WalletApprovalDeserializer
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
        const val X_STRIKE_HEADER = "X-Strike-Authorization-Signature"
        const val X_STRIKE_REQUIRED = "$X_STRIKE_HEADER: "

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

            val customGson = GsonBuilder()
                .registerTypeAdapter(WalletApproval::class.java, WalletApprovalDeserializer())
                .create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(customGson))
                .build()
                .create(BrooklynApiService::class.java)
        }
    }

    @GET("v1/users")
    @Headers(AUTH_REQUIRED)
    suspend fun verifyUser(): VerifyUser

    @GET("v1/wallet-signers")
    @Headers(AUTH_REQUIRED)
    suspend fun walletSigners(): List<WalletSigner?>

    @POST("v1/wallet-signers")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun addWalletSigner(@Body walletSignerBody: WalletSigner): WalletSigner

    @GET("v1/wallet-approvals")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun getWalletApprovals(): List<WalletApproval?>

    @POST("v1/notification-tokens")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun addPushNotificationToken(@Body pushData: PushBody): PushBody

    @DELETE("v1/notification-tokens/{deviceId}/{deviceType}")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun removePushNotificationToken(
        @Path("deviceId") deviceId: String,
        @Path("deviceType") deviceType: String
    )

    @POST("v1/wallet-approvals/{request_id}/dispositions")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun approveOrDenyDisposition(
        @Path("request_id") requestId: String,
        @Body registerApprovalDispositionBody: ApprovalDispositionRequest.RegisterApprovalDispositionBody
    ): ApprovalDispositionRequest.RegisterApprovalDispositionBody

    @POST("v1/wallet-approvals/{request_id}/initiations")
    @Headers(AUTH_REQUIRED, X_STRIKE_REQUIRED)
    suspend fun approveOrDenyInitiation(
        @Path("request_id") requestId: String,
        @Body initiationRequestBody: InitiationRequest.InitiateRequestBody
    ) : InitiationRequest.InitiateRequestBody

}

class AuthInterceptor(private val authProvider: AuthProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request().newBuilder().build()

        if (request.header(AUTH) != null) {
            try {
                val token = runBlocking {
                    authProvider.retrieveToken()
                }
                request = if (request.header(X_STRIKE_HEADER) != null) {
                    val signedToken = runBlocking {
                        authProvider.signToken(token)
                    }

                    chain.request().newBuilder()
                        .removeHeader(X_STRIKE_HEADER)
                        .removeHeader(AUTH)
                        .addHeader(AUTH, "Bearer $token")
                        .addHeader(X_STRIKE_HEADER, signedToken)
                        .build()
                } else {
                    chain.request().newBuilder()
                        .removeHeader(AUTH)
                        .addHeader(AUTH, "Bearer $token")
                        .build()
                }
            } catch (e: TokenExpiredException) {
                runBlocking { authProvider.signOut() }
                authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
            }
        }
        return chain.proceed(request)
    }
}
