package com.strikeprotocols.mobile.data

import com.google.gson.*
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.BaseRepository.Companion.UNAUTHORIZED
import com.strikeprotocols.mobile.data.BrooklynApiService.Companion.AUTH
import com.strikeprotocols.mobile.data.models.*
import com.strikeprotocols.mobile.data.models.approval.*
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response as RetrofitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers

interface BrooklynApiService {

    companion object {

        const val AUTH = "Authorization"
        const val AUTH_REQUIRED = "$AUTH: "

        fun create(authProvider: AuthProvider): BrooklynApiService {

            val client = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(authProvider))

            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            val customGson = GsonBuilder()
                .registerTypeAdapter(WalletApproval::class.java, WalletApprovalDeserializer())
                .registerTypeAdapterFactory(TypeFactorySettings.approvalSignatureAdapterFactory)
                .create()

            return Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(customGson))
                .build()
                .create(BrooklynApiService::class.java)
        }
    }

    @POST("v1/login")
    suspend fun login(@Body loginBody: LoginBody) : RetrofitResponse<LoginResponse>

    @GET("v1/users")
    @Headers(AUTH_REQUIRED)
    suspend fun verifyUser(): RetrofitResponse<VerifyUser>

    @GET("v1/wallet-signers")
    @Headers(AUTH_REQUIRED)
    suspend fun walletSigners(): RetrofitResponse<List<WalletSigner?>>

    @POST("v1/wallet-signers")
    @Headers(AUTH_REQUIRED)
    suspend fun addWalletSigner(@Body walletSignerBody: WalletSigner): RetrofitResponse<WalletSigner>

    @GET("v1/wallet-approvals")
    @Headers(AUTH_REQUIRED)
    suspend fun getWalletApprovals(): RetrofitResponse<List<WalletApproval?>>

    @POST("v1/notification-tokens")
    @Headers(AUTH_REQUIRED)
    suspend fun addPushNotificationToken(@Body pushData: PushBody): RetrofitResponse<PushBody>

    @DELETE("v1/notification-tokens/{deviceId}/{deviceType}")
    @Headers(AUTH_REQUIRED)
    suspend fun removePushNotificationToken(
        @Path("deviceId") deviceId: String,
        @Path("deviceType") deviceType: String
    ) : RetrofitResponse<Unit>

    @POST("v1/wallet-approvals/{request_id}/dispositions")
    @Headers(AUTH_REQUIRED)
    suspend fun approveOrDenyDisposition(
        @Path("request_id") requestId: String,
        @Body registerApprovalDispositionBody: ApprovalDispositionRequest.RegisterApprovalDispositionBody
    ): RetrofitResponse<ApprovalDispositionRequest.RegisterApprovalDispositionBody>

    @POST("v1/wallet-approvals/{request_id}/initiations")
    @Headers(AUTH_REQUIRED)
    suspend fun approveOrDenyInitiation(
        @Path("request_id") requestId: String,
        @Body initiationRequestBody: InitiationRequest.InitiateRequestBody
    ) : RetrofitResponse<InitiationRequest.InitiateRequestBody>

}

class AuthInterceptor(private val authProvider: AuthProvider) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request().newBuilder().build()

        val authRequired = request.header(AUTH) != null

        if (authRequired) {
            try {
                val token = runBlocking {
                    authProvider.retrieveToken()
                }

                if (token.isEmpty()) {
                    throw TokenExpiredException()
                }

                request =
                    chain.request().newBuilder()
                        .removeHeader(AUTH)
                        .addHeader(AUTH, "Bearer $token")
                        .build()

            } catch (e: TokenExpiredException) {
                runBlocking { authProvider.signOut() }
                authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
            }
        }
        val response = chain.proceed(request)

        if (authRequired && response.code == UNAUTHORIZED) {
            runBlocking { authProvider.signOut() }
            authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
        }

        return response
    }
}
