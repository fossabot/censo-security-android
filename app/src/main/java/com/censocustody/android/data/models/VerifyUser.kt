package com.censocustody.android.data.models

data class VerifyUser(
    val fullName: String?,
    val hasApprovalPermission: Boolean?,
    val id: String?,
    val loginName: String?,
    val organization: Organization?,
    val publicKeys: List<WalletPublicKey?>?,
    val useStaticKey: Boolean?,
    val deviceKey: String?
) {
    fun compareAgainstLocalKeys(hashMap: HashMap<String, String>): Boolean {
        if (publicKeys.isNullOrEmpty()) {
            return false
        }

        for (publicKey in publicKeys) {
            val publicKeyInMap =
                hashMap.getOrDefault(publicKey?.convertWalletTypeToLocalType(), "")
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
        for (chain in Chain.values()) {
            //todo: remove solana chain
            if (chain.label() == Chain.solana.label()) continue
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
            Chain.censo -> StoredKeyData.CENSO_KEY
            else -> ""
        }
}
