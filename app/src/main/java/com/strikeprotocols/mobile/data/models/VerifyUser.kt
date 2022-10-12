package com.strikeprotocols.mobile.data.models

data class VerifyUser(
    val fullName: String?,
    val hasApprovalPermission: Boolean?,
    val id: String?,
    val loginName: String?,
    val organization: Organization?,
    val publicKeys: List<WalletPublicKey?>?,
    val useStaticKey: Boolean?
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
            Chain.solana -> StoredKeyData.SOLANA_KEY
            Chain.ethereum -> StoredKeyData.ETHEREUM_KEY
            else -> ""
        }
}
