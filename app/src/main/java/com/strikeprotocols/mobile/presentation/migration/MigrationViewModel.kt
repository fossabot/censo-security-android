package com.strikeprotocols.mobile.presentation.migration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.strikeprotocols.mobile.common.BioCryptoUtil.FAIL_ERROR
import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.data.BioPromptData
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.PRIVATE_KEYS_KEY_NAME
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.ROOT_SEED_KEY_NAME
import com.strikeprotocols.mobile.data.MigrationRepository
import com.strikeprotocols.mobile.data.models.CipherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

/***
 *
 * Get root seed. Create all data w/ root seed. Send data to backend.
 *
 * Step 1: Get existing root seed
 *  - This involves biometry approval for v2 and v3 cases.
 * Step 2: Save all new data to v3 storage: root seed, private keys, public keys
 *  - Will require 2 biometry approvals
 * Step 3: Send public keys to backend (sign certain ones if needed)
 *  - Depending if backend does not have certain keys, we may need to sign some local keys when sending
 *
 * Kick off by calling retrieveRootSeed()
 *
 * Step 1:
 * V1: No need for biometry
 * V2: retrieveCipherToDecryptV2RootSeed() -> biometryApproved(RETRIEVE_V2_KEYS) -> retrieveV2KeyDataAndKickOffKeyStorage()
 * V3: retrieveCipherToDecryptV3RootSeed() -> biometryApproved(RETRIEVE_V3_ROOT_SEED) -> retrieveV3RootSeedAndKickOffKeyStorage()
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
                verifyUser = state.verifyUser,
                addWalletSigner = Resource.Loading()
            )
            viewModelScope.launch {
                retrieveRootSeed()
            }
        }
    }

    //region Step 1: Get Existing Root Seed
    private suspend fun retrieveRootSeed() {
        val v1RootSeed = migrationRepository.retrieveV1RootSeed()

        if (v1RootSeed != null) {
            state = state.copy(rootSeed = v1RootSeed.toList())
            generateAllNecessaryData(isImmediate = false)
            return
        }

        val haveV2RootSeed = migrationRepository.haveV2RootSeed()

        if (haveV2RootSeed) {
            retrieveCipherToDecryptV2RootSeed()
            return
        }

        val haveV3RootSeed = migrationRepository.haveV3RootSeed()

        if (haveV3RootSeed) {
            retrieveCipherToDecryptV3RootSeed()
            return
        }

        //have no data to migrate kick user out
        state = state.copy(kickUserOut = true)
    }

    //start step 1 for v2 migration
    private suspend fun retrieveCipherToDecryptV2RootSeed() {
        //look in biometryApproved to see where we retrieve root seed then move onto Step 2
        val cipher = cipherRepository.getCipherForV2KeysDecryption()
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_V2_KEYS, false)
            )
        }
    }

    private suspend fun retrieveCipherToDecryptV3RootSeed() {
        val cipher = cipherRepository.getCipherForV3RootSeedDecryption()
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptData = BioPromptData(BioPromptReason.RETRIEVE_V3_ROOT_SEED, false)
            )
        }
    }

    //finish step 1 and kick off step 2
    private suspend fun retrieveV2KeyDataAndKickOffKeyStorage(cipher: Cipher) {
        val rootSeed = migrationRepository.retrieveV2RootSeed(cipher)
        state = state.copy(rootSeed = rootSeed?.toList())
        generateAllNecessaryData()
    }

    private suspend fun retrieveV3RootSeedAndKickOffKeyStorage(cipher: Cipher) {
        val rootSeed = migrationRepository.retrieveV3RootSeed(cipher)
        state = state.copy(rootSeed = rootSeed?.toList())
        generateAllNecessaryData()
    }
    //endregion


    //region Step 2: Generate all data and send to backend. We will have a root seed at this point.
    // 1. Get cipher to save root seed
    // 2. Save root seed and go get cipher to save private keys
    // 3. Save private and public keys
    private suspend fun generateAllNecessaryData(isImmediate: Boolean = true) {
        //step 1: save root seed
        val cipher = cipherRepository.getCipherForEncryption(ROOT_SEED_KEY_NAME)
        if (cipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(cipher),
                bioPromptData = BioPromptData(
                    BioPromptReason.SAVE_V3_ROOT_SEED,
                    immediate = isImmediate
                )
            )
        }
    }

    private suspend fun saveRootSeed(rootSeedCipher: Cipher) {
        migrationRepository.saveV3RootSeed(
            rootSeed = state.rootSeed?.toByteArray() ?: byteArrayOf(),
            cipher = rootSeedCipher
        )

        val privateKeysCipher = cipherRepository.getCipherForEncryption(PRIVATE_KEYS_KEY_NAME)
        if (privateKeysCipher != null) {
            state = state.copy(
                triggerBioPrompt = Resource.Success(privateKeysCipher),
                bioPromptData = BioPromptData(BioPromptReason.SAVE_V3_ROOT_SEED, true)
            )
        }
    }

    private suspend fun savePrivateAndPublicKeys(cipher: Cipher) {
        migrationRepository.saveV3PrivateKeys(
            rootSeed = state.rootSeed?.toByteArray() ?: byteArrayOf(),
            cipher = cipher
        )

        migrationRepository.saveV3PublicKeys(
            rootSeed = state.rootSeed?.toByteArray() ?: byteArrayOf()
        )

        migrationRepository.clearOldData()

        makeApiCallToSaveDataWithBackend()
    }
    //endregion

    //region Step 3: Save all data
    private suspend fun makeApiCallToSaveDataWithBackend() {
        val rootSeed = state.rootSeed
        val verifyUser = state.verifyUser

        if (rootSeed == null || verifyUser == null) {
            state = state.copy(addWalletSigner = Resource.Error())
            return
        }

        val walletSignersToAdd = migrationRepository.retrieveWalletSignersToUpload(
            rootSeed = rootSeed.toByteArray(),
            verifyUser = verifyUser
        )

        //send up wallet signers to backend
        //make API call to send up bitcoin signed key
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
                BioPromptReason.RETRIEVE_V2_KEYS -> retrieveV2KeyDataAndKickOffKeyStorage(cipher)
                BioPromptReason.RETRIEVE_V3_ROOT_SEED -> retrieveV3RootSeedAndKickOffKeyStorage(cipher)
                BioPromptReason.SAVE_V3_ROOT_SEED -> saveRootSeed(cipher)
                BioPromptReason.SAVE_V3_KEYS -> savePrivateAndPublicKeys(cipher)
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

    fun cleanUp() {
        state = state.copy(rootSeed = null)
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