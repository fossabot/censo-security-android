package com.censocustody.android.data

import androidx.annotation.AnyThread
import com.auth0.android.jwt.JWT
import com.raygun.raygun4android.RaygunClient
import com.censocustody.android.common.CrashReportingUtil.JWT_TAG
import com.censocustody.android.common.CrashReportingUtil.MANUALLY_REPORTED_TAG
import java.util.*
import kotlin.collections.HashMap

interface AuthProvider {

    suspend fun retrieveToken(): String
    suspend fun signOut()
    suspend fun retrieveUserEmail(): String
    suspend fun retrieveDeviceId(): String

    //UserState Notifying Functionality
    fun setUserState(userState: UserState)
    fun addUserStateListener(listener: UserStateListener)
    fun clearAllListeners()
}

class CensoAuth(
    val encryptionManager: EncryptionManager,
    val securePreferences: SecurePreferences
) : AuthProvider {

    private val listeners: HashMap<String, UserStateListener> = hashMapOf()

    override suspend fun retrieveUserEmail(): String {
        val email = SharedPrefsHelper.retrieveUserEmail()

        if (email.isNotEmpty()) return email

        val token = retrieveToken()

        if (token.isNotEmpty()) {
            val jwtEmail = retrieveJwtEmail(token)
            if(jwtEmail.isNotEmpty()) {
                SharedPrefsHelper.saveUserEmail(jwtEmail)
            }
            return jwtEmail
        }

        return ""
    }

    private fun retrieveJwtEmail(jwt: String): String {
        return try {
            val jwtDecoded = JWT(jwt)
            val jwtEmail =
                if (jwtDecoded.claims.containsKey("email")) {
                    jwtDecoded.claims["email"]?.asString()
                } else {
                    ""
                }

            jwtEmail ?: ""
        } catch (e: Exception) {
            RaygunClient.send(e, listOf(JWT_TAG, MANUALLY_REPORTED_TAG))
            ""
        }
    }

    override suspend fun retrieveToken(): String {
        val token = securePreferences.retrieveToken()

        if (token.isEmpty()) {
            throw TokenExpiredException()
        }

        try {
            val jwtDecoded = JWT(token)

            val expiresAt = jwtDecoded.expiresAt
            val currentDate = Date()

            if (currentDate > expiresAt) {
                throw TokenExpiredException()
            }
        } catch (e: Exception) {
            RaygunClient.send(e, listOf(JWT_TAG, MANUALLY_REPORTED_TAG))
        }

        return token
    }

    override suspend fun retrieveDeviceId(): String {
        val email = retrieveUserEmail()
        return SharedPrefsHelper.retrieveDeviceId(email)
    }

    override suspend fun signOut() {
        SharedPrefsHelper.setUserLoggedIn(false)
        SharedPrefsHelper.clearEmail()
        securePreferences.clearToken()
    }

    @AnyThread
    override fun addUserStateListener(listener: UserStateListener) {
        synchronized(listeners) {
            listeners.put(listener::class.java.name, listener)
        }
    }

    @AnyThread
    override fun clearAllListeners() {
        synchronized(listeners) { listeners.clear() }
    }

    @AnyThread
    override fun setUserState(userState: UserState) {
        synchronized(listeners) {
            for (listener in listeners.values) {
                Thread { listener.onUserStateChanged(userState) }.start()
            }
        }
    }
}

class IncorrectSentinelException : Exception("Incorrect sentinel data")
class TokenExpiredException : Exception("Token No Longer Valid")
class MissingDeviceIdException : Exception("No Device Id Found For Email")

interface UserStateListener {
    fun onUserStateChanged(userState: UserState)
}

enum class UserState {
    REFRESH_TOKEN_EXPIRED, INVALIDATED_KEY, RESET_DEVICE
}