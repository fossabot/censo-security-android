package com.censocustody.android.viewModel

import com.censocustody.android.presentation.pending_approval.PendingApprovalViewModel

import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.Resource
import com.censocustody.android.data.models.DeviceKeyInfo
import com.censocustody.android.data.models.Organization
import com.censocustody.android.data.models.VerifyUser
import com.censocustody.android.data.repository.UserRepository
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

@OptIn(ExperimentalCoroutinesApi::class)
class PendingApprovalViewModelTest : BaseViewModelTest() {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var countdownTimer: CensoCountDownTimer

    private lateinit var pendingApprovalViewModel: PendingApprovalViewModel

    private val dispatcher = TestCoroutineDispatcher()

    private val basicVerifyUserCanAddSigners = VerifyUser(
        fullName = "Jason Jasonson",
        hasApprovalPermission = true,
        id = "09876564534",
        loginName = "jasonson",
        organization = Organization(id = "0987659876", name = "Main Company"),
        publicKeys = emptyList(),
        deviceKeyInfo = DeviceKeyInfo(
            "", true, null
        ),
        shardingPolicy = null,
        canAddSigners = true
    )

    private val basicVerifyUserCannotAddSigners = basicVerifyUserCanAddSigners.copy(
        canAddSigners = false
    )

    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        pendingApprovalViewModel =
            PendingApprovalViewModel(
                userRepository = userRepository,
                timer = countdownTimer
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `if cannot add signers then we should not send user to entrance`() =
        runTest {
            whenever(userRepository.verifyUser()).then { Resource.Success(basicVerifyUserCannotAddSigners) }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Success)
            assertEquals(pendingApprovalViewModel.state.sendUserToEntrance, Resource.Uninitialized)
        }

    @Test
    fun `if can add signers then we should not send user to entrance`() =
        runTest {
            whenever(userRepository.verifyUser()).then { Resource.Success(basicVerifyUserCanAddSigners) }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance.data == true)
        }

    @Test
    fun `if data changes from cannot add signers to add signers then we should not send user to entrance`() =
        runTest {
            whenever(userRepository.verifyUser()).then { Resource.Success(basicVerifyUserCannotAddSigners) }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Uninitialized)
            pendingApprovalViewModel.resetVerifyUserResult()

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Uninitialized)
            pendingApprovalViewModel.resetVerifyUserResult()

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Uninitialized)
            pendingApprovalViewModel.resetVerifyUserResult()

            whenever(userRepository.verifyUser()).then { Resource.Success(basicVerifyUserCanAddSigners) }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance.data == true)
        }

    @Test
    fun `if fail to retrieve user data then show error`() =
        runTest {
            whenever(userRepository.verifyUser()).then { Resource.Error<VerifyUser>() }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Error)
            assertEquals(pendingApprovalViewModel.state.sendUserToEntrance, Resource.Uninitialized)
        }

    @Test
    fun `if fail to retrieve user data then user can retry`() =
        runTest {
            whenever(userRepository.verifyUser()).then { Resource.Error<VerifyUser>() }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Error)
            assertEquals(pendingApprovalViewModel.state.sendUserToEntrance, Resource.Uninitialized)

            pendingApprovalViewModel.resetVerifyUserResult()

            whenever(userRepository.verifyUser()).then { Resource.Success(basicVerifyUserCannotAddSigners) }

            pendingApprovalViewModel.retrieveUserVerifyDetails()

            assert(pendingApprovalViewModel.state.verifyUserResult is Resource.Success)
            assert(pendingApprovalViewModel.state.sendUserToEntrance is Resource.Uninitialized)

        }
}