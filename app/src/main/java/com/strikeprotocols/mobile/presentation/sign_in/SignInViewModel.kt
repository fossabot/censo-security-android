package com.strikeprotocols.mobile.presentation.sign_in

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.NoInternetException
import com.strikeprotocols.mobile.data.NoInternetException.Companion.NO_INTERNET_ERROR
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.data.UserRepository
import kotlinx.coroutines.*

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(SignInState())
        private set

    fun updateEmail(updatedEmail: String) {
        state = state.copy(email = updatedEmail, emailErrorEnabled = false)
    }

    fun updatePassword(updatedPassword: String) {
        state = state.copy(password = updatedPassword, passwordErrorEnabled = false)
    }

    private fun attemptAddWalletSigner(walletSignerBody: WalletSigner) {
        viewModelScope.launch(Dispatchers.IO) {
            //Do not want to make actual call yet.
//            val addWalletSignerData = userRepository.addWalletSigner(walletSignerBody = walletSignerBody)
        }
    }

    fun attemptGetWalletSigners() {
        viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(walletSignersResult = Resource.Loading())
            val walletSigners = userRepository.getWalletSigners()
            state = state.copy(walletSignersResult = Resource.Success(walletSigners))
        }
    }

    private fun attemptVerify() {
        viewModelScope.launch(Dispatchers.IO) {

            val verifyUserData = userRepository.verifyUser()

            if (verifyUserData.publicKeys.isNullOrEmpty()) {
                handleFirstTimeLoginAuthFlow()
            } else {
                //Call repo methods to do next steps
            }
        }
    }

    fun attemptLogin() {
        if (state.signInButtonEnabled) {
            state = state.copy(loginResult = Resource.Loading())

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

    fun resetLoginCall() {
        state = state.copy(loginResult = Resource.Uninitialized)
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

    private suspend fun handleFirstTimeLoginAuthFlow() {
        val keyPair = userRepository.generateKeyPair()

        val randomPassword = userRepository.generateRandomPassword()

        val encryptedPrivateKey = encryptKey(keyPair.second)

        attemptAddWalletSigner(
            walletSignerBody = WalletSigner(
                encryptedKey = encryptedPrivateKey,
                publicKey = keyPair.first,
                walletType = WalletSigner.WALLET_TYPE_SOLANA
            )
        )

        userRepository.saveRandomPasswordToCloud(randomPassword = randomPassword)
    }

    private fun encryptKey(key: String): String {
        return key.reversed()
    }

}
