package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.*
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.CredentialsProviderImpl.Companion.CREDENTIAL_DATA_EMPTY
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.models.VerifyUser
import kotlinx.coroutines.*

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    //region Handle User Input
    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail, emailErrorEnabled = false)
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }
    //endregion

    //region Login + API Calls
    fun attemptLogin() {
        if (state.signInButtonEnabled) {
            state = state.copy(loginResult = Resource.Loading(), loadingData = true)
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
            val verifyUserData = userRepository.verifyUser()
            state = state.copy(verifyUserResult = Resource.Success(verifyUserData))

            handleAuthFlow(verifyUserData)
        }
    }

    fun setUserLoggedInSuccess() {
        viewModelScope.launch {
            userRepository.saveUserEmail(state.email)
            userRepository.setUserLoggedIn()
        }
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

    fun resetSaveCredential() {
        state = state.copy(saveCredential = Resource.Uninitialized)
    }

    fun resetRetrieveCredential() {
        state = state.copy(retrieveCredential = Resource.Uninitialized)
    }

    fun resetRegenerateData() {
        state = state.copy(regenerateData = Resource.Uninitialized)
    }

    fun resetVerifyCall() {
        state = state.copy(verifyUserResult = Resource.Uninitialized)
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

    fun resetShouldDisplaySmartLockDialog() {
        state = state.copy(shouldDisplaySmartLockDialog = false)
    }

    fun loadingFinished() {
        state = state.copy(loadingData = false)
    }
    //endregion

    //region Smart Lock Save + Retrieval
    //
    fun launchSmartLockRetrieveFlow() {
        if (state.retrieveCredential !is Resource.Loading) {
            state = state.copy(retrieveCredential = Resource.Loading())
        }
    }

    fun launchSmartLockSaveFlow() {
        if (state.saveCredential !is Resource.Loading) {
            state = state.copy(saveCredential = Resource.Loading())
        }
    }

    private fun launchSmartLockDialog() {
        if(!state.shouldDisplaySmartLockDialog) {
            state = state.copy(shouldDisplaySmartLockDialog = true)
        }
    }

    fun saveCredentialSuccess() {
        state = state.copy(saveCredential = Resource.Success(Unit))
        viewModelScope.launch {
            state.initialAuthData?.let { safeInitialAuthData ->
                userRepository.addWalletSigner(safeInitialAuthData.walletSignerBody)
            }
        }
    }

    fun retrieveCredentialSuccess(credential: String?) {
        state = state.copy(retrieveCredential = Resource.Success(credential))

        if (credential != null) {
            viewModelScope.launch {
                userRepository.saveGeneratedPassword(BaseWrapper.decode(credential))

                //Restart AuthFlow
                state.verifyUserResult.data?.let { safeVerifyUser ->
                    handleAuthFlow(safeVerifyUser)
                }
            }
        } else {
            retrieveCredentialFailed(CREDENTIAL_DATA_EMPTY)
        }
    }

    fun saveCredentialFailed(exception: Exception?) {
        viewModelScope.launch {
            userRepository.clearGeneratedAuthData()
            state = state.copy(
                saveCredential = Resource.Error(
                    exception?.message ?: "DEFAULT_SAVE_CREDENTIAL_ERROR"
                )
            )
        }
    }

    fun retrieveCredentialFailed(exception: Exception?) {
        state = state.copy(
            retrieveCredential = Resource.Error(
            exception?.message ?: "DEFAULT_RETRIEVE_CREDENTIAL_ERROR"))

    }
    //endregion

    //region Get Auth Flow State + Respond to Auth Flow State
    private suspend fun handleAuthFlow(
        verifyUser: VerifyUser,
    ) {
        val userAuthFlowState = getUserAuthFlowState(
            verifyUser = verifyUser
        )

        respondToUserAuthFlow(userAuthFlow = userAuthFlowState)
    }

    private suspend fun getUserAuthFlowState(verifyUser: VerifyUser) : UserAuthFlow {
        val savedEncryption = userRepository.getSavedPassword()
        val publicKeysPresent = !verifyUser.publicKeys.isNullOrEmpty()

        //no public keys on backend then we need to generate data
        if(!publicKeysPresent && savedEncryption.isEmpty()) {
            return UserAuthFlow.FIRST_LOGIN
        }  else if (!publicKeysPresent) {
            return UserAuthFlow.LOCAL_KEY_PRESENT_NO_BACKEND_KEYS
        }

        val walletSigners = userRepository.getWalletSigners()

        if(savedEncryption.isEmpty()) {
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
                launchSmartLockRetrieveFlow()
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
            //todo: add loading, exception logic, and corresponding VM state:
            // str-68: https://linear.app/strike-android/issue/STR-68/add-exception-logic-to-initial-auth-data-in-userrepository
            val initialAuthData = userRepository.generateInitialAuthData()
            state = state.copy(
                initialAuthData = initialAuthData
            )
            launchSmartLockDialog()
        }
    }
    //endregion

}
