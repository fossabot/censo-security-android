package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.bip39.Mnemonics
import com.google.firebase.messaging.FirebaseMessaging
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.models.PushBody
import com.strikeprotocols.mobile.data.models.VerifyUser
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.sign_in.SignInState.Companion.DEFAULT_SIGN_IN_ERROR_MESSAGE
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val strikeUserData: StrikeUserData,
    private val phraseValidator: PhraseValidator
) : ViewModel() {

    companion object {
        const val CLIPBOARD_LABEL_PHRASE = "Phrase"
    }

    var state by mutableStateOf(SignInState())
        private set

    //region Handle User Input
    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail, emailErrorEnabled = false)
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }

    fun updatePhrase(phrase: String) {
        val formattedPhrase = phraseValidator.format(text = phrase)
        state = state.copy(phrase = formattedPhrase)
    }
    //endregion

    //region Autocomplete state handling
    fun updatePredictions(query: String) {
        val updatedPredictions = if (query.isEmpty()) emptyList() else
            PhraseValidatorImpl.words.filter { it.startsWith(query.trim().lowercase()) }

        state = state.copy(
            wordQuery = query,
            wordPredictions = updatedPredictions
        )
    }

    fun clearQuery() {
        updatePredictions("")
    }

    fun wordSelected(word: String) {
        strikeLog(message = "Word selected: ${word.lowercase()}")
        state = state.copy(
            wordQuery = "",
            wordPredictions = emptyList()
        )
    }


    fun wordEntered() {
        strikeLog(message = "Word entered: ${state.wordQuery.lowercase()}")
        state = state.copy(
            wordQuery = "",
            wordPredictions = emptyList()
        )
    }
    //endregion

    //region lifecycle methods
    fun onStart() {
        state = state.copy(loggedInStatusResult = Resource.Loading())
        viewModelScope.launch {
            delay(500)
            val userLoggedIn = try {
                userRepository.userLoggedIn()
            } catch (e: Exception) {
                false
            }

            if (userLoggedIn) {
                triggerAutoAuthFlow()
            }
            state = state.copy(loggedInStatusResult = Resource.Uninitialized)
        }
    }
    //endregion

    //region Login + API Calls
    fun attemptLogin() {
        if (state.signInButtonEnabled) {
            state = state.copy(loginResult = Resource.Loading(), manualAuthFlowLoading = true)
            viewModelScope.launch(Dispatchers.IO) {
                state = try {
                    val sessionToken =
                        userRepository.retrieveSessionToken(state.email, state.password)
                    val token = userRepository.authenticate(sessionToken)
                    userRepository.saveUserEmail(state.email)
                    state.copy(loginResult = Resource.Success(token))
                } catch (e: Exception) {
                    state.copy(loginResult = Resource.Error(e.message ?: NO_INTERNET_ERROR))
                }
            }
        } else {
            state = state.copy(
                emailErrorEnabled = !state.emailValid(),
                passwordErrorEnabled = !state.passwordValid()
            )
        }
    }

    private fun retrieveUserVerifyDetails() {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, e ->
            state = state.copy(
                verifyUserResult = Resource.Error(
                    e.message ?: NO_INTERNET_ERROR
                )
            )
        }

        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            try {
                val verifyUserData = userRepository.verifyUser()
                strikeUserData.setStrikeUser(verifyUserData)
                state = state.copy(verifyUserResult = Resource.Success(verifyUserData))
                handleAuthFlow(verifyUserData)
            } catch (e: Exception) {
                strikeUserData.setStrikeUser(null)
                state = state.copy(
                    verifyUserResult = Resource.Error(
                        e.message ?: DEFAULT_SIGN_IN_ERROR_MESSAGE
                    )
                )
            }
        }
    }

    fun setUserLoggedInSuccess() {
        viewModelScope.launch {
            userRepository.saveUserEmail(state.email)
            userRepository.setUserLoggedIn()
            submitNotificationTokenForRegistration()
        }
    }

    private suspend fun submitNotificationTokenForRegistration() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()

            val deviceId = pushRepository.getDeviceId()

            if (token.isNotEmpty() && deviceId.isNotEmpty()) {
                val pushBody = PushBody(
                    deviceId = deviceId,
                    token = token
                )
                pushRepository.addPushNotification(pushBody = pushBody)
            }
        } catch (e: Exception) {

        }
    }

    private fun attemptAddWalletSigner() {
        viewModelScope.launch {
            state.initialAuthData?.let { safeInitialAuthData ->
                state = try {
                    val walletSigner = userRepository.addWalletSigner(safeInitialAuthData.walletSignerBody)
                    state.copy(addWalletSignerResult = Resource.Success(walletSigner))
                } catch (e: Exception) {
                    state.copy(addWalletSignerResult =
                        Resource.Error(e.message ?: DEFAULT_SIGN_IN_ERROR_MESSAGE)
                    )
                }
            } ?: restartAuthFlow()
        }
    }

    private fun triggerAutoAuthFlow() {
        state = state.copy(autoAuthFlowLoading = true)
        retrieveUserVerifyDetails()
    }
    //endregion

    //region Reset Resource State
    fun resetLoginCallAndRetrieveUserInformation() {
        resetLoginCall()
        retrieveUserVerifyDetails()
    }

    fun resetLoginCall() {
        state = state.copy(loginResult = Resource.Uninitialized)
    }

    fun resetVerifiedPhrase() {
        state = state.copy(verifiedPhrase = Resource.Uninitialized)
    }

    fun resetRegenerateKeyFromPhrase() {
        state = state.copy(regenerateKeyFromPhrase = Resource.Uninitialized)
    }

    fun resetRegenerateData() {
        state = state.copy(regenerateData = Resource.Uninitialized)
    }

    fun resetWalletSignersCall() {
        state = state.copy(walletSignersResult = Resource.Uninitialized)
    }

    fun resetAddWalletSignersCall() {
        state = state.copy(addWalletSignerResult = Resource.Uninitialized)
    }

    fun resetValidKey() {
        state = state.copy(keyValid = Resource.Uninitialized)
    }

    fun resetShouldDisplayPhraseVerificationDialog() {
        state = state.copy(showPhraseVerificationDialog = false)
    }

    fun loadingFinished() {
        //manual auth flow happens when the user signs in manually
        //auto auth flow happens when we detect that the user is currently logged in

        //If the auto auth flow is triggered,
        //we want to show a splash screen with loading indicator

        //When any loading is finished, reset both loading properties
        state = state.copy(manualAuthFlowLoading = false)
        state = state.copy(autoAuthFlowLoading = false)
    }

    fun resetAuthFlowException() {
        state = state.copy(authFlowException = Resource.Uninitialized)
    }

    fun resetVerifyUserResult() {
        state = state.copy(verifyUserResult = Resource.Uninitialized)
    }

    fun resetLoggedInResource() {
        state = state.copy(loggedInStatusResult = Resource.Uninitialized)
    }

    private fun resetStateToSendUserBackToLogin() {
        state = state.copy(
            email = "",
            password = "",
            manualAuthFlowLoading = false,
            autoAuthFlowLoading = false,
            verifyUserResult = Resource.Uninitialized
        )
    }
    //endregion

    //region Phrase + Key Generation Flows
    private fun launchRegenerateKeyFromPhraseFlow() {
        if (state.regenerateKeyFromPhrase !is Resource.Loading) {
            state = state.copy(
                regenerateKeyFromPhrase = Resource.Loading(),
                showPhraseKeyRegenerationUI = true)
        }
    }

    fun launchVerifyPhraseFlow() {
        if (state.verifiedPhrase !is Resource.Loading) {
            state = state.copy(verifiedPhrase = Resource.Loading(),
                showPhraseVerificationDialog = false
            )
        }
    }


    private fun launchPhraseVerificationDialog() {
        if(!state.showPhraseVerificationDialog) {
            state = state.copy(showPhraseVerificationDialog = true)
        }
    }

    fun launchPhraseVerificationUI() {
        if(!state.showPhraseVerificationUI) {
            state = state.copy(showPhraseVerificationUI = true)
        }
    }

    fun verifyPhraseToGenerateKeyPair() {
        if (phraseValidator.isPhraseValid(state.phrase ?: "")) {
            verifiedPhraseSuccess()
        } else {
            verifiedPhraseFailure(Exception("Failed to verify phrase"))
        }
    }

    private fun verifiedPhraseSuccess() {
        state = state.copy(
            verifiedPhrase = Resource.Success(Unit),
            showPhraseVerificationUI = false
        )
        resetVerifiedPhrase()

        viewModelScope.launch {

            state.phrase?.let {
                try {
                    val initialAuthData = userRepository.generateInitialAuthDataAndSaveKeyToUser(
                        Mnemonics.MnemonicCode(phrase = it))
                    //wiping key in the VM because we have saved private key, and will not reference phrase again.
                    state = state.copy(initialAuthData = initialAuthData, phrase = null)
                    attemptAddWalletSigner()
                } catch (e: Exception) {
                    //Todo: STR-241 handle error case if generating key fails. Less likely now that we have phrase but still could happen...
                }
            } ?: setVerifiedPhraseErrorState(PhraseException.NULL_PHRASE_IN_STATE)
        }
    }

    private fun setVerifiedPhraseErrorState(message: String) {
        state = state.copy(verifiedPhrase = Resource.Error(message = message))
    }

    private fun verifiedPhraseFailure(exception: Exception?) {
        //todo: need to wipe more state here on ticket str-256. That state does not currently exist, but will after we finalize UI.
        viewModelScope.launch {
            userRepository.clearGeneratedAuthData()
            state = state.copy(
                verifiedPhrase = Resource.Error(
                    exception?.message ?: "DEFAULT_SAVE_CREDENTIAL_ERROR",
                ),
                showPhraseVerificationUI = false
            )
        }
    }

    fun verifyPhraseToRegenerateKeyPair() {
        viewModelScope.launch {
            state.phrase?.let { safePhrase ->
                state = state.copy(keyRegenerationLoading = true)

                try {
                    if (!phraseValidator.isPhraseValid(phrase = safePhrase)) {
                        state = state.copy(
                            keyRegenerationLoading = false,
                            regenerateKeyFromPhrase = Resource.Error(InvalidKeyPhraseException.INVALID_KEY_PHRASE_ERROR)
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    state = state.copy(
                        keyRegenerationLoading = false,
                        regenerateKeyFromPhrase = Resource.Error(
                            e.message ?: RegenerateKeyPhraseException.DEFAULT_KEY_REGENERATION_ERROR
                        )
                    )
                }

                val verifyUser = state.verifyUserResult.data
                val publicKey = verifyUser?.firstPublicKey()

                if (verifyUser != null && publicKey != null) {
                    try {
                        userRepository.regenerateAuthDataAndSaveKeyToUser(safePhrase, publicKey)
                        state = state.copy(
                            regenerateKeyFromPhrase = Resource.Uninitialized,
                            keyRegenerationLoading = false,
                            showPhraseKeyRegenerationUI = false)
                        restartAuthFlow()
                    } catch (e: Exception) {
                        regeneratePhraseFailure(e)
                    }
                } else {
                    regeneratePhraseFailure(AuthDataException.InvalidVerifyUserException())
                }
            } ?: restartAuthFlow()
        }
    }

    private fun regeneratePhraseFailure(exception: Exception?) {
        //todo: need to wipe more state here on ticket str-255. That state does not currently exist, but will after we finalize UI.
        state = state.copy(
            regenerateKeyFromPhrase = Resource.Error(
            exception?.message ?: "DEFAULT_RETRIEVE_CREDENTIAL_ERROR"),
            keyRegenerationLoading = false
        )
    }

    fun restartRegenerateKeyFromPhraseFlow() {
        resetRegenerateKeyFromPhrase()
        launchRegenerateKeyFromPhraseFlow()
    }
    //endregion

    //region Get Auth Flow State + Respond to Auth Flow State
    private suspend fun handleAuthFlow(
        verifyUser: VerifyUser,
    ) {
        try {
            val userAuthFlowState = getUserAuthFlowState(
                verifyUser = verifyUser
            )

            respondToUserAuthFlow(userAuthFlow = userAuthFlowState)
        } catch (e: Exception) {
            handleEncryptionManagerException(exception = e)
        }
    }

    private suspend fun getUserAuthFlowState(verifyUser: VerifyUser) : UserAuthFlow {
        val savedPrivateKey = userRepository.getSavedPrivateKey()
        val publicKeysPresent = !verifyUser.publicKeys.isNullOrEmpty()

        //no public keys on backend then we need to generate data
        if(!publicKeysPresent && savedPrivateKey.isEmpty()) {
            return UserAuthFlow.FIRST_LOGIN
        }  else if (!publicKeysPresent) {
            return UserAuthFlow.LOCAL_KEY_PRESENT_NO_BACKEND_KEYS
        }

        val walletSigners: List<WalletSigner?> =
            try {
                userRepository.getWalletSigners()
            } catch (e: Exception) {
                throw WalletSignersException()
            }

        if(savedPrivateKey.isEmpty()) {
            return UserAuthFlow.EXISTING_BACKEND_KEY_LOCAL_KEY_MISSING
        }

        val doesUserHaveValidLocalKey =
            userRepository.doesUserHaveValidLocalKey(verifyUser, walletSigners)

        if(doesUserHaveValidLocalKey) {
            return UserAuthFlow.KEY_VALIDATED
        }

        return UserAuthFlow.NO_VALID_KEY
    }

    private fun respondToUserAuthFlow(userAuthFlow: UserAuthFlow) {
        when (userAuthFlow) {
            UserAuthFlow.FIRST_LOGIN -> {
                handleFirstTimeLoginAuthFlow()
            }
            UserAuthFlow.KEY_VALIDATED -> {
                state = state.copy(keyValid = Resource.Success(Unit))
            }
            UserAuthFlow.EXISTING_BACKEND_KEY_LOCAL_KEY_MISSING -> {
                launchRegenerateKeyFromPhraseFlow()
            }
            UserAuthFlow.NO_VALID_KEY, UserAuthFlow.NO_LOCAL_KEY_AVAILABLE -> {
                state = state.copy(shouldAbortUserFromAuthFlow = true)
            }
            UserAuthFlow.LOCAL_KEY_PRESENT_NO_BACKEND_KEYS -> {
                regenerateData()
            }
        }
    }

    private fun regenerateData() {
        state = state.copy(regenerateData = Resource.Loading())
        viewModelScope.launch {
            state = try {
                val walletSigner = userRepository.regenerateDataAndUploadToBackend()
                state.copy(regenerateData = Resource.Success(walletSigner))
            } catch (e: Exception) {
                state.copy(regenerateData = Resource.Error(e.message ?: ""))
            }
        }
    }

    private fun handleFirstTimeLoginAuthFlow() {
        viewModelScope.launch {
            val phrase = userRepository.generatePhrase()
            state = state.copy(
                phrase = phrase
            )
            launchPhraseVerificationDialog()
        }
    }

    private fun handleEncryptionManagerException(exception: Exception) {
        state = if(exception is WalletSignersException) {
            state.copy(
                walletSignersResult =
                Resource.Error(exception.message ?: DEFAULT_SIGN_IN_ERROR_MESSAGE)
            )
        } else {
            state.copy(authFlowException = Resource.Success(exception))
        }
    }

    private suspend fun restartAuthFlow() {
        //Restart AuthFlow
        state.verifyUserResult.data?.let { safeVerifyUser ->
            handleAuthFlow(safeVerifyUser)
        } ?: resetStateToSendUserBackToLogin()
    }
    //endregion

}
