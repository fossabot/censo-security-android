package com.strikeprotocols.mobile.data

import android.content.Context
import androidx.annotation.AnyThread
import com.okta.authn.sdk.AuthenticationFailureException
import com.okta.authn.sdk.client.AuthenticationClient
import com.okta.authn.sdk.client.AuthenticationClients
import com.okta.oidc.*
import com.okta.oidc.clients.AuthClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.oidc.util.AuthorizationException
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.OktaAuth.Companion.AUTH_FAILED
import com.strikeprotocols.mobile.data.OktaAuth.Companion.ISSUER_URL
import com.strikeprotocols.mobile.data.OktaAuth.Companion.LOGIN_REDIRECT_URI
import com.strikeprotocols.mobile.data.OktaAuth.Companion.LOGOUT_REDIRECT_URI
import com.strikeprotocols.mobile.data.OktaAuth.Companion.OIDC_SCOPES
import kotlin.coroutines.suspendCoroutine
import com.okta.oidc.results.Result as OktaResult

interface AuthProvider {

    val isAuthenticated: Boolean
    val isExpired: Boolean
    val token: String?

    suspend fun retrieveToken(): String

    //Sign In Methods
    suspend fun getSessionToken(username: String, password: String): String
    suspend fun authenticate(sessionToken: String): String
    suspend fun signOut()

    //UserState Notifying Functionality
    fun setUserState(userState: UserState)
    fun addUserStateListener(listener: UserStateListener)
    fun clearAllListeners()
}

class OktaAuth(applicationContext: Context) : AuthProvider {

    private val listeners: MutableList<UserStateListener> = mutableListOf()

    private val authClient: AuthClient
    private lateinit var authenticationClient: AuthenticationClient

    override val isAuthenticated: Boolean
        get() = authClient.sessionClient.isAuthenticated
    override val isExpired: Boolean
        get() = authClient.sessionClient.tokens.isAccessTokenExpired
    override val token: String?
        get() = authClient.sessionClient.tokens.idToken

    fun setupAuthenticationClient() {
        authenticationClient =
            AuthenticationClients.builder().setOrgUrl(BuildConfig.OKTA_BASE_URL).build()
    }

    init {
        setupAuthenticationClient()

        val config = OIDCConfig.Builder()
            .clientId(BuildConfig.OKTA_CLIENT_ID)
            .discoveryUri(ISSUER_URL)
            .redirectUri(LOGIN_REDIRECT_URI)
            .endSessionRedirectUri(LOGOUT_REDIRECT_URI)
            .scopes(*OIDC_SCOPES)
            .create()

        authClient = Okta.AuthBuilder()
            .withConfig(config)
            .withContext(applicationContext)
            .withStorage(SharedPreferenceStorage(applicationContext))
            .setRequireHardwareBackedKeyStore(!BuildConfig.DEBUG)
            .create()
    }

    override suspend fun retrieveToken() =
        if (!isExpired) {
            retrieveValidToken(authClient.sessionClient.tokens)
        } else {
            refreshToken()
        }

    override suspend fun getSessionToken(username: String, password: String): String =
        suspendCoroutine { cont ->
            try {
                authenticationClient.authenticate(username, password.toCharArray(), null, null)
                    ?.run {
                        if (statusString == Companion.SUCCESS_STATUS) {
                            cont.resumeWith(Result.success(sessionToken))
                        } else {
                            cont.resumeWith(Result.failure(SessionTokenException()))
                        }
                    }
            } catch (e: AuthenticationFailureException) {
                cont.resumeWith(Result.failure(SessionTokenException()))
            }
        }

    override suspend fun authenticate(sessionToken: String): String = suspendCoroutine { cont ->
        if (!isAuthenticated) {
            authClient.signIn(
                sessionToken, null, object : RequestCallback<OktaResult, AuthorizationException> {
                    override fun onSuccess(result: OktaResult) {
                        cont.resumeWith(Result.success(sessionToken))
                    }

                    override fun onError(
                        error: String?,
                        exception: AuthorizationException?
                    ) {
                        cont.resumeWith(
                            Result.failure(
                                exception ?: AuthorizationException.GeneralErrors.NETWORK_ERROR
                            )
                        )
                    }
                })
        } else {
            token
                ?.let { cont.resumeWith(Result.success(it)) }
                ?: cont.resumeWith(
                    Result.failure(
                        TokenExpiredException()
                    )
                )
        }
    }

    fun retrieveValidToken(tokens: Tokens) = tokens.idToken ?: throw TokenExpiredException()

    private suspend fun refreshToken(): String = suspendCoroutine { cont ->
        authClient.sessionClient.refreshToken(object :
            RequestCallback<Tokens, AuthorizationException> {
            override fun onSuccess(result: Tokens) {
                if (!result.isAccessTokenExpired) {
                    cont.resumeWith(Result.success(retrieveValidToken(result)))
                } else {
                    throw TokenExpiredException()
                }
            }

            override fun onError(error: String?, exception: AuthorizationException?) {
                throw TokenExpiredException()
            }
        })
    }

    override suspend fun signOut() {
        authClient.sessionClient.clear()
        return suspendCoroutine { cont ->
            authClient.signOut(object : ResultCallback<Int, AuthorizationException> {
                override fun onSuccess(result: Int) {
                    setupAuthenticationClient()
                    cont.resumeWith(Result.success(Unit))
                }

                override fun onCancel() {
                    setupAuthenticationClient()
                    cont.resumeWith(Result.success(Unit))
                }

                override fun onError(msg: String?, exception: AuthorizationException?) {
                    setupAuthenticationClient()
                    cont.resumeWith(Result.success(Unit))
                }
            })
        }
    }

    @AnyThread
    override fun addUserStateListener(listener: UserStateListener) {
        synchronized(listeners) { listeners.add(listener) }
    }

    @AnyThread
    override fun clearAllListeners() {
        synchronized(listeners) { listeners.clear() }
    }

    @AnyThread
    override fun setUserState(userState: UserState) {
        synchronized(listeners) {
            for (listener in listeners) {
                Thread { listener.onUserStateChanged(userState) }.start()
            }
        }
    }

    object Companion {
        const val SUCCESS_STATUS = "SUCCESS"
        const val AUTH_FAILED = "AUTH_FAILED"

        private const val REDIRECT_URI = "${BuildConfig.CORE_APP_ID}${BuildConfig.REDIRECT_SUFFIX}"
        const val LOGIN_REDIRECT_URI = "$REDIRECT_URI:/login"
        const val LOGOUT_REDIRECT_URI = "$REDIRECT_URI:/logout"
        val OIDC_SCOPES = arrayOf("openid", "profile", "email", "offline_access")
        const val ISSUER_URL = "${BuildConfig.OKTA_DOMAIN}/oauth2/${BuildConfig.OKTA_ISSUER_ID}"
    }

}

class TokenExpiredException : Exception("Token No Longer Valid")
class SessionTokenException : Exception(AUTH_FAILED)

interface UserStateListener {
    fun onUserStateChanged(userState: UserState)
}

enum class UserState {
    REFRESH_TOKEN_EXPIRED
}