package com.strikeprotocols.mobile.presentation.sign_in

import android.content.Context
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

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val pushRepository: PushRepository,
    private val strikeUserData: StrikeUserData,
    private val phraseValidator: PhraseValidator
) : ViewModel() {

    companion object {
        const val CLIPBOARD_LABEL_PHRASE = "Phrase"

        const val FIRST_WORD_INDEX = 0
        const val LAST_WORD_INDEX = 23

        const val LAST_WORD_RANGE_SET_INDEX = 20
        const val CHANGE_AMOUNT = 4
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
    fun onResume() {
        if (state.keyCreationFlowStep == KeyCreationFlowStep.PHRASE_COPIED_STEP) {
            state =
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.PHRASE_SAVED_STEP)
        }
    }

    fun onStart() {
        state = state.copy(loggedInStatusResult = Resource.Loading())
        viewModelScope.launch {
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
                    val sessionToken = userRepository.retrieveSessionToken(state.email, state.password)
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
                    state.copy(
                        addWalletSignerResult =
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
    private fun launchRecoverKeyFromPhraseFlow() {
        state = state.copy(
            showKeyRecoveryUI = true,
            keyRecoveryFlowStep = KeyRecoveryFlowStep.ENTRY_STEP
        )
    }

    private fun launchPhraseVerificationUI() {
        if (!state.showKeyCreationUI) {
            state = state.copy(
                showKeyCreationUI = true,
                keyCreationFlowStep = KeyCreationFlowStep.ENTRY_STEP,
            )
        }
    }

    fun phraseFlowAction(phraseFlowAction: PhraseFlowAction) {
        when (phraseFlowAction) {
            is PhraseFlowAction.WordIndexChanged -> {
                handleWordIndexChanged(increasing = phraseFlowAction.increasing)
            }
            PhraseFlowAction.LaunchManualKeyCreation -> {
                state = state.copy(
                    keyCreationFlowStep = KeyCreationFlowStep.WRITE_WORD_STEP,
                    wordIndex = 0
                )
            }
            is PhraseFlowAction.ChangeCreationFlowStep -> {
                state =
                    state.copy(keyCreationFlowStep = phraseFlowAction.phraseVerificationFlowStep)
            }
            is PhraseFlowAction.ChangeRecoveryFlowStep -> {
                state =
                    state.copy(keyRecoveryFlowStep = phraseFlowAction.phraseGenerationFlowStep)
            }
        }
    }

    fun phraseVerificationAction() {
        state = when (state.keyCreationFlowStep) {
            KeyCreationFlowStep.ENTRY_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.COPY_PHRASE_STEP)
            KeyCreationFlowStep.PHRASE_COPIED_STEP, KeyCreationFlowStep.COPY_PHRASE_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.PHRASE_COPIED_STEP)
            KeyCreationFlowStep.PHRASE_SAVED_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP)
            KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP, KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.ALL_SET_STEP)
            KeyCreationFlowStep.WRITE_WORD_STEP -> {
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.WRITE_WORD_STEP)
            }
            KeyCreationFlowStep.ALL_SET_STEP ->
                state.copy(
                    keyCreationFlowStep = KeyCreationFlowStep.FINISHED,
                    showKeyCreationUI = false
                )
            KeyCreationFlowStep.FINISHED ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.ENTRY_STEP)
            KeyCreationFlowStep.UNINITIALIZED ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.ENTRY_STEP)
        }
    }

    fun phraseVerificationBackNavigation() {
        state = when (state.keyCreationFlowStep) {
            KeyCreationFlowStep.ENTRY_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED)
            KeyCreationFlowStep.COPY_PHRASE_STEP,
            KeyCreationFlowStep.PHRASE_SAVED_STEP,
            KeyCreationFlowStep.PHRASE_COPIED_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.ENTRY_STEP)
            KeyCreationFlowStep.CONFIRM_KEY_ENTRY_STEP, KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.COPY_PHRASE_STEP)
            KeyCreationFlowStep.ALL_SET_STEP ->
                state.copy(
                    keyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED,
                    showKeyCreationUI = false
                )
            KeyCreationFlowStep.WRITE_WORD_STEP -> {
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.ENTRY_STEP)
            }
            KeyCreationFlowStep.FINISHED ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED)
            KeyCreationFlowStep.UNINITIALIZED ->
                state.copy(keyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED)
        }
    }

    fun phraseRegenerationAction() {
        state = when (state.keyRecoveryFlowStep) {
            KeyRecoveryFlowStep.ENTRY_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP)
            KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.ALL_SET_STEP)
            KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.ALL_SET_STEP)
            KeyRecoveryFlowStep.ALL_SET_STEP -> state.copy(
                keyRecoveryFlowStep = KeyRecoveryFlowStep.FINISHED
            )
            KeyRecoveryFlowStep.FINISHED,
            KeyRecoveryFlowStep.UNINITIALIZED ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.ENTRY_STEP)
        }
    }

    fun phraseRegenerationBackNavigation() {
        state = when (state.keyRecoveryFlowStep) {
            KeyRecoveryFlowStep.ENTRY_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.UNINITIALIZED)
            KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.ENTRY_STEP)
            KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.ENTRY_STEP)
            KeyRecoveryFlowStep.ALL_SET_STEP ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP)
            KeyRecoveryFlowStep.FINISHED ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.UNINITIALIZED)
            KeyRecoveryFlowStep.UNINITIALIZED ->
                state.copy(keyRecoveryFlowStep = KeyRecoveryFlowStep.UNINITIALIZED)
        }
    }

    fun verifyPastedPhrase(pastedPhrase: String) {
        if (pastedPhrase == state.pastedPhrase) {
            return
        }

        viewModelScope.launch {
            state = state.copy(pastedPhrase = pastedPhrase)
            try {
                if (phraseValidator.isPhraseValid(pastedPhrase) && pastedPhrase == state.phrase) {
                    verifiedPhraseSuccess()
                } else {
                    verifiedPhraseFailure(Exception("Failed to verify phrase"))
                }
            } catch (e: Exception) {
                verifiedPhraseFailure(Exception(e.message ?: "Failed to verify phrase"))
            }
        }
    }

    private suspend fun verifiedPhraseSuccess() {
        state.phrase?.let {
            try {
                val initialAuthData = userRepository.generateInitialAuthDataAndSaveKeyToUser(
                    Mnemonics.MnemonicCode(phrase = it)
                )
                //wiping key in the VM because we have saved private key, and will not reference phrase again.
                state = state.copy(initialAuthData = initialAuthData, phrase = null)
                attemptAddWalletSigner()
                state =
                    state.copy(keyCreationFlowStep = KeyCreationFlowStep.ALL_SET_STEP)
            } catch (e: Exception) {
                //Todo: STR-241 handle error case if generating key fails. Less likely now that we have phrase but still could happen...
            }
        } ?: setCreateKeyError(PhraseException.NULL_PHRASE_IN_STATE)
    }

    private fun setCreateKeyError(message: String) {
        state = state.copy(createKeyError = message)
    }

    private fun verifiedPhraseFailure(exception: Exception?) {
        //todo: need to wipe more state here on ticket str-256. That state does not currently exist, but will after we finalize UI.
        viewModelScope.launch {
            userRepository.clearGeneratedAuthData()
            state = state.copy(
                keyCreationFlowStep = KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP,
            )
        }
    }

    fun exitPhraseFlow() {
        state = state.copy(
            keyRecoveryFlowStep = KeyRecoveryFlowStep.UNINITIALIZED,
            keyCreationFlowStep = KeyCreationFlowStep.UNINITIALIZED,
            showKeyCreationUI = false,
            showKeyRecoveryUI = false,
            manualAuthFlowLoading = false,
            autoAuthFlowLoading = false,
            recoverKeyError = null,
            createKeyError = null
        )
    }

    fun verifyPhraseToRecoverKeyPair(pastedPhrase: String) {
        viewModelScope.launch {
            state = state.copy(keyRegenerationLoading = true)

            try {
                if (!phraseValidator.isPhraseValid(phrase = pastedPhrase)) {
                    state = state.copy(
                        keyRegenerationLoading = false,
                        recoverKeyError = InvalidKeyPhraseException.INVALID_KEY_PHRASE_ERROR
                    )
                    return@launch
                }
            } catch (e: Exception) {
                state = state.copy(
                    keyRegenerationLoading = false,
                    recoverKeyError = RecoverKeyException.DEFAULT_KEY_RECOVERY_ERROR
                )
            }

            val verifyUser = state.verifyUserResult.data
            val publicKey = verifyUser?.firstPublicKey()

            if (verifyUser != null && publicKey != null) {
                try {
                    userRepository.regenerateAuthDataAndSaveKeyToUser(pastedPhrase, publicKey)
                    state = state.copy(
                        recoverKeyError = null,
                        keyRecoveryFlowStep = KeyRecoveryFlowStep.ALL_SET_STEP
                    )
                } catch (e: Exception) {
                    recoverKeyFailure(e)
                }
            } else {
                recoverKeyFailure(AuthDataException.InvalidVerifyUserException())
            }
        }
    }

    private fun recoverKeyFailure(exception: Exception?) {
        //todo: need to wipe more state here on ticket str-255. That state does not currently exist, but will after we finalize UI.
        state = state.copy(
            recoverKeyError = exception?.message ?: RecoverKeyException.DEFAULT_KEY_RECOVERY_ERROR,
            keyRecoveryFlowStep = KeyRecoveryFlowStep.CONFIRM_KEY_ERROR_STEP
        )
    }

    private fun handleWordIndexChanged(increasing: Boolean) {
        var wordIndex =
            if (increasing) {
                state.wordIndex + CHANGE_AMOUNT
            } else {
                state.wordIndex - CHANGE_AMOUNT
            }

        if (wordIndex > LAST_WORD_INDEX) {
            wordIndex = FIRST_WORD_INDEX
        } else if (wordIndex < FIRST_WORD_INDEX) {
            wordIndex = LAST_WORD_RANGE_SET_INDEX
        }

        state = state.copy(wordIndex = wordIndex)
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

    private suspend fun getUserAuthFlowState(verifyUser: VerifyUser): UserAuthFlow {
        val savedPrivateKey = userRepository.getSavedPrivateKey()
        val publicKeysPresent = !verifyUser.publicKeys.isNullOrEmpty()

        //no public keys on backend then we need to generate data
        if (!publicKeysPresent && savedPrivateKey.isEmpty()) {
            return UserAuthFlow.FIRST_LOGIN
        } else if (!publicKeysPresent) {
            return UserAuthFlow.LOCAL_KEY_PRESENT_NO_BACKEND_KEYS
        }

        val walletSigners: List<WalletSigner?> =
            try {
                userRepository.getWalletSigners()
            } catch (e: Exception) {
                throw WalletSignersException()
            }

        if (savedPrivateKey.isEmpty()) {
            return UserAuthFlow.EXISTING_BACKEND_KEY_LOCAL_KEY_MISSING
        }

        val doesUserHaveValidLocalKey =
            userRepository.doesUserHaveValidLocalKey(verifyUser, walletSigners)

        if (doesUserHaveValidLocalKey) {
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
                launchRecoverKeyFromPhraseFlow()
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
            launchPhraseVerificationUI()
        }
    }

    private fun handleEncryptionManagerException(exception: Exception) {
        state = if (exception is WalletSignersException) {
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
