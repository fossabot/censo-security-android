package com.strikeprotocols.mobile.data

import android.content.Context
import android.content.IntentSender
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.RuntimeExecutionException
import java.lang.Exception

interface CredentialsProvider {
    fun saveCredential(
        email: String,
        password: String,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        saveSuccess: () -> Unit,
        saveFailed: (exception: Exception?) -> Unit
    )


    fun retrieveCredential(
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        retrievalSuccess: (String?) -> Unit,
        retrievalFailed: (exception: Exception?) -> Unit
    )
}

class CredentialsProviderImpl(
    context: Context
) : CredentialsProvider {

    private val credentialsClient = Credentials.getClient(context)

    override fun saveCredential(
        email: String,
        password: String,
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        saveSuccess: () -> Unit,
        saveFailed: (exception: Exception?) -> Unit
    ) {
        val credential: Credential = Credential.Builder(email).setPassword(password).build()

        credentialsClient.save(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveSuccess()
            } else {
                val error = task.exception
                if (error is ResolvableApiException) {
                    try {
                        launcher.launch(
                            IntentSenderRequest.Builder(error.resolution)
                                .build()
                        )
                    } catch (exception: IntentSender.SendIntentException) {
                        saveFailed(exception)
                    }
                } else {
                    saveFailed(error)
                }
            }
        }
    }

    override fun retrieveCredential(
        launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
        retrievalSuccess: (String?) -> Unit,
        retrievalFailed: (exception: Exception?) -> Unit
    ) {
        val credentialsRequest = CredentialRequest.Builder()
            .setPasswordLoginSupported(true)
            .setAccountTypes(IdentityProviders.GOOGLE)
            .build()

        credentialsClient.request(credentialsRequest).addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    val credentialPassword =
                        (task.result as CredentialRequestResponse).credential?.password
                    retrievalSuccess(credentialPassword)
                } else {
                    val error = task.exception
                    if (error is ResolvableApiException) {
                        try {
                            launcher.launch(
                                IntentSenderRequest.Builder(error.resolution)
                                    .build()
                            )
                        } catch (exception: IntentSender.SendIntentException) {
                            retrievalFailed(exception)
                        }
                    } else {
                        retrievalFailed(error)
                    }
                }
            } catch (e: RuntimeExecutionException) {
                when (val error = task.exception) {
                    is ResolvableApiException -> {
                        // This is most likely the case where the user has multiple saved
                        // credentials and needs to pick one. This requires showing UI to
                        // resolve the read request.
                        val resolvableException = (task.exception as ResolvableApiException)
                        try {
                            launcher.launch(
                                IntentSenderRequest.Builder(resolvableException.resolution)
                                    .build()
                            )
                        } catch (exception: IntentSender.SendIntentException) {
                            retrievalFailed(exception)
                        }
                    }
                    is ApiException -> {
                        // The user must create an account or sign in manually.
                        retrievalFailed(error)
                    }
                    else -> {
                        retrievalFailed(error)
                    }
                }
            } catch (e: Exception) {
                retrievalFailed(e)
            }
        }
    }

    companion object {
        val INTENT_FAILED = Exception("INTENT_RESULT_NOT_OK")
    }
}