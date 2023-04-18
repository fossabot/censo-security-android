package com.censocustody.android.viewModel

import com.censocustody.android.*
import com.censocustody.android.ResourceState.ERROR
import com.censocustody.android.ResourceState.SUCCESS
import com.censocustody.android.common.CensoCountDownTimer
import com.censocustody.android.common.Resource
import com.censocustody.android.data.*
import com.censocustody.android.data.models.ApprovalDisposition
import com.censocustody.android.data.models.RecoveryShard
import com.censocustody.android.data.models.approvalV2.ApprovalRequestV2
import com.censocustody.android.presentation.approval_disposition.ApprovalDispositionState
import com.censocustody.android.presentation.approvals.ApprovalsViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import java.util.*


@OptIn(ExperimentalCoroutinesApi::class)
class ApprovalsViewModelTest : BaseViewModelTest() {

    //region Mocks and testing objects
    @Mock
    lateinit var approvalsRepository: ApprovalsRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var countdownTimer: CensoCountDownTimer

    private lateinit var approvalsViewModel: ApprovalsViewModel

    private val dispatcher = StandardTestDispatcher()
    //endregion

    //region Testing data
    private val testApprovals = getFullListOfApprovalItems()

    private val testApprovalsSize = testApprovals.size
    private val testApprovalsFirstIndex = 0
    private val testApprovalsLastIndex = testApprovals.size - 1

    //These are not used in any way other than to fill method parameters
    private val mockDialogSecondaryMessage = "Send 1000 SOL"
    private val mockDialogMainMessage = "You are about to approve the following request"

    private val validEmail = "sharris@blue.rock"
    //would use the method createDeviceKeyId in the cryptography repository manager
    private val validDeviceId = UUID.randomUUID().toString().replace("-", "")

    private val mockMessages = Pair(mockDialogMainMessage, mockDialogSecondaryMessage)
    //endregion

    //region Before After Work
    @Before
    override fun setUp() = runBlocking {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(approvalsRepository.retrieveShards(any(), any())).thenAnswer { emptyList<RecoveryShard>() }

        whenever(approvalsRepository.approveOrDenyDisposition(any(), any(), any(), any())).thenAnswer {//"edeb9c6e-26cd-41aa-81db-850f0a170295"
            Resource.Success(data = null)
        }

        approvalsViewModel =
            ApprovalsViewModel(
                approvalsRepository = approvalsRepository,
                keyRepository = keyRepository,
                userRepository = userRepository,
                timer = countdownTimer
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    //region Feature Flow Testing
    /**
     * Test retrieving wallet approvals successfully and the state is assigned as expected
     *
     * Using [runTest] here because the method retrieveWalletApprovals() in the [approvalsViewModel] has a delay in it.
     * [runTest] can run code that has delays in it. [runBlocking] is not able to handle code with delays in it.
     *
     * Have to artificially delay the test by 250 ms after making the call to refresh data. This is
     * because we have a delay in the actual code due to the API call happening so fast and not providing the best UX
     *
     * Assertions:
     * - approvals in state should be empty before calling refresh data
     * - the walletApprovalsResult should be of type [Resource.Success], the approvals should NOT be empty and there should be 17 approvals
     */
    @Test
    fun `retrieve wallet approvals successfully then view model state should reflect the retrieved approvals`() = runTest {
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(expectedResourceState = SUCCESS, expectedSize = testApprovalsSize)
    }

    /**
     * Test retrieving wallet approvals but an error occurs, then the state should reflect an empty approvals list
     *
     * Assertions:
     * - approvals in state should be empty before calling refresh data
     * - the walletApprovalsResult should be of type [Resource.Error], the approvals should be empty since there is no cached approvals
     */
    @Test
    fun `retrieve wallet approvals error occurs then view model state should reflect empty approvals list`() = runTest {
        setupApprovalsRepositoryToReturnErrorOnGetApprovals()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = ERROR,
            expectedSize = 0
        )
    }

    /**
     * Test retrieving wallet approvals but an error occurs, but we have cached approvals then the state should reflect the cached approvals
     *
     * Assertions:
     * - approvals in state should be empty before calling refresh data
     * - there should be 17 approvals after refreshing data the first time
     * - after refreshing a second time the walletApprovalsResult should be of type [Resource.Error],
     *   and the approvals should be the cached approvals from the first refresh
     */
    @Test
    fun `retrieve wallet approvals error occurs but there are cached approvals then view model state should reflect cached approvals`() = runTest {
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        //First refresh, assert that the refresh was a success
        approvalsViewModel.refreshData()
        advanceUntilIdle()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = testApprovalsSize
        )

        //Second refresh, assert that the refresh was an error, but the approvals were cached
        setupApprovalsRepositoryToReturnErrorOnGetApprovals()

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        verify(approvalsRepository, times(2)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = ERROR,
            expectedSize = testApprovalsSize
        )
    }

    /**
     * Test resetting the approvals data and refreshing the data, then the view model should reflect the updated data
     *
     * Setup:
     * Get approvals data in state
     *
     * Assertions:
     * - approvals in state should be empty before calling refresh data
     * - there should be 17 approvals after refreshing data the first time
     * - after pulling down data, we wipe the approvals data to emulate a successful disposition, and assert that the data was reset
     * - after the data reset we retrieve the approvals again and assert that there are approvals in the state
     */
    @Test
    fun `reset the approvals data and refresh the data then the view model should reflect the changes`() = runTest {
        //region Get approvals data in state
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = testApprovalsSize
        )
        //endregion

        //Reset the approvals data and assert, then check if the data was refreshed
        approvalsViewModel.wipeDataAfterDispositionSuccess()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())
        assertEquals(true, approvalsViewModel.state.approvalsResultRequest is Resource.Uninitialized)

        //We want to wait for the refresh call to finish
        advanceUntilIdle()

        verify(approvalsRepository, times(2)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = testApprovalsSize
        )
    }

    /**
     * Test setting the selected approval, and then setting a different selected approval, then the view model state should reflect those changes
     *
     * Setup:
     * Get approvals data in state
     *
     * Assertions:
     * - assert that there is no/null selected approval in state
     * - after setting an approval as selected, assert that the selected approval is correct
     * - after setting a other approval as selected, assert that the other approval is the selected approval in state
     * - assert that the disposition is recorded accurately, either [ApprovalDisposition.APPROVE] or [ApprovalDisposition.DENY]
     */
    @Test
    fun `set selected approval then view model state should reflect the change`() = runTest {
        //region Get approvals data in state
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = testApprovalsSize
        )
        //endregion

        //Assert that the selected approval is null
        assertEquals(null, approvalsViewModel.state.selectedApproval)

        val approval = approvalsViewModel.state.approvals[testApprovalsFirstIndex]
        val otherApproval = approvalsViewModel.state.approvals[testApprovalsLastIndex]

        //Set an approval as selected with isApproving = true
        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = approval,
            isApproving = true,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.APPROVE, expectedSelectedApproval = approval
        )

        //Set other approval as selected with isApproving = false
        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = otherApproval,
            isApproving = false,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.DENY, expectedSelectedApproval = otherApproval
        )
    }

    /**
     * Test registering approval disposition successfully then the view model should reflect the success in state
     *
     * Set up:
     * Get 1 approval in state
     * Set the approval as selected approval
     *
     * Assertions:
     * - assert there is no multiple accounts nonce in state
     * - after triggering the register disposition call, assert there is nonce data in state
     * - after approval is approved, assert that the approval result was a success
     */
    @Test
    fun `register approval disposition successfully then view model should reflect the success in state`() = runTest {
        val firstApproval = testApprovals[testApprovalsFirstIndex]
        //region Get 1 approval in state
        whenever(approvalsRepository.getApprovalRequests()).thenAnswer {
            Resource.Success<List<ApprovalRequestV2?>>(
                data = listOf(firstApproval)
            )
        }

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()

        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()

        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = 1
        )
        //endregion

        //region Set first approval as selected approval
        assertEquals(null, approvalsViewModel.state.selectedApproval)

        //Set approval as selected with isApproving = true
        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = firstApproval,
            isApproving = true,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.APPROVE,
            expectedSelectedApproval = firstApproval
        )
        //endregion

        triggerRegisterDispositionCallAndAssertBioPromptState()

        //Let the viewModel coroutine finish the register disposition call
        advanceUntilIdle()

        //Assert that the disposition was a success
        assertEquals(true, approvalsViewModel.state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success)
    }
    //endregion

    //region Focused Testing
    @Test
    fun `call handleScreenForegrounded then view model should call refreshData`() = runTest {
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        approvalsViewModel.handleScreenForegrounded()

        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()
    }

    @Test
    fun `call resetApprovalsData then view model should reflect default state for the respective properties in state`() = runTest {
        assertExpectedDefaultStateForApprovalsData()

        setApprovalsDataInStateAndAssertStateWasSet()

        approvalsViewModel.resetApprovalsData()

        assertExpectedDefaultStateForApprovalsData()
    }

    @Test
    fun `call resetWalletApprovalsResult then view model should reflect uninitialized in state`() = runTest {
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        assertTrue(approvalsViewModel.state.approvalsResultRequest is Resource.Uninitialized)

        approvalsViewModel.refreshData()

        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getApprovalRequests()
        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(SUCCESS, testApprovalsSize)

        approvalsViewModel.resetWalletApprovalsResult()

        assertTrue(approvalsViewModel.state.approvalsResultRequest is Resource.Uninitialized)
    }

    @Test
    fun `call setShouldDisplayConfirmDispositionDialog then view model should reflect updated state properties`() {
        //Have to make a method call to get the data to use in asserts during the test
        val (dialogDetails, approvalDisposition) = approvalsViewModel.getDialogDetailsAndApprovalDispositionType(
            isApproving = true,
            dialogMessages = mockMessages
        )

        val testApproval = testApprovals[0]

        //assert initial state
        assertNull(approvalsViewModel.state.shouldDisplayConfirmDisposition)
        assertNull(approvalsViewModel.state.selectedApproval)
        assertEquals(ApprovalDispositionState(), approvalsViewModel.state.approvalDispositionState)

        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = testApproval,
            isApproving = true,
            dialogMessages = mockMessages
        )

        //assert updated state
        assertEquals(dialogDetails, approvalsViewModel.state.shouldDisplayConfirmDisposition)
        assertEquals(testApproval, approvalsViewModel.state.selectedApproval)
        assertTrue(approvalsViewModel.state.approvalDispositionState?.approvalDisposition is Resource.Success)
        assertEquals(approvalDisposition, approvalsViewModel.state.approvalDispositionState?.approvalDisposition?.data)
    }
    //endregion

    //region Helper methods & Custom Asserts

    private suspend fun setupApprovalsRepositoryToReturnApprovalsOnGetApprovals() {
        whenever(approvalsRepository.getApprovalRequests()).thenAnswer {
            Resource.Success<List<ApprovalRequestV2?>>(
                data = testApprovals
            )
        }
    }

    private suspend fun setupApprovalsRepositoryToReturnErrorOnGetApprovals() {
        whenever(approvalsRepository.getApprovalRequests()).thenAnswer {
            Resource.Error<Any?>(exception = Exception())
        }
    }

    private fun triggerRegisterDispositionCallAndAssertBioPromptState() = runTest {
        triggerBioPromptAndCheckBioPromptState()

        //Trigger the register disposition call (user triggers this when they give biometry approval)
        approvalsViewModel.biometryApproved()
    }

    private suspend fun setApprovalsDataInStateAndAssertStateWasSet() = runTest {
        setupApprovalsRepositoryToReturnApprovalsOnGetApprovals()

        approvalsViewModel.refreshData()

        advanceUntilIdle()

        //Assert expected refresh data
        assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(
            expectedResourceState = SUCCESS,
            expectedSize = testApprovalsSize
        )

        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = approvalsViewModel.state.approvals[testApprovalsFirstIndex],
            isApproving = true,
            dialogMessages = mockMessages
        )

        //Assert expected selectedApproval and disposition
        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.APPROVE,
            expectedSelectedApproval = approvalsViewModel.state.approvals[testApprovalsFirstIndex]
        )

        triggerBioPromptAndCheckBioPromptState()
    }

    private fun triggerBioPromptAndCheckBioPromptState() = runTest {
        //Assert that the bio prompt trigger is uninitialized
        assertTrue(approvalsViewModel.state.bioPromptTrigger is Resource.Uninitialized)

        approvalsViewModel.triggerBioPrompt()

        advanceUntilIdle()

        //Assert the prompt trigger is success
        assertTrue(approvalsViewModel.state.bioPromptTrigger is Resource.Success)
    }

    private fun assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(expectedResourceState: ResourceState, expectedSize: Int) {
        if (expectedResourceState == SUCCESS) {
            assertEquals(true, approvalsViewModel.state.approvalsResultRequest is Resource.Success)
        } else {
            assertEquals(true, approvalsViewModel.state.approvalsResultRequest is Resource.Error)
        }

        assertEquals(expectedSize, approvalsViewModel.state.approvals.size)
    }

    private fun assertExpectedDispositionAndExpectedSelectedApproval(expectedDisposition: ApprovalDisposition, expectedSelectedApproval: ApprovalRequestV2?) {
        assertEquals(expectedSelectedApproval, approvalsViewModel.state.selectedApproval)
        assertEquals(expectedDisposition, approvalsViewModel.state.approvalDispositionState?.approvalDisposition?.data)

        if (expectedDisposition == ApprovalDisposition.APPROVE) {
            assertEquals(true, approvalsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        } else {
            assertEquals(false, approvalsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        }
    }

    private fun assertExpectedDefaultStateForApprovalsData() {
        assertTrue(approvalsViewModel.state.approvals.isEmpty())
        assertTrue(approvalsViewModel.state.approvalsResultRequest is Resource.Uninitialized)
        assertNull(approvalsViewModel.state.selectedApproval)
    }

    //endregion

}