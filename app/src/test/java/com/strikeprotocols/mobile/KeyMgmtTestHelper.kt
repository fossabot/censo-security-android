package com.strikeprotocols.mobile

import com.strikeprotocols.mobile.common.GeneralDummyData
import com.strikeprotocols.mobile.data.models.WalletSigner
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementFlow
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementInitialData

fun getCreationFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    walletSigners = listOf(),
    flow = KeyManagementFlow.KEY_CREATION
)

fun getRecoveryFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    walletSigners = listOf(),
    flow = KeyManagementFlow.KEY_RECOVERY
)

fun getRegenerationFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    walletSigners = listOf(),
    flow = KeyManagementFlow.KEY_REGENERATION
)

fun getMigrationFlowInitialData() = KeyManagementInitialData(
    verifyUserDetails = getVerifyUser(),
    walletSigners = listOf(),
    flow = KeyManagementFlow.KEY_MIGRATION
)

fun getValidTestingPhrase() =
    "clerk wolf hover poverty salmon rough write any rigid horror sing air super misery critic grain dolphin again milk ocean fragile unveil boss random"

fun getInvalidTestingPhrase() =
    "clerk wolf jenkins poverty salmon rough write any rigid horror sing air parser misery critic grain dolphin again milk ocean fragile unveil boss random"

fun getWalletSigner() = WalletSigner(
    publicKey = GeneralDummyData.PhraseDummyData.PHRASE_PUBLIC_KEY,
    walletType = ""
)