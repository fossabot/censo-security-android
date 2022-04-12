package com.strikeprotocols.mobile.data.models.approval

import java.lang.Exception

object Transaction {

    fun compileMessage(
        instructions: List<TransactionInstruction>,
        feePayer: PublicKey,
        recentBlockhash: String
    ): TransactionMessage {
        // verify instructions
        if (instructions.isNullOrEmpty()) {
            throw Exception("No instructions provided")
        }

        // programIds & accountMetas
        val programIds = mutableListOf<PublicKey>()
        var accountMetas = mutableListOf<AccountMeta>()

        for (instruction in instructions) {
            accountMetas.addAll(instruction.keys)
            if (!programIds.contains(instruction.programId)) {
                programIds.add(instruction.programId)
            }
        }

        for (programId in programIds) {
            accountMetas.add(
                AccountMeta(
                    publicKey = programId,
                    isSigner = false,
                    isWritable = false
                )
            )
        }

        //todo: check if this sort matches iOS version which uses booleans
        // sort accountMetas, first by signer, then by writable
        accountMetas.sortWith(object : Comparator<AccountMeta> {
            override fun compare(x: AccountMeta?, y: AccountMeta?): Int {
                if (x?.isSigner != y?.isSigner) {
                    return if (x?.isSigner == true) -1 else 1
                }

                if (x?.isWritable != y?.isWritable) {
                    return if (x?.isWritable == true) -1 else 1
                }

                return 1
            }
        })

        //filterOut duplicate accounts, and try and keep writeable ones
        val uniqueMetas = mutableListOf<AccountMeta>()

        for (accountMeta in accountMetas) {
            val index = uniqueMetas.indexOfFirst { it.publicKey == accountMeta.publicKey }

            if (index == -1) {
                uniqueMetas.add(accountMeta)
            } else {
                uniqueMetas.getOrNull(index)?.let {
                    uniqueMetas[index] =
                        it.copy(isWritable = it.isWritable || accountMeta.isWritable)
                }
            }
        }

        accountMetas = uniqueMetas

        // move fee payer to front
        accountMetas.removeAll { it.publicKey == feePayer }
        accountMetas.add(
            index = 0,
            element = AccountMeta(publicKey = feePayer, isSigner = true, isWritable = true)
        )

        val header = Header()

        header.numRequiredSignatures = accountMetas.count { it.isSigner }
        header.numReadonlySignedAccounts = accountMetas.count { it.isSigner && !it.isWritable }
        header.numReadonlyUnsignedAccounts = accountMetas.count { !it.isSigner && !it.isWritable }

        val signedKeys : List<AccountMeta> = accountMetas.filter { it.isSigner }
        val unsignedKeys : List<AccountMeta> = accountMetas.filter { !it.isSigner }

        accountMetas = (signedKeys + unsignedKeys).toMutableList()

        return TransactionMessage(
            accountKeys = accountMetas,
            recentBlockhash = recentBlockhash,
            instructions = instructions
        )

    }
}