package com.censocustody.android.evm

import com.censocustody.android.common.evm.GnosisSafeConstants
import com.censocustody.android.common.evm.SafeTx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test


class EvmConfigTransactionHelperTest {

    @Test
    fun testAssertions() {
        // need at least 1 owner
        assertThrows(AssertionError::class.java) {
            SafeTx.Policy(listOf(), -1)
        }
        // cannot duplicate owners
        assertThrows(AssertionError::class.java) {
            SafeTx.Policy(listOf("owner-1", "owner-1"), -1)
        }
        // threshold must be >= 1
        assertThrows(AssertionError::class.java) {
            SafeTx.Policy(listOf("owner-1"), -1)
        }
        assertThrows(AssertionError::class.java) {
            SafeTx.Policy(listOf("owner-1"), 0)
        }
        // threshold must be <= len(owners)
        assertThrows(AssertionError::class.java) {
            SafeTx.Policy(listOf("owner-1"), 2)
        }

        SafeTx.Policy(listOf("owner-1"), 1)
    }

    @Test
    fun testAddOwners() {
        val startingPolicy = SafeTx.Policy(listOf("owner-1", "owner-2"), 1)
        listOf(
            Pair(listOf("owner-3"), listOf("owner-3")),
            Pair(listOf("owner-4", "owner-3", "owner-1"), listOf("owner-3", "owner-4")),
            Pair(listOf("owner-1"), listOf()),
            Pair(listOf("owner-1", "owner-2"), listOf()),
        ).forEach { (targetOwners, expected) ->
            assertEquals(startingPolicy.addedOwners(SafeTx.Policy(targetOwners, 1)), expected)
        }
    }

    @Test
    fun testRemoveOwners() {
        val startingPolicy = SafeTx.Policy(listOf("owner-2", "owner-1"), 1)
        listOf(
            Pair(listOf("owner-2"), listOf("owner-1")),
            Pair(listOf("owner-3"), listOf("owner-1", "owner-2")),
            Pair(listOf("owner-3", "owner-1"), listOf("owner-2")),
        ).forEach { (targetOwners, expected) ->
            assertEquals(startingPolicy.removedOwners(SafeTx.Policy(targetOwners, 1)), expected)
        }
    }

    @Test
    fun testSafeTransactions() {
        val startingPolicy = SafeTx.Policy(listOf("owner-1", "owner-2"), 1)

        listOf(
            Pair(
                SafeTx.Policy(listOf("owner-1", "owner-2"), 1),
                listOf<SafeTx>()
            ),
            Pair(
                SafeTx.Policy(listOf("owner-1"), 1),
                listOf(SafeTx.RemoveOwner("owner-1", "owner-2", 1))
            ),
            Pair(
                SafeTx.Policy(listOf("owner-2"), 1),
                listOf(SafeTx.RemoveOwner(GnosisSafeConstants.sentinelAddress, "owner-1", 1))
            ),
            Pair(
                SafeTx.Policy(listOf("owner-1", "owner-3"), 1),
                listOf(SafeTx.SwapOwner("owner-1", "owner-2", "owner-3"))
            ),
            Pair(
                SafeTx.Policy(listOf("owner-3", "owner-2"), 1),
                listOf(SafeTx.SwapOwner(GnosisSafeConstants.sentinelAddress, "owner-1", "owner-3"))
            ),
            Pair(
                SafeTx.Policy(listOf("owner-3", "owner-4"), 1),
                listOf(
                    SafeTx.SwapOwner(GnosisSafeConstants.sentinelAddress, "owner-1", "owner-3"),
                    SafeTx.SwapOwner("owner-3", "owner-2", "owner-4")
                )
            ),
            Pair(
                SafeTx.Policy(listOf("owner-3", "owner-4"), 2),
                listOf(
                    SafeTx.SwapOwner(GnosisSafeConstants.sentinelAddress, "owner-1", "owner-3"),
                    SafeTx.SwapOwner("owner-3", "owner-2", "owner-4"),
                    SafeTx.ChangeThreshold(2)
                )
            ),
            Pair(
                SafeTx.Policy(listOf("owner-1", "owner-3", "owner-4"), 2),
                listOf(
                    SafeTx.SwapOwner("owner-1", "owner-2", "owner-3"),
                    SafeTx.AddOwnerWithThreshold("owner-4", 2)
                )
            ),
        ).forEach { (targetPolicy, expected) ->
            val (transactions, endingPolicy) = startingPolicy.safeTransactions(targetPolicy)
            assertEquals(transactions, expected)
            assertPolicyMatches(startingPolicy, targetPolicy, transactions, endingPolicy)
        }
    }

    @Test
    fun testSafeTransactionsRandom() {
        val allOwners = listOf("owner-1", "owner-2", "owner-3", "owner-4", "owner-5", "owner-6", "owner-7", "owner-8", "owner-9", "owner-10")

        (0..100).forEach { _ ->
            val startCount = IntRange(1, allOwners.size).random()
            val startingThreshold = IntRange(1, startCount).random()
            val startingPolicy = SafeTx.Policy(allOwners.shuffled().slice(IntRange(0, startCount - 1)).toMutableList(), startingThreshold)
            val endingCount = IntRange(1, allOwners.size).random()
            val endingThreshold = IntRange(1, endingCount).random()
            val endingPolicy = SafeTx.Policy(allOwners.shuffled().slice(IntRange(0, endingCount - 1)).toMutableList(), endingThreshold)
            val (transactions, computedEndingPolicy) = startingPolicy.safeTransactions(endingPolicy)
            assertPolicyMatches(startingPolicy, endingPolicy, transactions, computedEndingPolicy)
        }
    }

    private fun assertPolicyMatches(startingPolicy: SafeTx.Policy, targetPolicy: SafeTx.Policy, transactions: List<SafeTx>, endingPolicy: SafeTx.Policy) {
        assertEquals(endingPolicy.owners.toSet(), targetPolicy.owners.toSet())
        assertEquals(endingPolicy.threshold, targetPolicy.threshold)
        // recompute ending policy from transactions
        var policy = SafeTx.Policy(startingPolicy.owners, startingPolicy.threshold)
        transactions.forEach {
            policy = policy.applyTransaction(it)
        }
        assertEquals(policy.owners, endingPolicy.owners)
        assertEquals(policy.threshold, endingPolicy.threshold)
    }
}
