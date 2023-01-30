package com.censocustody.android.data.models.approval

import androidx.biometric.BiometricPrompt
import com.censocustody.android.common.BaseWrapper
import com.censocustody.android.data.EncryptionManager
import com.censocustody.android.data.Signable
import com.censocustody.android.data.SignedInitiationData
import com.censocustody.android.data.generateEphemeralPrivateKey
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.data.models.SupplyDappInstruction
import com.censocustody.android.data.models.approval.PublicKey.Companion.ASSOCIATED_TOKEN_PROGRAM_ID
import com.censocustody.android.data.models.approval.PublicKey.Companion.EMPTY_KEY
import com.censocustody.android.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.censocustody.android.data.models.approval.PublicKey.Companion.SYSVAR_RENT_PUBKEY
import com.censocustody.android.data.models.approval.PublicKey.Companion.SYS_PROGRAM_ID
import com.censocustody.android.data.models.approval.PublicKey.Companion.TOKEN_PROGRAM_ID
import com.censocustody.android.data.models.approval.PublicKey.Companion.WRAPPED_SOL_MINT
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.*
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.Companion.UNKNOWN_INITIATION
import com.censocustody.android.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.io.ByteArrayOutputStream

data class InitiationRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val initiation: MultiSigOpInitiation,
    val requestType: ApprovalRequestDetails,
    val nonces: List<Nonce>,
    val email: String,
    val opAccountPrivateKey: Ed25519PrivateKeyParameters = generateEphemeralPrivateKey()
) : Signable {

    private val signingData : SigningData.SolanaSigningData =
        when (requestType) {
            is WalletCreation -> requestType.signingData!!
            is WithdrawalRequest -> requestType.signingData as SigningData.SolanaSigningData
            is DAppTransactionRequest -> requestType.signingData
            is WalletConfigPolicyUpdate -> requestType.signingData
            is BalanceAccountSettingsUpdate -> requestType.signingData
            is CreateAddressBookEntry -> requestType.signingData!!
            is DeleteAddressBookEntry -> requestType.signingData!!
            is BalanceAccountNameUpdate -> requestType.signingData
            is BalanceAccountPolicyUpdate -> requestType.signingData
            is BalanceAccountAddressWhitelistUpdate -> requestType.signingData
            is LoginApprovalRequest,
            is AcceptVaultInvitation,
            is PasswordReset,
            is UnknownApprovalType ->
                throw Exception(UNKNOWN_INITIATION)
        }

    private val opCode : Byte = when(requestType) {
        is WalletCreation -> 3
        is WithdrawalRequest -> 7
        is WalletConfigPolicyUpdate -> 14
        is DAppTransactionRequest -> 16
        is BalanceAccountSettingsUpdate -> 18
        is CreateAddressBookEntry, is DeleteAddressBookEntry -> 22
        is BalanceAccountNameUpdate -> 24
        is BalanceAccountPolicyUpdate -> 26
        is BalanceAccountAddressWhitelistUpdate -> 33
        is UnknownApprovalType,
        is AcceptVaultInvitation,
        is PasswordReset,
        is LoginApprovalRequest -> 0
    }

    private val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    fun opAccountPublicKey(): PublicKey {
        val publicKeyParams = opAccountPrivateKey.generatePublicKey()
        val publicKeyAsBase58 = BaseWrapper.encode(publicKeyParams.encoded)
        return PublicKey(publicKeyAsBase58)
    }

    private fun createOpAccountMeta(): List<AccountMeta> {
        return listOf(
            AccountMeta(
                publicKey = PublicKey(signingData.feePayer),
                isSigner = true,
                isWritable = true
            ),
            AccountMeta(
                publicKey = opAccountPublicKey(),
                isSigner = true,
                isWritable = true
            )
        )
    }

    private fun createOpAccounTransactionInstructionData(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.writeIntLE(0)
        buffer.writeLongLE(initiation.opAccountCreationInfo.minBalanceForRentExemption)
        buffer.writeLongLE(initiation.opAccountCreationInfo.accountSize)
        buffer.write(signingData.walletProgramId.base58Bytes())
        return buffer.toByteArray()
    }

    private fun createOpAccountInstruction() : TransactionInstruction =
        TransactionInstruction(
            keys = createOpAccountMeta(),
            programId = SYS_PROGRAM_ID,
            data = createOpAccounTransactionInstructionData()
        )

    private fun instructionData() : ByteArray {

        val commonBytes = signingData.commonInitiationBytes()

        signingData.base64DataToSign?.let {
            return@instructionData SignDataHelper.serializeSignData(it, commonBytes, 35)
        }

        when(requestType) {
            is WalletCreation -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is WithdrawalRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.destination.name.sha256HashBytes())
                return buffer.toByteArray()
            }
            is DAppTransactionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.dappInfo.address.base58Bytes())
                buffer.write(requestType.dappInfo.name.sha256HashBytes())
                buffer.writeShortLE(requestType.instructions.sumOf { it.decodedData().size }.toShort())
                return buffer.toByteArray()
            }
            is BalanceAccountPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is BalanceAccountNameUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is BalanceAccountSettingsUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is BalanceAccountAddressWhitelistUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is WalletConfigPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.approvalPolicy.combinedBytes())
                return buffer.toByteArray()
            }
            is CreateAddressBookEntry -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is DeleteAddressBookEntry -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is LoginApprovalRequest, is AcceptVaultInvitation, is UnknownApprovalType, is PasswordReset -> {
                throw Exception("Unknown Approval")
            }
        }
    }

    val supplyInstructions: List<SupplyDappInstruction> =
        if (requestType is DAppTransactionRequest) {
            if (requestType.instructions.size + 1 == nonces.size
                && nonces.size == requestType.signingData.nonceAccountAddresses.size) {

                requestType.instructions.withIndex().map { (index, instructionChunk) ->
                    SupplyDappInstruction(
                        nonce = nonces[index + 1],
                        nonceAccountAddress =
                            requestType.signingData.nonceAccountAddresses[index + 1],
                        instructionChunk = instructionChunk,
                        signingData = signingData,
                        opAccountPublicKey = opAccountPublicKey(),
                        walletAccountPublicKey = PublicKey(requestType.signingData.walletAddress),
                    )
                }
            } else {
                throw Exception("Nonce count does not match instruction count")
            }
        } else {
            emptyList()
        }

    private fun instructionAccountMeta(approverPublicKey: PublicKey) : List<AccountMeta> =
        when(requestType) {
            is WithdrawalRequest -> {
                getTransferAndConversionAccounts(
                    sourceAddress = requestType.account.address!!,
                    destinationAddress = requestType.destination.address,
                    tokenMintAddress = requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress!!,
                    approverPublicKey = approverPublicKey
                )
            }
            is DAppTransactionRequest -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                    AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = false)
                )
            }
            else -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                    AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = false)
                )
            }
        }

    private fun getTransferAndConversionAccounts(
        sourceAddress: String,
        destinationAddress: String,
        tokenMintAddress: String,
        approverPublicKey: PublicKey
    ): List<AccountMeta> {
        val sourcePublicKey = PublicKey(sourceAddress)
        val tokenMintPublicKey = PublicKey(tokenMintAddress)
        val destinationPublicKey = PublicKey(destinationAddress)
        var destinationTokenAddress = EMPTY_KEY
        if (tokenMintPublicKey != EMPTY_KEY) {
            destinationTokenAddress =
                PublicKey.tokenAddress(
                    wallet = destinationPublicKey,
                    tokenMint = tokenMintPublicKey
                )
        }

        return listOf(
            AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
            AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = true),
            AccountMeta(publicKey = sourcePublicKey, isSigner = false, isWritable = true),
            AccountMeta(publicKey = destinationPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
            AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
            AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = true),
            AccountMeta(publicKey = tokenMintPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = destinationTokenAddress, isSigner = false, isWritable = tokenMintPublicKey != EMPTY_KEY),
            AccountMeta(publicKey = SYS_PROGRAM_ID, isSigner= false, isWritable = false),
            AccountMeta(publicKey = TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = SYSVAR_RENT_PUBKEY, isSigner = false, isWritable = false),
            AccountMeta(publicKey = ASSOCIATED_TOKEN_PROGRAM_ID, isSigner = false, isWritable = false)
        )
    }

    fun convertToApiBody(
        encryptionManager: EncryptionManager, cryptoObject: BiometricPrompt.CryptoObject
    ): InitiateRequestBody {

        if (cryptoObject.cipher == null) {
            throw Exception("Missing cipher to sign data")
        }

        val initiationSignedData : SignedInitiationData =
            encryptionManager.signInitiationRequestData(
                signable = this,
                email = email,
                ephemeralPrivateKey = opAccountPrivateKey.encoded,
                supplyInstructions = supplyInstructions,
                cipher = cryptoObject.cipher!!
            )

        val nonce = nonces.firstOrNull()?.value ?: ""
        val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull() ?: ""

        val opAccountAddress = opAccountPublicKey().toBase58()

        return InitiateRequestBody(
            approvalDisposition = approvalDisposition,
            nonce = nonce,
            nonceAccountAddress = nonceAccountAddress,
            initiatorSignature = initiationSignedData.initiatorSignature,
            opAccountSignature = initiationSignedData.opAccountSignature,
            opAccountAddress = opAccountAddress,
            supplyDappInstructions = initiationSignedData.supplyDAppInstructions
        )
    }

    inner class InitiateRequestBody(
        val approvalDisposition: ApprovalDisposition,
        val initiatorSignature: String,
        val nonce: String,
        val nonceAccountAddress: String,
        val opAccountAddress: String,
        val opAccountSignature: String,
        val supplyDappInstructions: SupplyDAppInstructions?
    )

    class SupplyDAppInstructions(
        val supplyInstructionInitiatorSignatures: List<SupplyDappInstructionsTxSignature>
    )

    class SupplyDappInstructionsTxSignature(
        val nonce: String,
        val nonceAccountAddress: String,
        val signature: String
    )

    inner class OpAccountSignatureInfo(
        val signature: String,
        val publicKey: String
    )

    override fun retrieveSignableData(approverPublicKey: String?): List<ByteArray> {
        if(approverPublicKey == null) {
            throw Exception("MISSING_APPROVER_KEY")
        }

        val nonce = nonces.firstOrNull()
        val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull()

        if (nonce == null || nonceAccountAddress == null) {
            throw Exception("NOT_ENOUGH_NONCE_ACCOUNTS")
        }

        val instructions = mutableListOf<TransactionInstruction>()

        instructions.add(
            createAdvanceNonceInstruction(
                nonceAccountAddress = nonceAccountAddress,
                feePayer = signingData.feePayer
            )
        )

        instructions.add(createOpAccountInstruction())

        instructions.add(
            TransactionInstruction(
                keys = instructionAccountMeta(approverPublicKey = PublicKey(approverPublicKey)),
                programId = PublicKey(signingData.walletProgramId),
                data = instructionData()
            )
        )

        return listOf(Transaction.compileMessage(
            feePayer = PublicKey(signingData.feePayer),
            recentBlockhash = nonce.value,
            instructions = instructions
        ).serialize())
    }
}
