package com.censocustody.android.common.evm

sealed class SafeTx {
    data class SwapOwner(
        val prev: String,
        val old: String,
        val new: String
    ) : SafeTx()

    data class AddOwnerWithThreshold(
        val owner: String,
        var threshold: Int
    ) : SafeTx()

    data class RemoveOwner(
        val prev: String,
        val owner: String,
        var threshold: Int
    ) : SafeTx()

    data class ChangeThreshold(
        val threshold: Int
    ) : SafeTx()

    data class Policy(
        val owners: List<String>,
        val threshold: Int
    ) {
        init {
            assert(owners.toSet().size == owners.size)
            assert(owners.size >= 1)
            assert(threshold >= 1)
            assert(threshold <= owners.size)
        }

        fun addedOwners(targetPolicy: Policy): List<String> {
            return (targetPolicy.owners.toSet() - owners.toSet()).sorted()
        }

        fun removedOwners(targetPolicy: Policy): List<String> {
            return (owners.toSet() - targetPolicy.owners.toSet()).sorted()
        }

        fun prevOwner(owner: String): String {
            var prev: String? = null
            owners.forEach { o ->
                if (o == owner) {
                    return prev ?: GnosisSafeConstants.sentinelAddress
                }
                prev = o
            }
            throw Exception("Owner not found")
        }

        fun applyTransaction(tx: SafeTx): Policy {
            val currentOwners = owners.toMutableList()
            var currentThreshold = threshold
            when (tx) {
                is SwapOwner -> {
                    val ownerIndex = currentOwners.indexOf(tx.old)
                    if (ownerIndex == 0) {
                        assert(tx.prev == GnosisSafeConstants.sentinelAddress) { "prev != sentinel " }
                    } else {
                        assert(tx.prev == currentOwners[ownerIndex - 1])
                    }
                    currentOwners[ownerIndex] = tx.new
                }
                is AddOwnerWithThreshold -> {
                    currentOwners.add(0, tx.owner)
                    assert(tx.threshold >= 1)
                    assert(tx.threshold <= currentOwners.size)
                    currentThreshold = tx.threshold
                }
                is RemoveOwner -> {
                    val ownerIndex = owners.indexOf(tx.owner)
                    if (ownerIndex == 0) {
                        assert(tx.prev == GnosisSafeConstants.sentinelAddress) { "prev != sentinel " }
                    } else {
                        assert(tx.prev == currentOwners[ownerIndex - 1])
                    }
                    currentOwners.remove(tx.owner)
                    assert(tx.threshold >= 1)
                    assert(tx.threshold <= currentOwners.size)
                    currentThreshold = tx.threshold
                }
                is ChangeThreshold -> {
                    currentThreshold = tx.threshold
                }
            }
            return Policy(currentOwners, currentThreshold)
        }

        fun safeTransactions(targetPolicy: Policy): Pair<List<SafeTx>, Policy> {
            val toAdd = addedOwners(targetPolicy)
            val toRemove = removedOwners(targetPolicy)
            val numSwaps = minOf(toAdd.size, toRemove.size)
            val transactions = mutableListOf<SafeTx>()
            var currentPolicy = Policy(owners, threshold)
            (0 until numSwaps).forEach { i ->
                val tx = SwapOwner(currentPolicy.prevOwner(toRemove[i]), toRemove[i], toAdd[i])
                currentPolicy = currentPolicy.applyTransaction(tx)
                transactions.add(tx)
            }

            val numAdds = toAdd.size - numSwaps
            val numRemoves = toRemove.size - numSwaps

            (numSwaps until toAdd.size).forEach { i ->
                val tx = AddOwnerWithThreshold(toAdd[i], threshold)
                currentPolicy = currentPolicy.applyTransaction(tx)
                transactions.add(tx)
            }

            (numSwaps until toRemove.size).forEach { i ->
                val tx = RemoveOwner(currentPolicy.prevOwner(toRemove[i]), toRemove[i], maxOf(1, threshold - (1 + i - numSwaps)))
                currentPolicy = currentPolicy.applyTransaction(tx)
                transactions.add(tx)
            }

            if (currentPolicy.threshold != targetPolicy.threshold) {
                if (transactions.size == 0 || (numAdds == 0 && numRemoves == 0)) {
                    val tx = ChangeThreshold(targetPolicy.threshold)
                    currentPolicy = currentPolicy.applyTransaction(tx)
                    transactions.add(tx)
                } else {
                    when (val lastTx = transactions.last()) {
                        is AddOwnerWithThreshold -> lastTx.threshold = targetPolicy.threshold
                        is RemoveOwner -> lastTx.threshold = targetPolicy.threshold
                        else -> {}
                    }
                    currentPolicy = currentPolicy.copy(
                        threshold = targetPolicy.threshold
                    )
                }
            }

            return Pair(transactions, currentPolicy)
        }
    }
}
