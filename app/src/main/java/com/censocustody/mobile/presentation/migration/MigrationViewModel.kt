package com.censocustody.mobile.presentation.migration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.censocustody.mobile.common.BioCryptoUtil.FAIL_ERROR
import com.censocustody.mobile.common.BioPromptReason
import com.censocustody.mobile.common.Resource
import com.censocustody.mobile.data.BioPromptData
import com.censocustody.mobile.data.MigrationRepository
import com.censocustody.mobile.data.models.CipherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

/***
 *
 * Get root seed. Create all data w/ root seed. Send data to backend.
 *
 * Step 1: Get existing root seed: this involves biometry approval
 * Step 2: Save all new data to v3 storage: public keys
 *  - Will require 1 biometry approval
 * Step 3: Send public keys to backend (sign certain ones if needed)
 *  - Depending if backend does not have certain keys, we may need to sign some local keys when sending
 *
 * Kick off by calling retrieveRootSeed()
 *
 * Step 1:
 * retrieveCipherToDecryptV3RootSeed() -> biometryApproved(RETRIEVE_V3_ROOT_SEED) -> retrieveV3RootSeedAndKickOffKeyStorage()
 *
 * Step 2 + 3 same for all migrations:
 * generateAllNecessaryData() -> biometryApproved(SAVE_V3_ROOT_SEED) -> saveRootSeed() -> biometryApproved(SAVE_V3_KEYS) -> savePrivateAndPublicKeys -> makeApiCallToSaveDataWithBackend()
 */

@HiltViewModel
class MigrationViewModel @Inject constructor(
    private val cipherRepository: CipherRepository,
    private val migrationRepository: MigrationRepository
) : ViewModel() {

    var state by mutableStateOf(MigrationState())
        private set

    //region VM SETUP
    fun onStart(initialData: VerifyUserInitialData) {
        if (state.initialData == null) {
            state = state.copy(
                initialData = initialData,
                verifyUser = initialData.verifyUserDetails,
                addWalletSigner = Resource.Loading()
            )
            viewModelScope.launch {
                retrieveRootSeed()
            }
        }
    }

    //region Step 1: Get Existing Root Seed
    private suspend fun retrieveRootSeed() {
        val haveV3RootSeed = migrationRepository.haveV3RootSeed()

        if (haveV3RootSeed) {
            retrieveCipherToDecryptV3RootSeed()
            return
        }

        //have no data to migrate kick user out
        state = state.copy(kickUserOut = true)
    }

    private suspend fun retrieveCipherToDecryptV3RootSeed() {
        val cipher = cipherRepository.getCipherForV3RootSeedDecryption()
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_V3_ROOT_SEED)
            )
        }
    }

    //region Step 2: Save new public keys
    private suspend fun retrieveV3RootSeedAndKickOffKeyStorage(cipher: Cipher) {
        val rootSeed = migrationRepository.retrieveV3RootSeed(cipher)

        if (rootSeed == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        migrationRepository.saveV3PublicKeys(rootSeed = rootSeed)
        makeApiCallToSaveDataWithBackend(rootSeed = rootSeed)
    }
    //endregion


    //region Step 3: Save all data
    private suspend fun makeApiCallToSaveDataWithBackend(rootSeed: ByteArray) {
        val verifyUser = state.verifyUser

        if (verifyUser == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        val walletSignersToAdd =
            migrationRepository.retrieveWalletSignersToUpload(rootSeed)

        //make API call to send up any needed signed keys
        val walletSigner = migrationRepository.migrateSigner(walletSignersToAdd)

        if (walletSigner is Resource.Success) {
            state = state.copy(finishedMigration = true)
        } else if (walletSigner is Resource.Error) {
            state = state.copy(addWalletSigner = walletSigner)
        }
    }
    //endregion

    //region handle all biometry events
    fun biometryApproved(cipher: Cipher) {
        viewModelScope.launch {
            when (state.bioPromptData.bioPromptReason) {
                BioPromptReason.RETRIEVE_V3_ROOT_SEED -> retrieveV3RootSeedAndKickOffKeyStorage(
                    cipher
                )
                else -> {}
            }
        }
    }
    //endregion

    //region utility
    fun retry() {
        viewModelScope.launch {
            state = state.copy(addWalletSigner = Resource.Loading())
            retrieveRootSeed()
        }
    }

    fun biometryFailed(failedReason: Int? = null) {
        if (failedReason != FAIL_ERROR) {
            state = state.copy(showToast = Resource.Success(""))
            retry()
        }
    }

    fun resetKickOut() {
        state = state.copy(kickUserOut = false)
    }

    fun resetShowToast() {
        state = state.copy(showToast = Resource.Uninitialized)
    }

    fun resetAddWalletSignerCall() {
        state = state.copy(
            addWalletSigner = Resource.Uninitialized,
            finishedMigration = false
        )
    }

    fun resetPromptTrigger() {
        state = state.copy(triggerBioPrompt = Resource.Uninitialized)
    }
    //endregion
}