package com.censocustody.android.data.models

data class ShardingParticipant(
    val participantId: String,
    val devicePublicKeys: List<String>
)

data class ShardingPolicy(
    val policyRevisionGuid: String,
    val threshold: Int,
    val participants: List<ShardingParticipant>
)

data class DeviceKeyInfo(
    val key: String,
    val approved: Boolean,
    val bootstrapKey: String?,
)

data class OrgAdminInfo(
    val hasRecoveryContract: Boolean,
    val participantId: String,
    val bootstrapParticipantId: String?,
    val hasPendingOrgRecovery: Boolean,
    val canInitiateOrgRecovery: Boolean
)

data class VerifyUser(
    val fullName: String?,
    val hasApprovalPermission: Boolean?,
    val id: String?,
    val loginName: String?,
    val organization: Organization?,
    val publicKeys: List<WalletPublicKey?>?,
    val deviceKeyInfo: DeviceKeyInfo?,
    val userShardedToPolicyGuid: String?,
    // if this come back as null, then this is the bootstrap user for the org they belong to.
    val shardingPolicy: ShardingPolicy?,
    val canAddSigners: Boolean,
    val orgAdminInfo: OrgAdminInfo?
) {
    fun compareAgainstLocalKeys(hashMap: HashMap<String, String>): Boolean {
        if (publicKeys.isNullOrEmpty()) {
            return false
        }

        for (publicKey in publicKeys) {
            val publicKeyInMap = hashMap.getOrDefault(publicKey?.convertWalletTypeToLocalType(), "")
            if (publicKeyInMap.isEmpty() || publicKey?.key != publicKeyInMap) {
                return false
            }
        }

        return true
    }

    fun determineKeysUserNeedsToUpload(localKeys: List<WalletSigner?>): List<WalletSigner> {
        val walletsSavedBackend = publicKeys?.map { it?.chain } ?: emptyList()
        return localKeys.filter { it != null && it.chain !in walletsSavedBackend }.filterNotNull()
    }

    fun userNeedsToUpdateKeyRegistration(localKeys: List<WalletSigner?>): Boolean {
        //User has no keys saved to public keys
        if (localKeys.isEmpty()) return true

        //which chains has the user saved on the backend?
        val chainsSavedOnBackend = publicKeys?.mapNotNull { it?.chain } ?: emptyList()

        //loop over all chains a user should have, and make sure backend covers them
        for (chain in Chain.chainsWithSigningKeys()) {
            if (chain !in chainsSavedOnBackend) {
                return true
            }
        }

        return false
    }
}

data class Organization(
    val id: String?,
    val name: String?
)

data class WalletPublicKey(
    val key: String?,
    val chain: Chain?
) {
    fun convertWalletTypeToLocalType() =
        when (chain) {
            Chain.bitcoin -> StoredKeyData.BITCOIN_KEY
            Chain.ethereum -> StoredKeyData.ETHEREUM_KEY
            Chain.offchain -> StoredKeyData.OFFCHAIN_KEY
            else -> ""
        }
}
