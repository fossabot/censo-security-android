package com.strikeprotocols.mobile.data.models.approval

import com.strikeprotocols.mobile.common.BaseWrapper
import com.strikeprotocols.mobile.data.EncryptionManager
import com.strikeprotocols.mobile.data.Signable
import com.strikeprotocols.mobile.data.SignableSupplyInstructions
import com.strikeprotocols.mobile.data.generateEphemeralPrivateKey
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.ASSOCIATED_TOKEN_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.EMPTY_KEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYSVAR_RENT_PUBKEY
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.SYS_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.PublicKey.Companion.TOKEN_PROGRAM_ID
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestType.Companion.UNKNOWN_REQUEST_APPROVAL
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionError
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.io.ByteArrayOutputStream
import java.util.*

data class InitiationRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val initiation: MultiSigOpInitiation,
    val requestType: SolanaApprovalRequestType,
    val blockhash: String,
    val email: String,
) : Signable, SignableSupplyInstructions {
    private val opAccountPrivateKey: Ed25519PrivateKeyParameters = generateEphemeralPrivateKey()
    private val dataAccountPrivateKey: Ed25519PrivateKeyParameters = generateEphemeralPrivateKey()

    private val signingData : SolanaSigningData =
        when (requestType) {
            is SolanaApprovalRequestType.BalanceAccountCreation -> requestType.signingData
            is SolanaApprovalRequestType.WithdrawalRequest -> requestType.signingData
            is SolanaApprovalRequestType.ConversionRequest -> requestType.signingData
            is SolanaApprovalRequestType.SignersUpdate -> requestType.signingData
            is SolanaApprovalRequestType.DAppTransactionRequest -> requestType.signingData
            is SolanaApprovalRequestType.UnknownApprovalType -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }

    private val opCode : Byte = when(requestType) {
        is SolanaApprovalRequestType.BalanceAccountCreation -> 3
        is SolanaApprovalRequestType.WithdrawalRequest -> 7
        is SolanaApprovalRequestType.ConversionRequest -> 7
        is SolanaApprovalRequestType.SignersUpdate -> 12
        is SolanaApprovalRequestType.DAppTransactionRequest -> 16
        else -> 0
    }

    private val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    fun opAccountPublicKey(): PublicKey {
        val publicKeyParams = opAccountPrivateKey.generatePublicKey()
        val publicKeyAsBase58 = BaseWrapper.encode(publicKeyParams.encoded)
        return PublicKey(publicKeyAsBase58)
    }

    fun dataAccountPublicKey(): PublicKey {
        val publicKeyParams = dataAccountPrivateKey.generatePublicKey()
        val publicKeyAsBase58 = BaseWrapper.encode(publicKeyParams.encoded)
        return PublicKey(publicKeyAsBase58)
    }

    fun createOpAccountMeta(): List<AccountMeta> {
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

    fun createOpAccounTransactionInstructionData(): ByteArray {
        val buffer = ByteArrayOutputStream()
        //todo: Do not think this is best way to handle this
        buffer.write(Int.MIN_VALUE)
        buffer.writeLongLE(initiation.opAccountCreationInfo.minBalanceForRentExemption)
        buffer.writeLongLE(initiation.opAccountCreationInfo.accountSize)
        buffer.write(signingData.walletProgramId.base58Bytes())
        return buffer.toByteArray()
    }

    fun createOpAccountInstruction() : TransactionInstruction =
        TransactionInstruction(
            keys = createOpAccountMeta(),
            programId = SYS_PROGRAM_ID,
            data = createOpAccounTransactionInstructionData()
        )

    fun createDataAccountMeta(): List<AccountMeta> {
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

    fun createDataAccountTransactionInstructionData() : ByteArray {
        val dataAccountCreationInfo =
            initiation.dataAccountCreationInfo ?: throw Exception("Missing data account creation")

        val buffer = ByteArrayOutputStream()
        //todo: is this the same as UInt32(0)
        buffer.write(Int.MIN_VALUE)
        buffer.writeLongLE(dataAccountCreationInfo.minBalanceForRentExemption)
        buffer.writeLongLE(dataAccountCreationInfo.accountSize)
        buffer.write(signingData.walletProgramId.base58Bytes())

        return buffer.toByteArray()
    }

    fun createDataAccountInstruction(): TransactionInstruction =
        TransactionInstruction(
            keys = createDataAccountMeta(),
            programId = SYS_PROGRAM_ID,
            data = createDataAccountTransactionInstructionData()
        )

    fun instructionData() : ByteArray {
        when(requestType) {
            is SolanaApprovalRequestType.BalanceAccountCreation -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(requestType.accountInfo.identifier.sha256HashBytes())
                buffer.write(byteArrayOf(requestType.accountSlot))
                buffer.write(requestType.accountInfo.name.sha256HashBytes())
                buffer.write(byteArrayOf(requestType.approvalsRequired))
                buffer.writeLongLE(requestType.approvalTimeout.convertToSeconds())
                buffer.write(byteArrayOf(requestType.approvers.size.toByte()))
                buffer.write(requestType.approvers.flatMap { it.combinedBytes().toList() }.toByteArray())
                buffer.write(byteArrayOf(requestType.whitelistEnabled.toSolanaProgramValue()))
                buffer.write(byteArrayOf(requestType.dappsEnabled.toSolanaProgramValue()))
                buffer.write(byteArrayOf(requestType.addressBookSlot))
                return buffer.toByteArray()
            }
            is SolanaApprovalRequestType.WithdrawalRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.destination.name.sha256HashBytes())
                return buffer.toByteArray()
            }
            is SolanaApprovalRequestType.ConversionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.destination.name.sha256HashBytes())
                return buffer.toByteArray()
            }
            is SolanaApprovalRequestType.SignersUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
                buffer.write(requestType.signer.combinedBytes())
                return buffer.toByteArray()
            }
            is SolanaApprovalRequestType.DAppTransactionRequest -> {
                byteArrayOf(requestType.instructions.sumOf { it.instructions.size }.toByte())
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(opCode))
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.dappInfo.address.base58Bytes())
                buffer.write(requestType.dappInfo.name.sha256HashBytes())
                //todo: Ask ATA if this is what was happenning in the iOS code
                //([UInt8(request.instructions.map { $0.instructions.count }.reduce(0, +))])
                byteArrayOf(requestType.instructions.sumOf { it.instructions.size }.toByte())
                return buffer.toByteArray()
            }
            else -> throw Exception(UNKNOWN_REQUEST_APPROVAL)
        }
    }

    val supplyInstructions: List<SolanaInstructionBatch> =
        if(requestType is SolanaApprovalRequestType.DAppTransactionRequest) {
            requestType.instructions
        } else {
            emptyList()
        }

    fun instructionAccountMeta(approverPublicKey: PublicKey) : List<AccountMeta> =
        when(requestType) {
            is SolanaApprovalRequestType.WithdrawalRequest -> {
                getTransferAndConversionAccounts(
                    sourceAddress = requestType.account.address!!,
                    destinationAddress = requestType.destination.address,
                    tokenMintAddress = requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress,
                    approverPublicKey = approverPublicKey
                )
            }
            is SolanaApprovalRequestType.ConversionRequest -> {
                getTransferAndConversionAccounts(
                    sourceAddress = requestType.account.address!!,
                    destinationAddress = requestType.destination.address,
                    tokenMintAddress = requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress,
                    approverPublicKey = approverPublicKey
                )
            }
            is SolanaApprovalRequestType.DAppTransactionRequest -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = dataAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
                    AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                )
            }
            else -> {
                listOf(
                    AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                    AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
                    AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
                    AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
                )
            }
        }

    fun getTransferAndConversionAccounts(
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
            AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = false),
            AccountMeta(publicKey = PublicKey(signingData.walletAddress), isSigner = false, isWritable = false),
            AccountMeta(publicKey = sourcePublicKey, isSigner = false, isWritable = true),
            AccountMeta(publicKey = destinationPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = approverPublicKey, isSigner = true, isWritable = false),
            AccountMeta(publicKey = SYSVAR_CLOCK_PUBKEY, isSigner = false, isWritable = false),
            AccountMeta(publicKey = tokenMintPublicKey, isSigner = false, isWritable = false),
            AccountMeta(publicKey = destinationTokenAddress, isSigner = false, isWritable = true),
            AccountMeta(publicKey = PublicKey(signingData.feePayer), isSigner = true, isWritable = true),
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
            throw Exception(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
        }

        val opAccountSignature = try {
            encryptionManager.signApprovalInitiationMessage(
                ephemeralPrivateKey = opAccountPrivateKey.encoded,
                signable = this, userEmail = email
            )
        } catch (e: Exception) {
            throw Exception(ApprovalDispositionError.SIGNING_DATA_FAILURE.error)
        }

        val opAccountSignatureInfo = OpAccountSignatureInfo(
            signature = opAccountSignature,
            publicKey = opAccountPublicKey().toBase58()
        )

        var supplyInstructionInitiatorSignatures : List<String> = emptyList()
        var dataAccountSignatureInfo : String? = null

        if(initiation.dataAccountCreationInfo != null) {
            supplyInstructionInitiatorSignatures =
                encryptionManager.signatures(userEmail = email, signableSupplyInstructions = this)
            dataAccountSignatureInfo = encryptionManager.signApprovalInitiationMessage(
                ephemeralPrivateKey = dataAccountPrivateKey.encoded,
                signable = this,
                userEmail = email
            )

        }

        return InitiateRequestBody(
            approvalDisposition = approvalDisposition,
            recentBlockhash = blockhash,
            initiatorSignature = initiatorSignature,
            dataAccountSignatureInfo = dataAccountSignatureInfo,
            opAccountSignatureInfo = opAccountSignatureInfo,
            supplyInstructionInitiatorSignatures = supplyInstructionInitiatorSignatures
        )
    }

    inner class InitiateRequestBody(
        val approvalDisposition: ApprovalDisposition,
        val initiatorSignature: String,
        val dataAccountSignatureInfo: String? = null,
        val opAccountSignatureInfo: OpAccountSignatureInfo,
        val recentBlockhash: String,
        val supplyInstructionInitiatorSignatures: List<String>
    )

    inner class OpAccountSignatureInfo(
        val signature: String,
        val publicKey: String
    )

    override fun retrieveSignableData(approverPublicKey: String?): ByteArray {
        if(approverPublicKey == null) {
            throw Exception("Missing Approver Public Key")
        }

        val instructions = mutableListOf(createOpAccountInstruction())
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
            recentBlockhash = blockhash,
            instructions = instructions
        ).serialize()
    }

    override fun signableSupplyInstructions(approverPublicKey: String): List<ByteArray> {
        return supplyInstructions.map {
            Transaction.compileMessage(
                feePayer = PublicKey(signingData.feePayer),
                recentBlockhash = blockhash,
                instructions = listOf(
                    TransactionInstruction(
                        programId = PublicKey(signingData.walletProgramId),
                        keys = listOf(
                            AccountMeta(publicKey = opAccountPublicKey(), isSigner = false, isWritable = true),
                            AccountMeta(publicKey = dataAccountPublicKey(), isSigner = false, isWritable = true),
                            AccountMeta(publicKey = PublicKey(approverPublicKey), isSigner = true, isWritable = false),
                        ),
                        data = it.combinedBytes()
                    )
                )
            ).serialize()
        }
    }
}