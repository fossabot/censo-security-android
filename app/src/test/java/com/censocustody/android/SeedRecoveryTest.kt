package com.censocustody.android

import com.censocustody.android.common.*
import com.censocustody.android.common.SecretSharerUtils.randomFieldElement
import org.junit.Assert.*
import java.math.BigInteger
import java.util.UUID
import kotlin.random.Random
import org.junit.Test

class SeedRecoveryTest {

    @Test
    fun `test secret sharer sequential small order`() {
        val rnd = java.security.SecureRandom()
        val order = 65537
        val secret = BigInteger(16, rnd)
        val seedRecovery = SecretSharer(
            secret, 3, (1..6).map { BigInteger.valueOf(it.toLong()) }, order.toBigInteger()
        )
        assertEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[0],
                    seedRecovery.shards[1],
                    seedRecovery.shards[2],
                )
            )
        )
    }

    @Test
    fun `test secret sharer one participant`() {
        val rnd = java.security.SecureRandom()
        val secret = BigInteger(ORDER.bitLength(), rnd)
        val seedRecovery = SecretSharer(
            secret, 1, listOf(BigInteger.valueOf(1))
        )
        assertEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[0],
                )
            )
        )
        assertEquals(
            secret,
            seedRecovery.shards[0].y,
        )
    }

    @Test
    fun `test secret sharer two participant and 64 byte number`() {

        val secret = BigInteger("a3a4a523f3fcd16ab61fb7eba989e7b4155a5f960eb30877a5a4fdeaa7b8fd8373eb765067c15c50803bd5d141fa1b1a43fc7415bc664d34d6b3ce14db67daee", 16)
        val seedRecovery = SecretSharer(
            secret, 2, listOf(BigInteger.valueOf(1), BigInteger.valueOf(2))
        )
        assertEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[0],
                    seedRecovery.shards[1],
                )
            )
        )
    }

    @Test
    fun `test secret sharer random`() {
        val rnd = java.security.SecureRandom()
        val secret = BigInteger(ORDER.bitLength(), rnd)
        val seedRecovery = SecretSharer(
            secret, 3, (1..6).map { BigInteger.valueOf(Random.nextLong()) }
        )
        assertEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[0], seedRecovery.shards[1], seedRecovery.shards[2]
                )
            )
        )
        assertEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[2], seedRecovery.shards[4], seedRecovery.shards[5]
                )
            )
        )
        assertNotEquals(
            secret,
            seedRecovery.recoverSecret(
                listOf(
                    seedRecovery.shards[2],
                    seedRecovery.shards[4],
                )
            )
        )
    }

    private fun assertMatrix(matrix: Matrix, vararg expected: Long) {
        var i = 0
        matrix.forEach { row ->
            row.forEach { value ->
                assertEquals(value, BigInteger.valueOf(expected[i]))
                i += 1
            }
        }
    }

    @Test
    fun `test matrix inversion`() {
        val seedRecovery = SecretSharer(BigInteger.ONE, 1, listOf(BigInteger.ONE), order = BigInteger.valueOf(65537))
        val vandermonde = seedRecovery.vandermonde(listOf(7, 8, 9, 10).map { BigInteger.valueOf(it.toLong()) }, 4)
        val(lu, p) = seedRecovery.decomposeLUP(vandermonde)
        val inverse = seedRecovery.invertLUP(lu, p)
        assertMatrix(
            inverse,
            120, 65222, 280, 65453,
            43651, 32880, 65434, 54646,
            32773, 65524, 32781, 65533,
            54614, 32769, 32768, 10923,
        )
    }

    @Test
    fun `test resharing`() {
        val rnd = java.security.SecureRandom()
        val secret = BigInteger(ORDER.bitLength(), rnd)
        val participants = (1..6).map { BigInteger.valueOf(it.toLong()) }
        val sharer = SecretSharer(secret, 3, participants)
        val newParticipants = (7..12).map { BigInteger.valueOf(it.toLong()) }
        val reshares = sharer.getReshares(newParticipants, 4)
        val vandermonde = sharer.vandermonde(participants.take(3), 3)
        val(lu, p) = sharer.decomposeLUP(vandermonde)
        val vandermondeInverse = sharer.invertLUP(
            lu, p
        )
        val newShares = newParticipants.mapIndexed { i, part ->
            Point(part, sharer.addShards(reshares.map { it[i].y }, vandermondeInverse[0].asList()))
        }
        assertEquals(
            secret,
            sharer.recoverSecret(
                listOf(
                    newShares[0],
                    newShares[1],
                    newShares[2],
                    newShares[3],
                )
            )
        )
        assertEquals(
            secret,
            sharer.recoverSecret(
                listOf(
                    newShares[1],
                    newShares[2],
                    newShares[3],
                    newShares[4],
                )
            )
        )
        assertNotEquals(
            secret,
            sharer.recoverSecret(
                listOf(
                    newShares[1],
                    newShares[2],
                    newShares[3],
                )
            )
        )
    }

    @Test
    fun `test user workflows`() {
        // initial org creation
        val shardStore = ShardStore()
        val user1 = User("user1@org", mutableListOf())
        val recoveryKeys = (1..3).map { "org-${UUID.randomUUID()}" }
        val org = Org(shardStore, recoveryKeys[0], recoveryKeys[1], recoveryKeys[2])
        val app1 = MobileApp(user1, org, shardStore)
        val policy1 = Policy(listOf(app1.device), 1, "rev-${UUID.randomUUID()}")
        val sid1 = app1.shareToAdmins(policy1)
        assertEquals(
            listOf(
                Shard(sid1, app1.participantId, policy1.threshold, app1.rootSeed, policy1.revision, user1.email)
            ),
            shardStore.getShards(sid = setOf(sid1))
        )
        // should also be an org recovery share with 3 shards
        val orgRecoveryShares = shardStore.getShards().filter { it.sid != sid1 }
        assertEquals(3, orgRecoveryShares.size)
        assertEquals(1, orgRecoveryShares.map { it.sid }.toSet().size)
        assertEquals(
            app1.rootSeed,
            SecretSharerUtils.recoverSecret(
                listOf(
                    Point(orgRecoveryShares[0].pid, orgRecoveryShares[0].shard),
                    Point(orgRecoveryShares[1].pid, orgRecoveryShares[1].shard),
                ),
                ORDER
            )
        )

        // adding a second user
        val user2 = User("user2@org", mutableListOf())
        val app2 = MobileApp(user2, org, shardStore)
        val sid2 = app2.shareToAdmins(policy1)
        assertEquals(
            listOf(
                Shard(sid2, app1.participantId, policy1.threshold, app2.rootSeed, policy1.revision, user2.email)
            ),
            shardStore.getShards(sid = setOf(sid2))
        )

        // policy revision to add user2 as an admin
        val policy2 = Policy(listOf(app1.device, app2.device), 1, "rev-${UUID.randomUUID()}")
        // approving the policy revision should publish two reshares, one for each user's seed
        val sids1 = app1.approvePolicyRevision(policy1, policy2)
        assertEquals(2, sids1.size)

        // each share has two shards, one destined for each of the admins in the new policy
        // because the policy is still 1-of-n, the shards are still just the actual root seed
        assertEquals(
            setOf(
                Triple(app1.participantId, policy2.revision, app1.rootSeed),
                Triple(app2.participantId, policy2.revision, app1.rootSeed),
                Triple(app1.participantId, policy2.revision, app2.rootSeed),
                Triple(app2.participantId, policy2.revision, app2.rootSeed),
            ),
            shardStore.getShards(sid = sids1.toSet()).map {
                Triple(it.pid, it.revision, it.shard)
            }.toSet()
        )

        // check that we have an org recovery share (with 3 shards) for each of the shards above
        assertEquals(
            12,
            shardStore.getShards(revision = setOf(policy2.revision)).filterNot { sids1.contains(it.sid) }.size
        )

        // add the third user
        val user3 = User("user3@org", mutableListOf())
        val app3 = MobileApp(user3, org, shardStore)
        val sid3 = app3.shareToAdmins(policy2)
        assertEquals(
            setOf(
                Shard(sid3, app1.participantId, policy2.threshold, app3.rootSeed, policy2.revision, user3.email),
                Shard(sid3, app2.participantId, policy2.threshold, app3.rootSeed, policy2.revision, user3.email),
            ),
            shardStore.getShards(sid = setOf(sid3)).toSet()
        )

        // policy revision to add user3 as an admin
        val policy3 = Policy(listOf(app1.device, app2.device, app3.device), 2, "rev-${UUID.randomUUID()}")
        // approving the policy revision should publish three reshares, one for each user's seed
        val sids2 = app1.approvePolicyRevision(policy2, policy3)
        assertEquals(3, sids2.size)

        sids2.forEach { sid ->
            // now each share has three shards, one destined for each of the admins in the new policy
            // the policy is now 2-of-n, so the shards are no longer just the secret
            val shards = shardStore.getShards(sid = setOf(sid))
            assertEquals(
                setOf(
                    Pair(app1.participantId, policy3.revision),
                    Pair(app2.participantId, policy3.revision),
                    Pair(app3.participantId, policy3.revision),
                ),
                shards.map {
                    Pair(it.pid, it.revision)
                }.toSet()
            )

            shards.forEach {
                assertNotEquals(app1.rootSeed, it.shard)
                assertNotEquals(app2.rootSeed, it.shard)
                assertNotEquals(app3.rootSeed, it.shard)
            }

            // we should be able to recover the root seed from two of the shards
            val recovered = SecretSharerUtils.recoverSecret(
                listOf(
                    Point(shards[0].pid, shards[0].shard),
                    Point(shards[1].pid, shards[1].shard),
                ),
                ORDER
            )
            assert(recovered == app1.rootSeed || recovered == app2.rootSeed || recovered == app3.rootSeed)
        }

        // recover the 3rd user
        val app3new = MobileApp(user3, org, shardStore)
        var app1Shards = app1.approveDeviceRecovery(policy3, app3new)
        var app2Shards = app2.approveDeviceRecovery(policy3, app3new)
        app3new.recoverSeed(app1Shards + app2Shards)
        assertEquals(
            app3.rootSeed,
            app3new.rootSeed
        )

        // policy revision to switch the 3rd user's device
        val policy4 = Policy(listOf(app1.device, app2.device, app3new.device), 2, "rev-${UUID.randomUUID()}")
        // approving the policy revision should publish three reshares, one for each user's seed
        val app1Sids = app1.approvePolicyRevision(policy3, policy4)
        val app2Sids = app2.approvePolicyRevision(policy3, policy4)
        assertEquals(3, app1Sids.size)
        assertEquals(3, app2Sids.size)

        // recover the 2nd user
        val app2new = MobileApp(user2, org, shardStore)
        app1Shards = app1.approveDeviceRecovery(policy4, app2new)
        var app3Shards = app3new.approveDeviceRecovery(policy4, app2new)
        app2new.recoverSeed(app1Shards + app3Shards)
        assertEquals(
            app2.rootSeed,
            app2new.rootSeed
        )

        // recover the 2nd user using org recovery
        val origApp2Shards = shardStore.getShards(pid = setOf(app2.participantId), revision = setOf(policy4.revision))
        val app2org = MobileApp(user2, org, shardStore)
        app2org.recoverSeedAndShards(
            policy4,
            org.recoverShards(policy4, 0, app2.participantId),
            org.recoverShards(policy4, 1, app2.participantId),
            org.recoverSeed(user2, 0),
            org.recoverSeed(user2, 1),
        )

        assertEquals(
            app2.rootSeed,
            app2org.rootSeed
        )
        // shards should be the same
        assertEquals(
            origApp2Shards,
            shardStore.getShards(pid = setOf(app2org.participantId), revision = setOf(policy4.revision))
        )

        // the recovered 2nd user should now be able to help recover e.g. the 1st user
        val app1new = MobileApp(user1, org, shardStore)
        app2Shards = app2org.approveDeviceRecovery(policy4, app1new)
        app3Shards = app3new.approveDeviceRecovery(policy4, app1new)
        app1new.recoverSeed(app2Shards + app3Shards)
        assertEquals(
            app1.rootSeed,
            app1new.rootSeed
        )
    }

    @Test
    fun `test multiple reshares`() {
        // initial org creation
        val shardStore = ShardStore()
        val user1 = User("user1@org", mutableListOf())
        val recoveryKeys = (1..3).map { "org-${UUID.randomUUID()}" }
        val org = Org(shardStore, recoveryKeys[0], recoveryKeys[1], recoveryKeys[2])
        val app1 = MobileApp(user1, org, shardStore)
        val policy1 = Policy(listOf(app1.device), 1, "rev-${UUID.randomUUID()}")
        app1.shareToAdmins(policy1)
        fun assertRecovers(from: MobileApp, user: User, policy: Policy, admins: List<MobileApp>) {
            assertEquals(
                from.rootSeed,
                MobileApp(user, org, shardStore).let {
                    it.recoverSeed(admins.flatMap { admin -> admin.approveDeviceRecovery(policy, it) })
                    it.rootSeed
                }
            )
        }
        assertRecovers(app1, user1, policy1, listOf(app1))

        // adding a second user
        val user2 = User("user2@org", mutableListOf())
        val app2 = MobileApp(user2, org, shardStore)
        app2.shareToAdmins(policy1)
        assertRecovers(app2, user2, policy1, listOf(app1))

        // policy revision to add user2 as an admin
        val policy2 = Policy(listOf(app1.device, app2.device), 2, "rev-${UUID.randomUUID()}")
        app1.approvePolicyRevision(policy1, policy2)

        assertRecovers(app2, user2, policy2, listOf(app1, app2))

        // add a third user
        val user3 = User("user3@org", mutableListOf())
        val app3 = MobileApp(user3, org, shardStore)
        app3.shareToAdmins(policy2)

        assertRecovers(app3, user3, policy2, listOf(app1, app2))

        // policy revision to add user3 as an admin
        val policy3 = Policy(listOf(app1.device, app2.device, app3.device), 2, "rev-${UUID.randomUUID()}")
        // approving the policy revision should publish three reshares, one for each user's seed
        app1.approvePolicyRevision(policy2, policy3)
        app2.approvePolicyRevision(policy2, policy3)

        listOf(Pair(app1, user1), Pair(app2, user2), Pair(app3, user3)).forEach { (app, user) ->
            assertRecovers(app, user, policy3, listOf(app1, app2))
            assertRecovers(app, user, policy3, listOf(app2, app3))
            assertRecovers(app, user, policy3, listOf(app1, app3))
        }

        val user4 = User("user4@org", mutableListOf())
        val app4 = MobileApp(user4, org, shardStore)
        app4.shareToAdmins(policy3)

        assertRecovers(app4, user4, policy3, listOf(app1, app2))

        // policy revision to add user4 as an admin
        val policy4 = Policy(listOf(app4.device, app2.device, app3.device), 2, "rev-${UUID.randomUUID()}")
        // approving the policy revision should publish three reshares, one for each user's seed
        app2.approvePolicyRevision(policy3, policy4)
        app3.approvePolicyRevision(policy3, policy4)

        assertRecovers(app1, user1, policy4, listOf(app2, app3))
    }
}

data class Shard(
    val sid: Sid,
    val pid: Pid,
    val threshold: Int,
    val shard: BigInteger,
    val revision: Revision,
    val email: String,
    val parentShards: List<Shard>? = null
)

class ShardStore {
    val shards: MutableSet<Shard> = mutableSetOf()

    fun getShards(sid: Set<Sid>? = null, pid: Set<Pid>? = null, email: Set<String>? = null, revision: Set<Revision>? = null, isReshare: Boolean? = null, parentPid: Pid? = null): List<Shard> {
        return shards.filter { shard ->
            (
                    sid?.let {
                        sid.contains(shard.sid)
                    } ?: true
                    ) &&
                    (
                            pid?.let {
                                pid.contains(shard.pid)
                            } ?: true
                            ) &&
                    (
                            email?.let {
                                email.contains(shard.email)
                            } ?: true
                            ) &&
                    (
                            revision?.let {
                                revision.contains(shard.revision)
                            } ?: true
                            ) &&
                    (
                            when (isReshare) {
                                true -> (shard.parentShards?.size ?: 0) > 0
                                false -> (shard.parentShards?.size ?: 0) == 0
                                else -> true
                            }
                            ) &&
                    (
                            parentPid?.let {
                                (shard.parentShards ?: listOf()).firstOrNull()?.pid == it
                            } ?: true
                            )
        }
    }

    fun replaceShare(policy: Policy, email: String, parentShards: List<Shard>, shard: BigInteger) {
        val existingShards = getShards(revision = setOf(policy.revision), pid = setOf(parentShards.first().pid), email = setOf(email), parentPid = if (parentShards.size > 1) parentShards[1].pid else null)
        assertEquals(1, existingShards.size)
        shards.remove(existingShards[0])
        shards.add(
            Shard(existingShards[0].sid, parentShards.first().pid, policy.threshold, shard, policy.revision, email, parentShards.slice(1 until parentShards.size))
        )
    }
}

typealias Key = String
typealias Sid = String
typealias Pid = BigInteger
typealias Revision = String
data class User(val email: String, val pids: MutableList<Pid>)

data class Device(val user: User, val key: Key, val pid: Pid)

data class Policy(val approvers: List<Device>, val threshold: Int, val revision: Revision) {
    val participants = approvers.map { it.pid }
    fun shareSecret(secret: BigInteger) = SecretSharer(secret, threshold, participants).shards
    fun reshareShard(shard: Shard, policy: Policy) = SecretSharer(shard.shard, policy.threshold, policy.participants).shards
}

data class Org(val shardStore: ShardStore, val orgRecoveryKey1: Key, val orgRecoveryKey2: Key, val orgRecoveryKey3: Key) {
    private val pids = (1..3).map { randomFieldElement(ORDER) }
    fun shareSecret(secret: BigInteger) = SecretSharer(secret, 2, pids).shards
    fun recoverShards(policy: Policy, which: Int, adminPid: Pid) = shardStore.getShards(
        revision = setOf(policy.revision), pid = setOf(pids[which]), parentPid = adminPid
    )
    fun recoverSeed(user: User, which: Int) = shardStore.getShards(
        email = setOf(user.email), pid = setOf(pids[which]), isReshare = false
    )[0]
}

class MobileApp(val user: User, val org: Org, val shardStore: ShardStore) {
    private val rnd = java.security.SecureRandom()
    var rootSeed = BigInteger(ORDER.bitLength(), rnd)
    val deviceKey = "dev-${UUID.randomUUID()}"
    var participantId = randomFieldElement(ORDER)
    val device = Device(user, deviceKey, participantId)
    init {
        user.pids.add(participantId)
    }

    fun shareToAdmins(policy: Policy): Sid {
        val shareId = "sid-share-${UUID.randomUUID()}"
        val orgShareId = "$shareId-org"
        shardStore.shards.addAll(
            policy.shareSecret(rootSeed).map { point ->
                Shard(shareId, point.x, policy.threshold, point.y, policy.revision, user.email)
            }.toList() + org.shareSecret(rootSeed).map { orgPoint ->
                Shard(orgShareId, orgPoint.x, 2, orgPoint.y, policy.revision, user.email)
            }
        )
        return shareId
    }

    fun approvePolicyRevision(oldPolicy: Policy, newPolicy: Policy): List<Sid> {
        return shardStore.getShards(revision = setOf(oldPolicy.revision), pid = setOf(participantId)).map { shard ->
            val shareId = "sid-reshare-${UUID.randomUUID()}"
            shardStore.shards.addAll(
                oldPolicy.reshareShard(shard, newPolicy).flatMap { point ->
                    Shard(shareId, point.x, newPolicy.threshold, point.y, newPolicy.revision, shard.email, listOf(shard) + (shard.parentShards ?: emptyList())).let {
                        val orgShareId = "$shareId-org"
                        listOf(it) + org.shareSecret(point.y).map { orgPoint ->
                            Shard(orgShareId, orgPoint.x, 2, orgPoint.y, newPolicy.revision, shard.email, listOf(it, shard) + (shard.parentShards ?: emptyList()))
                        }
                    }
                }.toList()
            )
            shareId
        }
    }

    fun approveDeviceRecovery(policy: Policy, app: MobileApp) =
        shardStore.getShards(revision = setOf(policy.revision), email = setOf(app.user.email), pid = setOf(participantId))

    fun recoverSeed(shards: List<Shard>) {
        var remainingShardGroups = shards.groupBy { it.sid }
        while (remainingShardGroups.size > 1) {
            remainingShardGroups = remainingShardGroups.flatMap {
                if (it.value[0].parentShards == null) {
                    it.value
                } else {
                    val exemplar = it.value[0].parentShards!![0]
                    listOf(
                        Shard(
                            exemplar.sid,
                            exemplar.pid,
                            exemplar.threshold,
                            SecretSharerUtils.recoverSecret(
                                it.value.map { s -> Point(s.pid, s.shard) },
                                ORDER
                            ),
                            exemplar.revision,
                            exemplar.email,
                            exemplar.parentShards
                        )
                    )
                }
            }.groupBy { it.sid }
        }
        rootSeed = SecretSharerUtils.recoverSecret(
            remainingShardGroups.values.first().map { s -> Point(s.pid, s.shard) },
            ORDER
        )
    }

    fun recoverSeedAndShards(policy: Policy, org1Shards: List<Shard>, org2Shards: List<Shard>, org1Seed: Shard, org2Seed: Shard) {
        rootSeed = SecretSharerUtils.recoverSecret(
            listOf(org1Seed, org2Seed).map { Point(it.pid, it.shard) },
            ORDER
        )
        val allShards = org1Shards + org2Shards
        assert(allShards.map { it.parentShards!![0].pid }.toSet().size == 1)
        participantId = allShards[0].parentShards!![0].pid
        allShards.groupBy { it.sid }.map {
            val shard = SecretSharerUtils.recoverSecret(
                it.value.map { s -> Point(s.pid, s.shard) },
                ORDER
            )
            shardStore.replaceShare(policy, it.value[0].email, it.value[0].parentShards!!, shard)
        }
    }
}
