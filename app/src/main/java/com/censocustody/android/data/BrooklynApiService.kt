package com.censocustody.android.data

import android.os.Build
import com.google.gson.*
import com.censocustody.android.BuildConfig
import com.censocustody.android.data.BaseRepository.Companion.UNAUTHORIZED
import com.censocustody.android.data.BrooklynApiService.Companion.APP_VERSION_HEADER
import com.censocustody.android.data.BrooklynApiService.Companion.AUTH
import com.censocustody.android.data.BrooklynApiService.Companion.DEVICE_TYPE_HEADER
import com.censocustody.android.data.BrooklynApiService.Companion.OS_VERSION_HEADER
import com.censocustody.android.data.BrooklynApiService.Companion.X_CENSO_ID
import com.censocustody.android.data.models.*
import com.censocustody.android.data.models.approvalV2.ApprovalDispositionRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2Deserializer
import com.censocustody.android.data.models.approvalV2.ApprovalSignature.Companion.approvalSignatureAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.Response as RetrofitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.http.Headers
import java.time.Duration

interface BrooklynApiService {

    companion object {

        const val AUTH = "Authorization"
        const val AUTH_REQUIRED = "$AUTH: "
        const val X_CENSO_ID = "X-Censo-Device-Identifier"

        const val DEVICE_TYPE_HEADER = "X-Censo-Device-Type"
        const val APP_VERSION_HEADER = "X-Censo-App-Version"
        const val OS_VERSION_HEADER = "X-Censo-OS-Version"

        fun create(authProvider: AuthProvider): BrooklynApiService {
            val client = OkHttpClient.Builder()
                .addInterceptor(AnalyticsInterceptor())
                .addInterceptor(AuthInterceptor(authProvider))
                .connectTimeout(Duration.ofSeconds(30))
                .readTimeout(Duration.ofSeconds(30))
                .callTimeout(Duration.ofSeconds(30))
                .writeTimeout(Duration.ofSeconds(30))


            if (BuildConfig.DEBUG) {
                val logger =
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
                client.addInterceptor(logger)
            }

            val customGson = GsonBuilder()
                .registerTypeAdapter(ApprovalRequestV2::class.java, ApprovalRequestV2Deserializer())
                .registerTypeAdapterFactory(approvalSignatureAdapterFactory)
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

    @GET("v1/user-devices")
    @Headers(AUTH_REQUIRED)
    suspend fun userDevices(): RetrofitResponse<List<UserDevice?>>

    @POST("v1/user-devices")
    @Headers(AUTH_REQUIRED)
    suspend fun addUserDevice(@Body userDevice: UserDevice): RetrofitResponse<Unit>

    @POST("v1/bootstrap-user-devices")
    @Headers(AUTH_REQUIRED)
    suspend fun addBootstrapUserDeviceAndSigners(@Body userDeviceAndSigners: BootstrapUserDeviceAndSigners): RetrofitResponse<Unit>

    @GET("v1/wallet-signers")
    @Headers(AUTH_REQUIRED)
    suspend fun walletSigners(): RetrofitResponse<List<WalletSigner?>>

    @POST("v3/wallet-signers")
    @Headers(AUTH_REQUIRED)
    suspend fun addWalletSigner(@Body signers: Signers): RetrofitResponse<Unit>

    @GET("v2/approval-requests")
    @Headers(AUTH_REQUIRED)
    suspend fun getApprovalRequests(): RetrofitResponse<List<ApprovalRequestV2?>>

    @POST("v1/notification-tokens")
    @Headers(AUTH_REQUIRED)
    suspend fun addPushNotificationToken(@Body pushData: PushBody): RetrofitResponse<PushBody>

    @DELETE("v1/notification-tokens/{deviceId}/{deviceType}")
    @Headers(AUTH_REQUIRED)
    suspend fun removePushNotificationToken(
        @Path("deviceId") deviceId: String,
        @Path("deviceType") deviceType: String
    ) : RetrofitResponse<Unit>

    @POST("v2/approval-requests/{request_id}/dispositions")
    @Headers(AUTH_REQUIRED)
    suspend fun approveOrDenyDisposition(
        @Path("request_id") requestId: String,
        @Body registerApprovalDispositionBody: ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body
    ): RetrofitResponse<ApprovalDispositionRequestV2.RegisterApprovalDispositionV2Body>
}

class AnalyticsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain) =
        chain.proceed(
            chain.request().newBuilder()
                .apply {
                    addHeader(
                        DEVICE_TYPE_HEADER,
                        "Android ${Build.MANUFACTURER} - ${Build.DEVICE} (${Build.MODEL})"
                    )
                    addHeader(
                        APP_VERSION_HEADER,
                        "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                    )
                    addHeader(
                        OS_VERSION_HEADER,
                        "${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})"
                    )
                }
                .build()
        )
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

                request = request.newBuilder()
                    .removeHeader(AUTH)
                    .addHeader(AUTH, "Bearer $token")
                    .build()

            } catch (e: TokenExpiredException) {
                runBlocking { authProvider.signOut() }
                authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
            }
        }

        try {
            val deviceId = runBlocking { authProvider.retrieveDeviceId() }

            if (deviceId.isNotEmpty()) {
                request = request.newBuilder()
                    .removeHeader(X_CENSO_ID)
                    .addHeader(X_CENSO_ID, deviceId)
                    .build()
            }
        } catch (e: TokenExpiredException) {
            runBlocking { authProvider.signOut() }
            authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
        }

        val response = chain.proceed(request)

        if (authRequired && response.code == UNAUTHORIZED) {
            runBlocking { authProvider.signOut() }
            authProvider.setUserState(userState = UserState.REFRESH_TOKEN_EXPIRED)
        }

        return response
    }
}
