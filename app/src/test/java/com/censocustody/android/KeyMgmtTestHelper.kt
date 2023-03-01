package com.censocustody.android

import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.WalletSigner
import com.censocustody.android.presentation.key_management.KeyManagementFlow
import com.censocustody.android.presentation.key_management.KeyManagementInitialData

fun getCreationFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    flow = KeyManagementFlow.KEY_CREATION
)

fun getRecoveryFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    flow = KeyManagementFlow.KEY_RECOVERY
)

fun getValidTestingPhrase() = ExampleMnemonicAndKeys.MNEMONIC

fun getInvalidTestingPhrase() = ExampleMnemonicAndKeys.BAD_MNEMONIC

fun getWalletSigner() = WalletSigner(
    publicKey = ExampleMnemonicAndKeys.PUBLIC_KEY,
    chain = Chain.offchain
)