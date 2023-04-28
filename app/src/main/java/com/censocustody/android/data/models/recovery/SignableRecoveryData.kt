package com.censocustody.android.data.models.recovery

sealed class SignableRecoveryData {

    data class Offchain(
        val dataToSend: ByteArray,
        val dataToSign: ByteArray
    ) : SignableRecoveryData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Offchain

            if (!dataToSend.contentEquals(other.dataToSend)) return false
            if (!dataToSign.contentEquals(other.dataToSign)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSend.contentHashCode()
            result = 31 * result + dataToSign.contentHashCode()
            return result
        }
    }


    interface Evm {
        val dataToSign: ByteArray
    }

    data class Ethereum(
        override val dataToSign: ByteArray,
    ) : SignableRecoveryData(), Evm {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Ethereum

            if (!dataToSign.contentEquals(other.dataToSign)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSign.contentHashCode()
            return result
        }
    }

    data class Polygon(
        override val dataToSign: ByteArray
    ) : SignableRecoveryData(), Evm {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Polygon

            if (!dataToSign.contentEquals(other.dataToSign)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = dataToSign.contentHashCode()
            return result
        }
    }
}