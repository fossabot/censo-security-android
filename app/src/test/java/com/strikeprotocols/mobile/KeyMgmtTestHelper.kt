package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementFlow
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementInitialData

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
    chain = null
)