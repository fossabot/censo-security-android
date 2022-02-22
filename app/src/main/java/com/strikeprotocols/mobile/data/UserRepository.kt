package com.strikeprotocols.mobile.data

import android.content.Context
import com.okta.oidc.OIDCConfig
import com.okta.oidc.Okta.AuthBuilder
import com.okta.oidc.RequestCallback
import com.okta.oidc.clients.AuthClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.oidc.util.AuthorizationException
import com.okta.oidc.util.AuthorizationException.GeneralErrors.NETWORK_ERROR
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.data.UserRepositoryImpl.Companion.DISCOVERY_URI
import com.strikeprotocols.mobile.data.UserRepositoryImpl.Companion.LOGIN_REDIRECT_URI
import com.strikeprotocols.mobile.data.UserRepositoryImpl.Companion.LOGOUT_REDIRECT_URI
import com.strikeprotocols.mobile.data.UserRepositoryImpl.Companion.OIDC_SCOPES
import kotlin.coroutines.suspendCoroutine
import com.okta.oidc.results.Result as OktaResult

interface UserRepository {
    suspend fun authenticate(sessionToken: String): String
}

class UserRepositoryImpl(applicationContext: Context) : UserRepository {

    private val authClient: AuthClient

    override suspend fun authenticate(sessionToken: String): String = suspendCoroutine { cont ->
        val sessionClient = authClient.sessionClient

        if (!sessionClient.isAuthenticated) {

            authClient.signIn(
                sessionToken,
                null,
                object : RequestCallback<OktaResult, AuthorizationException> {
                    override fun onSuccess(result: com.okta.oidc.results.Result) {
                        cont.resumeWith(Result.success(sessionToken))
                    }

                    override fun onError(
                        error: String?,
                        exception: AuthorizationException?
                    ) {
                        cont.resumeWith(Result.failure(exception ?: NETWORK_ERROR))
                    }

                })
        } else cont.resumeWith(Result.success(sessionToken))
    }

    init {
        val config = OIDCConfig.Builder()
            .clientId(BuildConfig.OKTA_CLIENT_ID)
            .discoveryUri(DISCOVERY_URI)
            .redirectUri(LOGIN_REDIRECT_URI)
            .endSessionRedirectUri(LOGOUT_REDIRECT_URI)
            .scopes(*OIDC_SCOPES)
            .create()

        authClient = AuthBuilder()
            .withConfig(config)
            .withContext(applicationContext)
            .withStorage(SharedPreferenceStorage(applicationContext))
            .create()

    }

    object Companion {
        private const val REDIRECT_URI = "${BuildConfig.CORE_APP_ID}${BuildConfig.REDIRECT_SUFFIX}"
        const val DISCOVERY_URI = "${BuildConfig.OKTA_DOMAIN}/oauth2/default"
        const val LOGIN_REDIRECT_URI = "$REDIRECT_URI:/login"
        const val LOGOUT_REDIRECT_URI = "$REDIRECT_URI:/logout"
        val OIDC_SCOPES = arrayOf("openid", "profile", "email", "offline_access")
    }


}
