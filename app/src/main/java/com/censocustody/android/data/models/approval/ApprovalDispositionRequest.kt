package com.censocustody.android.data.models.approval

import com.censocustody.android.common.BaseWrapper.decodeFromBase64
import com.censocustody.android.data.EncryptionManager
import com.censocustody.android.data.Signable
import com.censocustody.android.data.SignedPayload
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.Chain
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.data.models.approval.PublicKey.Companion.SYSVAR_CLOCK_PUBKEY
import com.censocustody.android.data.models.approval.TransactionInstruction.Companion.createAdvanceNonceInstruction
import org.web3j.crypto.Hash
import java.io.ByteArrayOutputStream
import kotlin.Exception
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.Companion.INVALID_REQUEST_APPROVAL
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.Companion.UNKNOWN_REQUEST_APPROVAL
import com.censocustody.android.data.models.approval.ApprovalRequestDetails.*
import com.censocustody.android.common.EthereumTransactionUtil
import com.censocustody.android.common.Operation
import java.nio.ByteBuffer
import javax.crypto.Cipher
import org.bouncycastle.util.encoders.Hex
import java.math.BigInteger

data class ApprovalDispositionRequest(
    val requestId: String,
    val approvalDisposition: ApprovalDisposition,
    val requestType: ApprovalRequestDetails,
    val nonces: List<Nonce>,
    val email: String
) : Signable {

    val opIndex: Byte = 9

    val solanaProgramValue: Byte =
        if (approvalDisposition == ApprovalDisposition.APPROVE) 1 else 2

    private fun retrieveOpCode(): Byte {
        return when (requestType) {
            is WalletCreation -> 1
            is WithdrawalRequest, is ConversionRequest -> 3
            is SignersUpdate -> 5
            is WrapConversionRequest -> 4
            is WalletConfigPolicyUpdate -> 6
            is DAppTransactionRequest -> 7
            is BalanceAccountSettingsUpdate -> 8

            is DAppBookUpdate -> 9
            is CreateAddressBookEntry, is DeleteAddressBookEntry -> 10
            is BalanceAccountNameUpdate -> 11
            is BalanceAccountPolicyUpdate -> 12
            is BalanceAccountAddressWhitelistUpdate -> 14
            is SignData -> 15

            is LoginApprovalRequest,
            is UnknownApprovalType,
            is AcceptVaultInvitation,
            is PasswordReset -> 0
        }
    }

    fun opHashData(): ByteArray {
        val signingData: SigningData.SolanaSigningData = when (val s = signingData()) {
            is SigningData.SolanaSigningData -> s
            else -> return ByteArray(0)
        }
        val commonBytes = signingData.commonOpHashBytes()

        signingData.base64DataToSign?.let {
            return@opHashData SignDataHelper.serializeSignData(it, commonBytes, 15)
        }

        return when (requestType) {
            is WalletCreation -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is WithdrawalRequest -> {
                val buffer = ByteArrayOutputStream()

                val opAndCommonBuffer = ByteArrayOutputStream()
                opAndCommonBuffer.write(byteArrayOf(retrieveOpCode()))
                opAndCommonBuffer.write(commonBytes)
                buffer.write(opAndCommonBuffer.toByteArray())

                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.destination.address.base58Bytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress!!.base58Bytes())

                buffer.toByteArray()
            }
            is ConversionRequest -> {
                val buffer = ByteArrayOutputStream()

                val opAndCommonBuffer = ByteArrayOutputStream()
                opAndCommonBuffer.write(byteArrayOf(retrieveOpCode()))
                opAndCommonBuffer.write(commonBytes)
                buffer.write(opAndCommonBuffer.toByteArray())

                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.destination.address.base58Bytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(requestType.symbolAndAmountInfo.symbolInfo.tokenMintAddress!!.base58Bytes())

                buffer.toByteArray()
            }
            is SignersUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(byteArrayOf(requestType.slotUpdateType.toSolanaProgramValue()))
                buffer.write(requestType.signer.opHashBytes())

                buffer.toByteArray()
            }

            is WrapConversionRequest -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.writeLongLE(requestType.symbolAndAmountInfo.fundamentalAmount())
                buffer.write(byteArrayOf(requestType.symbolAndAmountInfo.symbolInfo.getSOLProgramValue()))

                buffer.toByteArray()
            }
            is WalletConfigPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.approvalPolicy.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountSettingsUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is DAppBookUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is CreateAddressBookEntry -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is DeleteAddressBookEntry -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountNameUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountPolicyUpdate -> {
                val buffer = ByteArrayOutputStream()

                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())

                buffer.toByteArray()
            }
            is BalanceAccountAddressWhitelistUpdate -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.combinedBytes())
                buffer.toByteArray()
            }

            is DAppTransactionRequest -> {
                val buffer = ByteArrayOutputStream()
                buffer.write(byteArrayOf(retrieveOpCode()))
                buffer.write(commonBytes)
                buffer.write(requestType.account.identifier.sha256HashBytes())
                buffer.write(requestType.dappInfo.address.base58Bytes())
                buffer.write(requestType.dappInfo.name.sha256HashBytes())
                buffer.writeShortLE(requestType.instructions.sumOf { it.decodedData().size }.toShort())
                buffer.write(requestType.instructions.map { it.decodedData() }.reduce { array, next -> array + next })
                buffer.toByteArray()
            }
            is SignData -> {
                SignDataHelper.serializeSignData(requestType.base64Data, commonBytes, 15)
            }
            is LoginApprovalRequest, is AcceptVaultInvitation, is PasswordReset -> throw Exception(
                INVALID_REQUEST_APPROVAL
            )
            is UnknownApprovalType -> throw Exception(
                UNKNOWN_REQUEST_APPROVAL
            )
        }
    }

    private fun signingData(): SigningData =
        when (requestType) {
            is WalletCreation -> requestType.signingData ?: throw Exception(INVALID_REQUEST_APPROVAL)
            is WithdrawalRequest -> requestType.signingData
            is ConversionRequest -> requestType.signingData
            is SignersUpdate -> requestType.signingData
            is WrapConversionRequest -> requestType.signingData
            is WalletConfigPolicyUpdate -> requestType.signingData
            is BalanceAccountSettingsUpdate -> requestType.signingData
            is DAppBookUpdate -> requestType.signingData
            is CreateAddressBookEntry -> requestType.signingData ?: throw Exception(INVALID_REQUEST_APPROVAL)
            is DeleteAddressBookEntry -> requestType.signingData ?: throw Exception(INVALID_REQUEST_APPROVAL)
            is BalanceAccountNameUpdate -> requestType.signingData
            is BalanceAccountPolicyUpdate -> requestType.signingData
            is BalanceAccountAddressWhitelistUpdate -> requestType.signingData
            is DAppTransactionRequest -> requestType.signingData
            is SignData -> requestType.signingData

            is LoginApprovalRequest, is AcceptVaultInvitation, is PasswordReset -> throw Exception(
                INVALID_REQUEST_APPROVAL
            )
            is UnknownApprovalType -> throw Exception(
                UNKNOWN_REQUEST_APPROVAL
            )
        }

    private fun generateTransactionInstructionData(): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(byteArrayOf(opIndex))
        buffer.write(byteArrayOf(solanaProgramValue))
        buffer.write(Hash.sha256(opHashData()))
        return buffer.toByteArray()
    }

    override fun retrieveSignableData(approverPublicKey: String?): List<ByteArray> {
        return when (requestType) {
            is LoginApprovalRequest ->
                listOf(requestType.jwtToken.toByteArray(charset = Charsets.UTF_8))
            is AcceptVaultInvitation ->
                listOf(requestType.vaultName.toByteArray(charset = Charsets.UTF_8))
            is PasswordReset ->
                listOf(requestId.toByteArray(charset = Charsets.UTF_8))
            is WalletCreation ->
                when (requestType.accountInfo.chain) {
                    Chain.bitcoin, Chain.ethereum -> listOf(requestType.toJson().toByteArray())
                    else -> listOf(serializeSolanaRequest(approverPublicKey))
                }
            is CreateAddressBookEntry ->
                when (requestType.chain) {
                    Chain.bitcoin, Chain.ethereum -> listOf(requestType.toJson().toByteArray())
                    else -> listOf(serializeSolanaRequest(approverPublicKey))
                }
            is DeleteAddressBookEntry ->
                when (requestType.chain) {
                    Chain.bitcoin, Chain.ethereum -> listOf(requestType.toJson().toByteArray())
                    else -> listOf(serializeSolanaRequest(approverPublicKey))
                }
            is WithdrawalRequest -> {
                when (requestType.signingData) {
                    is SigningData.SolanaSigningData -> listOf(serializeSolanaRequest(approverPublicKey))
                    is SigningData.BitcoinSigningData -> requestType.signingData.transaction.txIns.map { decodeFromBase64(it.base64HashForSignature) }
                    is SigningData.EthereumSigningData -> listOf(ethereumWithdrawal(requestType, requestType.signingData.transaction))
                }
            }
            else -> listOf(serializeSolanaRequest(approverPublicKey))
        }
    }

    private fun ByteBuffer.putPadded(data: ByteArray, padTo: Int = 32) {
        require(data.size <= padTo)
        this.put(ByteArray(padTo - data.size))
        this.put(data)
    }

    private fun erc20WithdrawalTx(withdrawalRequest: WithdrawalRequest): ByteArray {
        val data = ByteBuffer.allocate(4 + 32*2)
        data.put(Hex.decode("a9059cbb"))
        data.putPadded(Hex.decode(withdrawalRequest.destination.address.removePrefix("0x")))
        data.putPadded(withdrawalRequest.symbolAndAmountInfo.fundamentalAmountAsBigInteger().toByteArray())
        return data.array()
    }

    private fun erc721WithdrawalTx(withdrawalRequest: WithdrawalRequest): ByteArray {
        // safeTransferFrom(address,address,uint256)
        val data = ByteBuffer.allocate(4 + 32*3)
        data.put(Hex.decode("42842e0e"))
        withdrawalRequest.account.address?.let {
            data.putPadded(Hex.decode(withdrawalRequest.account.address.removePrefix("0x")))
        }
        data.putPadded(Hex.decode(withdrawalRequest.destination.address.removePrefix("0x")))
        withdrawalRequest.symbolAndAmountInfo.symbolInfo.ethTokenInfo?.tokenId?.let { tokenId ->
            data.putPadded(BigInteger(tokenId).toByteArray())
        }
        return data.array()
    }

    private fun erc1155WithdrawalTx(withdrawalRequest: WithdrawalRequest): ByteArray {
        // safeTransferFrom(address,address,uint256,uint256,bytes)
        val data = ByteBuffer.allocate(4 + 32*6)
        data.put(Hex.decode("f242432a"))
        withdrawalRequest.account.address?.let {
            data.putPadded(Hex.decode(withdrawalRequest.account.address.removePrefix("0x")))
        }
        data.putPadded(Hex.decode(withdrawalRequest.destination.address.removePrefix("0x")))
        withdrawalRequest.symbolAndAmountInfo.symbolInfo.ethTokenInfo?.tokenId?.let { tokenId ->
            data.putPadded(BigInteger(tokenId).toByteArray())
        }
        data.putPadded(withdrawalRequest.symbolAndAmountInfo.fundamentalAmountAsBigInteger().toByteArray())
        // this the 5th bytes param which is dynamic data - the 160 is the offset where dynamic data starts
        // followed by 32 bytes of 0 for the length of the dynamic data
        data.putPadded(BigInteger("160").toByteArray())
        data.putPadded(ByteArray(0))
        return data.array()
    }

    private fun ethereumWithdrawal(withdrawalRequest: WithdrawalRequest, ethereumTransaction: SigningData.EthereumTransaction): ByteArray {
        return withdrawalRequest.symbolAndAmountInfo.symbolInfo.tokenMintAddress?.let { contractAddress ->
            EthereumTransactionUtil.computeSafeTransactionHash(
                ethereumTransaction.chainId,
                withdrawalRequest.account.address!!,
                contractAddress,
                BigInteger.ZERO,
                when (withdrawalRequest.symbolAndAmountInfo.symbolInfo.ethTokenInfo?.tokenType) {
                    EthTokenType.ERC721 -> erc721WithdrawalTx(withdrawalRequest)
                    EthTokenType.ERC1155 -> erc1155WithdrawalTx(withdrawalRequest)
                    else -> erc20WithdrawalTx(withdrawalRequest)
                },
                Operation.CALL,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                "0x0000000000000000000000000000000000000000",
                "0x0000000000000000000000000000000000000000",
                ethereumTransaction.safeNonce.toBigInteger(),
            )
        } ?:
        EthereumTransactionUtil.computeSafeTransactionHash(
            ethereumTransaction.chainId,
            withdrawalRequest.account.address!!,
            withdrawalRequest.destination.address,
            withdrawalRequest.symbolAndAmountInfo.fundamentalAmountAsBigInteger(),
            ByteArray(0),
            Operation.CALL,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            "0x0000000000000000000000000000000000000000",
            "0x0000000000000000000000000000000000000000",
            ethereumTransaction.safeNonce.toBigInteger(),
        )
    }

    fun serializeSolanaRequest(approverPublicKey: String?): ByteArray {
        if (approverPublicKey == null) throw Exception("MISSING KEY")

        val signingData = signingData() as SigningData.SolanaSigningData

        val nonce = nonces.firstOrNull()
        val nonceAccountAddress = requestType.nonceAccountAddresses().firstOrNull()

        if (nonce == null || nonceAccountAddress == null) {
            throw Exception("NOT ENOUGH NONCE ACCOUNTS")
        }

        val keyList = listOf(
            AccountMeta(
                publicKey = PublicKey(signingData.multisigOpAccountAddress),
                isSigner = false,
                isWritable = true
            ),
            AccountMeta(
                publicKey = PublicKey(approverPublicKey),
                isSigner = true,
                isWritable = false
            ),
            AccountMeta(
                publicKey = SYSVAR_CLOCK_PUBKEY,
                isSigner = false,
                isWritable = false
            )
        )

        val programId = PublicKey(signingData.walletProgramId)

        val transactionMessage = Transaction.compileMessage(
            feePayer = PublicKey(signingData.feePayer),
            recentBlockhash = nonce.value,
            instructions = listOf(
                createAdvanceNonceInstruction(
                    nonceAccountAddress = nonceAccountAddress,
                    feePayer = signingData.feePayer
                ),
                TransactionInstruction(
                    keys = keyList,
                    programId = programId,
                    data = generateTransactionInstructionData()
                )
            )
        )

        return transactionMessage.serialize()
    }

    fun convertToApiBody(
        encryptionManager: EncryptionManager,
        cipher: Cipher): RegisterApprovalDispositionBody {

        val signatureInfo: ApprovalSignature = when (requestType) {
            is LoginApprovalRequest, is PasswordReset, is AcceptVaultInvitation ->
                ApprovalSignature.NoChainSignature(
                    signRequestWithSolanaKey(encryptionManager, cipher)
                )
            is WalletCreation -> getSignatureInfo(requestType.accountInfo.chain ?: Chain.solana, encryptionManager, cipher)
            is CreateAddressBookEntry -> getSignatureInfo(requestType.chain, encryptionManager, cipher)
            is DeleteAddressBookEntry -> getSignatureInfo(requestType.chain, encryptionManager, cipher)
            is WithdrawalRequest -> {
                when (requestType.signingData) {
                    is SigningData.BitcoinSigningData ->
                        ApprovalSignature.BitcoinSignatures(
                            signatures = signRequestWithBitcoinKey(encryptionManager, cipher, requestType.signingData.childKeyIndex).map { it.signature }
                        )
                    is SigningData.SolanaSigningData ->
                        ApprovalSignature.SolanaSignature(
                            signature = signRequestWithSolanaKey(encryptionManager, cipher).signature,
                            nonce = nonces.first().value,
                            nonceAccountAddress = requestType.nonceAccountAddresses().first()
                        )
                    is SigningData.EthereumSigningData ->
                        ApprovalSignature.EthereumSignature(
                            signature = signRequestWithEthereumKey(encryptionManager, cipher).signature,
                        )
                }
            }
            else -> ApprovalSignature.SolanaSignature(
                signature = signRequestWithSolanaKey(encryptionManager, cipher).signature,
                nonce = nonces.first().value,
                nonceAccountAddress = requestType.nonceAccountAddresses().first()
            )
        }
        
        return RegisterApprovalDispositionBody(
            approvalDisposition = approvalDisposition,
            signatureInfo = signatureInfo
        )
    }

    private fun getSignatureInfo(chain: Chain, encryptionManager: EncryptionManager, cipher: Cipher): ApprovalSignature {
        return when (chain) {
            Chain.bitcoin, Chain.ethereum -> ApprovalSignature.NoChainSignature(
                signRequestWithCensoKey(encryptionManager, cipher)
            )
            else -> ApprovalSignature.SolanaSignature(
                signature = signRequestWithSolanaKey(encryptionManager, cipher).signature,
                nonce = nonces.first().value,
                nonceAccountAddress = requestType.nonceAccountAddresses().first()
            )
        }
    }

    private fun signRequestWithSolanaKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signSolanaApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    private fun signRequestWithBitcoinKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher,
        childKeyIndex: Int): List<SignedPayload> {

        return try {
            encryptionManager.signBitcoinApprovalDispositionMessage(
                signable = this,
                cipher = cipher,
                email = email,
                childKeyIndex = childKeyIndex
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    private fun signRequestWithEthereumKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signEthereumApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher,
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    private fun signRequestWithCensoKey(
        encryptionManager: EncryptionManager,
        cipher: Cipher): SignedPayload {

        return try {
            encryptionManager.signCensoApprovalDispositionMessage(
                signable = this,
                email = email,
                cipher = cipher,
            )
        } catch (e: Exception) {
            throw Exception("Signing data failure")
        }
    }

    inner class RegisterApprovalDispositionBody(
        val approvalDisposition: ApprovalDisposition,
        val signatureInfo: ApprovalSignature
    )
}