package com.strikeprotocols.mobile.viewModel

import androidx.biometric.BiometricPrompt
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.*
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.*
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approvals.ApprovalsViewModel
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import com.strikeprotocols.mobile.ResourceState.ERROR
import com.strikeprotocols.mobile.ResourceState.SUCCESS
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class ApprovalsViewModelTest : BaseViewModelTest() {

    @Mock
    lateinit var solanaRepository: SolanaRepository

    @Mock
    lateinit var durableNonceViewModel: DurableNonceViewModel

    @Mock
    lateinit var approvalsRepository: ApprovalsRepository

    @Mock
    lateinit var countdownTimer: StrikeCountDownTimer

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var cipher: Cipher

    lateinit var cryptoObject: BiometricPrompt.CryptoObject

    private lateinit var approvalsViewModel: ApprovalsViewModel

    private val dispatcher = StandardTestDispatcher()

    //region Testing data
    private val testApprovals = getWalletApprovals()

    private val testApprovalsSize = testApprovals.size
    private val testApprovalsFirstIndex = 0
    private val testApprovalsLastIndex = testApprovals.size - 1

    private val testNonce = getNonce()

    //These are not used in any way other than to fill method parameters
    private val mockDialogTitle = "Title"
    private val mockDialogText = "Text"
    //endregion

    //region Before After Work
    @Before
    override fun setUp() = runBlocking {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(approvalsRepository.approveOrDenyDisposition(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }
        whenever(approvalsRepository.approveOrDenyInitiation(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }

        approvalsViewModel =
            ApprovalsViewModel(approvalsRepository = approvalsRepository, keyRepository, countdownTimer)

        cryptoObject = BiometricPrompt.CryptoObject(cipher)

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    //TODO:
    // Timer test, created a sub ticket for this

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

        verify(approvalsRepository, times(1)).getWalletApprovals()

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

        verify(approvalsRepository, times(1)).getWalletApprovals()

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

        verify(approvalsRepository, times(2)).getWalletApprovals()

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
        assertEquals(true, approvalsViewModel.state.walletApprovalsResult is Resource.Uninitialized)

        //We want to wait for the refresh call to finish
        advanceUntilIdle()

        verify(approvalsRepository, times(2)).getWalletApprovals()

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

        verify(approvalsRepository, times(1)).getWalletApprovals()

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
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
        )

        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.APPROVE, expectedSelectedApproval = approval
        )

        //Set other approval as selected with isApproving = false
        approvalsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = otherApproval,
            isApproving = false,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        whenever(approvalsRepository.getWalletApprovals()).thenAnswer {
            Resource.Success<List<WalletApproval?>>(
                data = listOf(firstApproval)
            )
        }

        assertEquals(true, approvalsViewModel.state.approvals.isEmpty())

        approvalsViewModel.refreshData()
        advanceUntilIdle()

        verify(approvalsRepository, times(1)).getWalletApprovals()

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
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
        )

        assertExpectedDispositionAndExpectedSelectedApproval(
            expectedDisposition = ApprovalDisposition.APPROVE,
            expectedSelectedApproval = firstApproval
        )
        //endregion

        //Grab data to know if the approval is initiation or regular
        val isInitiationRequest =
            approvalsViewModel.state.selectedApproval?.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Assert that there is no nonce data before setting nonce data
        assertEquals(null, approvalsViewModel.state.multipleAccounts)

        //Set nonce data to trigger the registerDisposition call
        val multipleAccounts = durableNonceViewModel.MultipleAccounts(nonces = listOf(Nonce(testNonce)))
        approvalsViewModel.setMultipleAccounts(multipleAccounts)

        assertEquals(multipleAccounts, approvalsViewModel.state.multipleAccounts)

        //Trigger the register disposition call (user triggers this when they give biometry approval)
        approvalsViewModel.biometryApproved(cryptoObject)

        //Let the viewModel coroutine finish the register disposition call
        advanceUntilIdle()

        //Assert that the disposition was a success
        if (isInitiationRequest) {
            assertEquals(true, approvalsViewModel.state.approvalDispositionState?.initiationDispositionResult is Resource.Success)
        } else {
            assertEquals(true, approvalsViewModel.state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success)
        }
    }

    private suspend fun setupApprovalsRepositoryToReturnApprovalsOnGetApprovals() {
        whenever(approvalsRepository.getWalletApprovals()).thenAnswer {
            Resource.Success<List<WalletApproval?>>(
                data = testApprovals
            )
        }
    }

    private suspend fun setupApprovalsRepositoryToReturnErrorOnGetApprovals() {
        whenever(approvalsRepository.getWalletApprovals()).thenAnswer {
            Resource.Error<Any?>(exception = Exception())
        }
    }


    //Custom Asserts
    private fun assertExpectedWalletApprovalsResultAndExpectedApprovalsSize(expectedResourceState: ResourceState, expectedSize: Int) {
        if (expectedResourceState == SUCCESS) {
            assertEquals(true, approvalsViewModel.state.walletApprovalsResult is Resource.Success)
        } else {
            assertEquals(true, approvalsViewModel.state.walletApprovalsResult is Resource.Error)
        }

        assertEquals(expectedSize, approvalsViewModel.state.approvals.size)
    }

    private fun assertExpectedDispositionAndExpectedSelectedApproval(expectedDisposition: ApprovalDisposition, expectedSelectedApproval: WalletApproval?) {
        assertEquals(expectedSelectedApproval, approvalsViewModel.state.selectedApproval)
        assertEquals(expectedDisposition, approvalsViewModel.state.approvalDispositionState?.approvalDisposition?.data)

        if (expectedDisposition == ApprovalDisposition.APPROVE) {
            assertEquals(true, approvalsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        } else {
            assertEquals(false, approvalsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        }
    }

}