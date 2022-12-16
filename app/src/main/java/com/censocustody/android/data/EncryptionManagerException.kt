package com.censocustody.android.data

sealed class EncryptionManagerException(val value: String) : Exception(value) {

    class KeyPairGenerationFailedException : EncryptionManagerException(GENERATION_FAILED)
    class PublicKeyRegenerationFailedException : EncryptionManagerException(REGENERATION_FAILED)
    class EncryptionFailedException : EncryptionManagerException(ENCRYPTION_FAILED)
    class DecryptionFailedException : EncryptionManagerException(DECRYPTION_FAILED)
    class VerifyFailedException : EncryptionManagerException(VERIFY_KEY_PAIR_FAILED)
    class SignDataException : EncryptionManagerException(SIGN_DATA_EXCEPTION)

    companion object {
        const val GENERATION_FAILED = "Key pair generation failed"
        const val REGENERATION_FAILED = "Public key regeneration failed"
        const val ENCRYPTION_FAILED = "Encryption failed"
        const val DECRYPTION_FAILED = "Decryption failed"
        const val VERIFY_KEY_PAIR_FAILED = "Verify Key Pair failed"
        const val SIGN_DATA_EXCEPTION = "Sign Data failed"
    }

}