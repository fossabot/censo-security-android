package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.generateEphemeralPrivateKey
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.SupplyDappInstruction
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.ASSOCIATED_TOKEN_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.EMPTY_KEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_RENT_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYS_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.TOKEN_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.WRAPPED_SOL_MINT
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.*
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_INITIATION
import com.strikeprotocols.mobile.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.io.ByteArrayOutputStream
import java.util.Base64

data class InitiationRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val initiation: MultiSigOpInitiation,
    val requestType: SolanaApprovalRequestType,
    val nonces: List<Nonce>,
    val email: String,
    val opAccountPrivateKey: Ed25519PrivateKeyParameters = generateEphemeralPrivateKey(),
    val dataAccountPrivateKey: Ed25519PrivateKeyParameters? = generateEphemeralPrivateKey()
) : Signable {

    private val signingData : SolanaSigningData =
        when (requestType) {
            is BalanceAccountCreation -> requestType.signingData
            is WithdrawalRequest -> requestType.signingData
            is ConversionRequest -> requestType.signingData
            is SignersUpdate -> requestType.signingData
            is DAppTransactionRequest -> requestType.signingData
            is WrapConversionRequest -> requestType.signingData
            is WalletConfigPolicyUpdate -> requestType.signingData
            is BalanceAccountSettingsUpdate -> requestType.signingData
            is DAppBookUpdate -> requestType.signingData
            is AddressBookUpdate -> requestType.signingData
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
        is BalanceAccountCreation -> 3
        is WithdrawalRequest -> 7
        is ConversionRequest -> 7
        is WrapConversionRequest -> 10
        is SignersUpdate -> 12
        is WalletConfigPolicyUpdate -> 14
        is DAppTransactionRequest -> 16
        is BalanceAccountSettingsUpdate -> 18
        is DAppBookUpdate -> 20
        is AddressBookUpdate -> 22
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

    fun dataAccountPublicKey(): PublicKey {
        if (dataAccountPrivateKey == null) {
            throw Exception("Missing data account private key")
        }
        val publicKeyParams = dataAccountPrivateKey.generatePublicKey()
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

    private fun createDataAccountMeta(): List<AccountMeta> {
        return listOf(
            AccountMeta(
                publicKey = PublicKey(signingData.feePayer),
                isSigner = true,
                isWritable = true
            ),
            AccountMeta(
                publicKey = dataAccountPublicKey(),
                isSigner = true,
                isWritable = true
            )
        )
    }

    private fun createDataAccountTransactionInstructionData() : ByteArray {
        val dataAccountCreationInfo =
            initiation.dataAccountCreationInfo ?: throw Exception("MISSING_DATA_ACCOUNT_CREATION")

        val buffer = ByteArrayOutputStream()
        buffer.writeIntLE(0)
        buffer.writeLongLE(dataAccountCreationInfo.minBalanceForRentExemption)
        buffer.writeLongLE(dataAccountCreationInfo.accountSize)
        buffer.write(signingData.walletProgramId.base58Bytes())

        return buffer.toByteArray()
    }

    private fun createDataAccountInstruction(): TransactionInstruction =
        TransactionInstruction(
            keys = createDataAccountMeta(),
            programId = SYS_PROGRAM_ID,
            data = createDataAccountTransactionInstructionData()
        )

    private fun instructionData() : ByteArray {

        val commonBytes = signingData.commonInitiationBytes()

        when(requestType) {
            is BalanceAccountCreation -> {
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
            is ConversionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.destination.name.sha256HashBytes())
                return buffer.toByteArray()
            }
            is SignersUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
                buffer.write(requestType.signer.combinedBytes())
                return buffer.toByteArray()
            }
            is DAppTransactionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.dappInfo.address.base58Bytes())
                buffer.write(requestType.dappInfo.name.sha256HashBytes())
                buffer.write(byteArrayOf(requestType.instructions.sumOf { it.instructions.size }.toByte()))
                return buffer.toByteArray()
            }
            is WrapConversionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(byteArrayOf(requestType.symbolAndAmountInfo.symbolInfo.getSOLProgramValue()))
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
            is AddressBookUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                return buffer.toByteArray()
            }
            is DAppBookUpdate -> {
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

                requestType.instructions.withIndex().map { (index, instructionBatch) ->
                    SupplyDappInstruction(
                        nonce = nonces[index + 1],
                        nonceAccountAddress =
                            requestType.signingData.nonceAccountAddresses[index + 1],
                        instructionBatch = instructionBatch,
                        signingData = signingData,
                        opAccountPublicKey = opAccountPublicKey(),
                        dataAccountPublicKey = dataAccountPublicKey(),
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
                    tokenMintAddress = requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress,
                    approverPublicKey = approverPublicKey
                )
            }
            is ConversionRequest -> {
                getTransferAndConversionAccounts(
                    sourceAddress = requestType.account.address!!,
                    destinationAddress = requestType.destination.address,
                    tokenMintAddress = requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress,
                    approverPublicKey = approverPublicKey
                )
            }
            is WrapConversionRequest -> {
                if(requestType.account.address == null) {
                    throw Exception("MISSING_SOURCE_KEY")
                }

                val sourcePublicKey = PublicKey(requestType.account.address)
                val sourceTokenPublicKey = PublicKey.tokenAddress(
                        wallet = sourcePublicKey,
                        tokenMint = WRAPPED_SOL_MINT
                    )

                val isUnwrap = requestType.destinationSymbolInfo.symbol == "SOL"
                listOfNotNull(
                    AccountMeta(opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
                    AccountMeta(sourcePublicKey, isSigner = false, isWritable = true),
                    AccountMeta(sourceTokenPublicKey, isSigner = false, isWritable = true),
                    AccountMeta(WRAPPED_SOL_MINT, isSigner = false, isWritable = false),
                    AccountMeta(approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                    AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = false),
                    if (isUnwrap) {
                        AccountMeta(
                            PublicKey.findProgramAddress(
                                listOf(
                                    b64Decoder.decode(signingData.walletGuidHash),
                                    opAccountPublicKey().bytes
                                ), PublicKey(signingData.walletProgramId)
                            ).address,
                            isSigner = false,
                            isWritable = true
                        )
                    } else null,
                    AccountMeta(SYS_PROGRAM_ID, isSigner = false, isWritable = false),
                    AccountMeta(TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
                    AccountMeta(SYSVAR_RENT_PUBKEY, isSigner = false, isWritable = false),
                    AccountMeta(ASSOCIATED_TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
                    if (isUnwrap) {
                        AccountMeta(
                            PublicKey.findProgramAddress(
                                listOf(
                                    b64Decoder.decode(signingData.walletGuidHash),
                                    b64Decoder.decode(signingData.feeAccountGuidHash),
                                ),
                                PublicKey(signingData.walletProgramId)
                            ).address,
                            isSigner = false,
                            isWritable = true
                        )
                    } else null,
                )
            }
            is DAppTransactionRequest -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = dataAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
                    AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                    AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = false)
                )
            }
            else -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
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
            AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
            AccountMeta(publicKey = sourcePublicKey, isSigner = false, isWritable = true),
            AccountMeta(publicKey = destinationPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
            AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
            AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = true),
            AccountMeta(publicKey = tokenMintPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = destinationTokenAddress, isSigner = false, isWritable = true),
            AccountMeta(publicKey = SYS_PROGRAM_ID, isSigner= false, isWritable = false),
            AccountMeta(publicKey = TOKEN_PROGRAM_ID, isSigner = false, isWritable = false),
            AccountMeta(publicKey = SYSVAR_RENT_PUBKEY, isSigner = false, isWritable = false),
            AccountMeta(publicKey = ASSOCIATED_TOKEN_PROGRAM_ID, isSigner = false, isWritable = false)
        )
    }

    fun convertToApiBody(encryptionManager: EncryptionManager): InitiateRequestBody {
        val initiatorSignature = try {
            encryptionManager.signApprovalDispositionMessage(
                signable = this,
                userEmail = email,
            )
        } catch (e: Exception) {
            throw Exception("SIGNING_DATA_FAILURE")
        }

        val nonce = nonces.firstOrNull()?.value ?: ""
        val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull() ?: ""

        val opAccountSignature = try {
            encryptionManager.signApprovalInitiationMessage(
                ephemeralPrivateKey = opAccountPrivateKey.encoded,
                signable = this,
                userEmail = email
            )
        } catch (e: Exception) {
            throw Exception("SIGNING_DATA_FAILURE")
        }

        val opAccountAddress = opAccountPublicKey().toBase58()

        val supplyDappInstruction =
            if (dataAccountPrivateKey != null && initiation.dataAccountCreationInfo != null) {
                val dataAccountAddress = dataAccountPublicKey().toBase58()
                val dataAccountSignature = encryptionManager.signApprovalInitiationMessage(
                    ephemeralPrivateKey = dataAccountPrivateKey.encoded,
                    signable = this,
                    userEmail = email
                )

                val supplyInstructionInitiatorSignatures = supplyInstructions.map { instruction ->
                    SupplyDappInstructionsTxSignature(
                        nonce = instruction.nonce.value,
                        nonceAccountAddress = instruction.nonceAccountAddress,
                        signature =
                        encryptionManager.signApprovalDispositionMessage(
                            signable = instruction, userEmail = email
                        )
                    )
                }

                SupplyDAppInstructions(
                    dataAccountAddress = dataAccountAddress,
                    dataAccountSignature = dataAccountSignature,
                    supplyInstructionInitiatorSignatures = supplyInstructionInitiatorSignatures
                )
            } else {
                null
            }

        return InitiateRequestBody(
            approvalDisposition = approvalDisposition,
            nonce = nonce,
            nonceAccountAddress = nonceAccountAddress,
            initiatorSignature = initiatorSignature,
            opAccountSignature = opAccountSignature,
            opAccountAddress = opAccountAddress,
            supplyDappInstructions = supplyDappInstruction
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

    inner class SupplyDAppInstructions(
        val dataAccountAddress: String,
        val dataAccountSignature: String,
        val supplyInstructionInitiatorSignatures: List<SupplyDappInstructionsTxSignature>
    )

    inner class SupplyDappInstructionsTxSignature(
        val nonce: String,
        val nonceAccountAddress: String,
        val signature: String
    )

    inner class OpAccountSignatureInfo(
        val signature: String,
        val publicKey: String
    )

    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
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

        if(initiation.dataAccountCreationInfo != null) {
            instructions.add(createDataAccountInstruction())
        }

        instructions.add(
            TransactionInstruction(
                keys = instructionAccountMeta(approverPublicKey = PublicKey(approverPublicKey)),
                programId = PublicKey(signingData.walletProgramId),
                data = instructionData()
            )
        )

        return Transaction.compileMessage(
            feePayer = PublicKey(signingData.feePayer),
            recentBlockhash = nonce.value,
            instructions = instructions
        ).serialize()
    }
}