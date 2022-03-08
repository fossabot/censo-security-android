package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.strikeLog
import com.strikeprotocols.mobile.data.NoInternetException
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.UserAuthFlow
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.UserRepository
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

            val coroutineExceptionHandler = CoroutineExceptionHandler { _, _ ->
                state = state.copy(
                    loginResult = Resource.Error(
                        NoInternetException().message ?: NO_INTERNET_ERROR
                    )
                )
            }

            viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
                val sessionToken = userRepository.retrieveSessionToken(state.email, state.password)
                val token = userRepository.authenticate(sessionToken)
                state = state.copy(loginResult = Resource.Success(token))
            }
        } else {
            state = state.copy(
                emailErrorEnabled = !state.emailValid(),
                passwordErrorEnabled = !state.passwordValid()
            )
        }
    }

    private fun attemptAddWalletSigner(walletSignerBody: WalletSigner) {
        viewModelScope.launch(Dispatchers.IO) {
            val addWalletSignerData = userRepository.addWalletSigner(walletSignerBody = walletSignerBody)
        }
    }

    fun attemptGetWalletSigners() {
        viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(walletSignersResult = Resource.Loading())
            val walletSigners = userRepository.getWalletSigners()
            state = state.copy(walletSignersResult = Resource.Success(walletSigners))
        }
    }

    private fun retrieveUserVerifyDetails() {
        viewModelScope.launch(Dispatchers.IO) {

            val verifyUserData = userRepository.verifyUser()
            strikeLog(message = "Verify User Data: $verifyUserData")
            val userAuthState = getUserAuthFlowState(verifyUserData)
            strikeLog(message = "User Auth Flow: $userAuthState")

            respondToUserAuthFlow(userAuthState)
        }
    }
    //endregion

    //region Reset Resource State
    fun resetLoginCallAndRetrieveUserInformation() {
        state = state.copy(loginResult = Resource.Uninitialized)
        retrieveUserVerifyDetails()
    }

    fun resetSaveCredential() {
        state = state.copy(saveCredential = Resource.Uninitialized)
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

    private fun launchSmartLockSaveFlow() {
        if (state.saveCredential !is Resource.Loading) {
            state = state.copy(saveCredential = Resource.Loading())
        }
    }

    fun setCredentialLocallySaved() {
        viewModelScope.launch {
            userRepository.savePassword()
        }
    }

    fun clearCredential() {
        viewModelScope.launch {
            userRepository.clearSavedPassword()
        }
    }

    fun saveCredentialSuccess() {
        strikeLog(message = "Save credential request success")
        state = state.copy(saveCredential = Resource.Success(Unit))
        viewModelScope.launch {
            state.initialAuthData?.let { safeInitialAuthData ->
                userRepository.addWalletSigner(safeInitialAuthData.walletSignerBody)
            }
        }
    }

    fun retrieveCredentialSuccess(credential: String?) {
        strikeLog(message = "Retrieve credential request: $credential")
        state = state.copy(retrieveCredential = Resource.Success(credential))
    }

    fun saveCredentialFailed(exception: Exception?) {
        strikeLog(message = "Save credential failed: $exception")
        state = state.copy(saveCredential = Resource.Error(
            exception?.message ?: "DEFAULT_SAVE_CREDENTIAL_ERROR"))
    }

    fun retrieveCredentialFailed(exception: Exception?) {
        strikeLog(message = "Retrieve credential failed: $exception")
        state = state.copy(retrieveCredential = Resource.Error(
            exception?.message ?: "DEFAULT_RETRIEVE_CREDENTIAL_ERROR"))

    }
    //endregion

    //region Get Auth Flow State + Respond to Auth Flow State
    private suspend fun getUserAuthFlowState(verifyUser: VerifyUser) : UserAuthFlow {
        val savedEncryption = userRepository.getSavedPassword()
        val publicKeysPresent = !verifyUser.publicKeys.isNullOrEmpty()

        //no public keys on backend then we need to generate data
        if(!publicKeysPresent && savedEncryption.isEmpty()) {
            return UserAuthFlow.FIRST_LOGIN
        }  else if (!publicKeysPresent) {
            //do we need to regenerate data here and send up to backend?
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
        strikeLog(message = "USER AUTH FLOW RECEIVED: $userAuthFlow")
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
                //todo: need to ask Ata what to do here again. Believe it is to regenerate data somehow.
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
            launchSmartLockSaveFlow()
        }
    }
    //endregion

}
