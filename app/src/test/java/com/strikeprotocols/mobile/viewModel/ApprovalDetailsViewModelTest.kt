package com.strikeprotocols.mobile.viewModel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.*
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.SolanaRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.CipherRepository
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.ApprovalRequest
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel
import com.strikeprotocols.mobile.presentation.approval_disposition.ApprovalDispositionState
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class ApprovalDetailsViewModelTest : BaseViewModelTest() {

    //region Mocks and testing objects
    @Mock
    lateinit var solanaRepository: SolanaRepository

    @Mock
    lateinit var durableNonceViewModel: DurableNonceViewModel

    @Mock
    lateinit var approvalsRepository: ApprovalsRepository

    @Mock
    lateinit var cipherRepository: CipherRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var cipher: Cipher

    @Mock
    lateinit var countdownTimer: StrikeCountDownTimer

    private lateinit var approvalDetailsViewModel: ApprovalDetailsViewModel

    private val dispatcher = StandardTestDispatcher()
    //endregion

    //region Testing data
    private val testLoginApproval = getLoginApproval()
    private val testRemoveDAppBookEntryApproval = getRemoveDAppBookEntryApproval()
    private val testMultiSigWalletCreationApprovalRequest =
        getMultiSigWalletCreationApprovalRequest()

    private lateinit var testMultipleAccounts: DurableNonceViewModel.MultipleAccounts

    //These are not used in any way other than to fill method parameters
    private val mockDialogSecondaryMessage = "Send 1000 SOL"
    private val mockDialogMainMessage = "You are about to approve the following request"

    private val mockMessages = Pair(mockDialogMainMessage, mockDialogSecondaryMessage)
    //endregion

    //region Setup TearDown
    @Before
    override fun setUp() = runBlocking {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(cipherRepository.getCipherForV3RootSeedDecryption()).thenAnswer {
            cipher
        }

        approvalDetailsViewModel =
            ApprovalDetailsViewModel(
                approvalsRepository = approvalsRepository,
                keyRepository = keyRepository,
                cipherRepository = cipherRepository,
                timer = countdownTimer
            )

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)

        testMultipleAccounts =  durableNonceViewModel.MultipleAccounts(nonces = listOf(Nonce(getNonce())))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    //region Feature Flow Testing
    /**
     * Test approving an approval successfully then the view model should reflect the success in state
     *
     * Assertions:
     * - assert that the approval in state is null
     * - set approval to state and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result was a success
     */
    @Test
    fun `approve an approval successfully then view model should reflect the success in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyDisposition(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testLoginApproval)

        assertExpectedApprovalInState(expectedApproval = testLoginApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testLoginApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.APPROVE, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success)
    }

    /**
     * Test denying an approval successfully then the view model should reflect the success in state
     *
     * Assertions:
     * - assert that the approval in state is null
     * - set approval to state and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result was a success
     */
    @Test
    fun `deny an approval successfully then view model should reflect the success in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyDisposition(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testRemoveDAppBookEntryApproval)

        assertExpectedApprovalInState(expectedApproval = testRemoveDAppBookEntryApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testRemoveDAppBookEntryApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.DENY, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Success)
    }

    /**
     * Test approving a multi signature approval successfully then the view model should reflect the success in state
     *
     * Assertions:
     * - assert that the approval in state is null
     * - set approval to state and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result was a success
     */
    @Test
    fun `approve a multi sig approval successfully then view model should reflect the success in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyInitiation(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testMultiSigWalletCreationApprovalRequest)

        assertExpectedApprovalInState(expectedApproval = testMultiSigWalletCreationApprovalRequest)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigWalletCreationApprovalRequest.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.APPROVE, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assert(approvalDetailsViewModel.state.approvalDispositionState?.initiationDispositionResult is Resource.Success)
    }

    /**
     * Test denying a multi signature approval successfully then the view model should reflect the success in state
     *
     * Assertions:
     * - assert that the approval in state is null
     * - set approval to state and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result was a success
     */
    @Test
    fun `deny a multi sig approval successfully then view model should reflect the success in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyInitiation(any(), any(), any())).thenAnswer {
            Resource.Success(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testMultiSigWalletCreationApprovalRequest)

        assertExpectedApprovalInState(expectedApproval = testMultiSigWalletCreationApprovalRequest)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigWalletCreationApprovalRequest.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.DENY, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.initiationDispositionResult is Resource.Success)
    }

    /**
     * Test denying an approval but api error occurs then view model should hold retry data and reflect error in state
     *
     * Assertions:
     * - assert there is no approval in state
     * - set approval and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result is a error and there is retry data in state
     */
    @Test
    fun `deny an approval but api error occurs then view model should hold retry data and reflect error in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyDisposition(any(), any(), any())).thenAnswer {
            Resource.Error(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testRemoveDAppBookEntryApproval)

        assertExpectedApprovalInState(expectedApproval = testRemoveDAppBookEntryApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testRemoveDAppBookEntryApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.DENY, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        //Assert that the registerDisposition call failed and the retry data holds the disposition and isInitiationRequest data
        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.registerApprovalDispositionResult is Resource.Error)
        assertEquals(false, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isApproving)
        assertEquals(isInitiationRequest, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isInitiationRequest)
    }

    /**
     * Test approving a multi signature approval but api error occurs then view model should hold retry data and reflect error in state
     *
     * Assertions:
     * - assert there is no approval in state
     * - set approval and assert it was set
     * - assert expected disposition was set in state
     * - assert that there is no nonce data in state
     * - set nonce data to trigger disposition call and assert that the nonce data is set
     * - assert the register disposition result is a error and there is retry data in state
     */
    @Test
    fun `approve a multi sig approval but api error occurs then view model should hold retry data and reflect error in state`() = runTest {
        whenever(approvalsRepository.approveOrDenyInitiation(any(), any(), any())).thenAnswer {
            Resource.Error(data = null)
        }

        assertApprovalInStateIsNull()

        //Set approval in state
        approvalDetailsViewModel.handleInitialData(testMultiSigWalletCreationApprovalRequest)

        assertExpectedApprovalInState(expectedApproval = testMultiSigWalletCreationApprovalRequest)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigWalletCreationApprovalRequest.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogMessages = mockMessages,
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.APPROVE, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.initiationDispositionResult is Resource.Error)
        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isApproving)
        assertEquals(isInitiationRequest, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isInitiationRequest)
    }

    /**
     * Test the scenario of a user leaving the details screen and selecting a new approval to view the details
     *
     * VM has to be re initialized before calling onStart.
     * We need to do this to mock how the screen would instantiate a new VM instance
     * when a new approval is selected for viewing the details
     */
    @Test
    fun `set an initial approval, set a new initial approval then view model should reflect the approvals in state`() {
        assertNull(approvalDetailsViewModel.state.selectedApproval)

        approvalDetailsViewModel.onStart(testLoginApproval)

        assertEquals(testLoginApproval, approvalDetailsViewModel.state.selectedApproval)

        approvalDetailsViewModel.wipeDataAndKickUserOutToApprovalsScreen()

        assertNull(approvalDetailsViewModel.state.selectedApproval)

        approvalDetailsViewModel.onStart(testRemoveDAppBookEntryApproval)

        assertEquals(testRemoveDAppBookEntryApproval, approvalDetailsViewModel.state.selectedApproval)
    }

    //endregion

    //region Focused Testing
    @Test
    fun `call handleScreenBackgrounded then view model should set screen backgrounded in state`() {
        assertFalse(approvalDetailsViewModel.state.screenWasBackgrounded)

        approvalDetailsViewModel.handleScreenBackgrounded()

        assertTrue(approvalDetailsViewModel.state.screenWasBackgrounded)
    }

    @Test
    fun `call handleScreenForegrounded after the screen was backgrounded then view model should set state to kick out user`() {
        assertFalse(approvalDetailsViewModel.state.screenWasBackgrounded)

        approvalDetailsViewModel.handleScreenBackgrounded()

        assertTrue(approvalDetailsViewModel.state.screenWasBackgrounded)

        approvalDetailsViewModel.handleScreenForegrounded()

        assertFalse(approvalDetailsViewModel.state.screenWasBackgrounded)
        assertTrue(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)
    }

    @Test
    fun `call handleScreenForegrounded when the screen was not backgrounded then view model should do nothing`() {
        assertFalse(approvalDetailsViewModel.state.screenWasBackgrounded)

        approvalDetailsViewModel.handleScreenForegrounded()

        assertFalse(approvalDetailsViewModel.state.screenWasBackgrounded)
    }

    @Test
    fun `call handleInitialData then view model should set approval in state`() {
        assertNull(approvalDetailsViewModel.state.selectedApproval)

        approvalDetailsViewModel.handleInitialData(testLoginApproval)

        assertEquals(testLoginApproval, approvalDetailsViewModel.state.selectedApproval)
    }

    @Test
    fun `call handleEmptyInitialData then view model should call setShouldKickOutUser and set the property in state`() {
        assertFalse(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)

        approvalDetailsViewModel.handleEmptyInitialData()

        assertTrue(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)
    }

    @Test
    fun `call setShouldKickOutUser then view model should call setShouldKickOutUser and set the property in state`() {
        assertFalse(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)

        approvalDetailsViewModel.setShouldKickOutUser()

        assertTrue(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)
    }

    @Test
    fun `call resetShouldKickOutUser then view model should reflect the reset data in state`() {
        assertFalse(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)

        approvalDetailsViewModel.setShouldKickOutUser()

        assertTrue(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)

        approvalDetailsViewModel.resetShouldKickOutUser()

        assertFalse(approvalDetailsViewModel.state.shouldKickOutUserToApprovalsScreen)
    }

    @Test
    fun `call setShouldDisplayConfirmDispositionDialog then view model should reflect updated state properties`() {
        //Have to make a method call to get the data to use in asserts during the test
        val (dialogDetails, approvalDisposition) = approvalDetailsViewModel.getDialogDetailsAndApprovalDispositionType(
            isApproving = true,
            dialogMessages = mockMessages
        )

        //assert initial state
        assertNull(approvalDetailsViewModel.state.shouldDisplayConfirmDisposition)
        assertEquals(ApprovalDispositionState(), approvalDetailsViewModel.state.approvalDispositionState)

        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            approval = testLoginApproval,
            isInitiationRequest = false,
            isApproving = true,
            dialogMessages = mockMessages
        )

        //assert updated state
        assertEquals(dialogDetails, approvalDetailsViewModel.state.shouldDisplayConfirmDisposition)
        assertTrue(approvalDetailsViewModel.state.approvalDispositionState?.approvalDisposition is Resource.Success)
        assertEquals(approvalDisposition, approvalDetailsViewModel.state.approvalDispositionState?.approvalDisposition?.data)
        assertTrue(approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isApproving == true)
        assertTrue(approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isInitiationRequest == false)
    }
    //endregion

    //region Helper method

    private fun triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState() = runTest {
        assertEquals(null, approvalDetailsViewModel.state.multipleAccounts)
        assertTrue(approvalDetailsViewModel.state.bioPromptTrigger is Resource.Uninitialized)

        approvalDetailsViewModel.setMultipleAccounts(testMultipleAccounts)

        advanceUntilIdle()

        assertEquals(testMultipleAccounts, approvalDetailsViewModel.state.multipleAccounts)
        assertTrue(approvalDetailsViewModel.state.bioPromptTrigger is Resource.Success)

        approvalDetailsViewModel.biometryApproved(cipher)
    }
    //endregion

    //region Custom Asserts
    private fun assertApprovalInStateIsNull() {
        assertEquals(null, approvalDetailsViewModel.state.selectedApproval)
    }

    private fun assertExpectedApprovalInState(expectedApproval: ApprovalRequest) {
        assertEquals(expectedApproval, approvalDetailsViewModel.state.selectedApproval)
    }

    private fun assertExpectedDispositionAndExpectedInitiation(expectedDisposition: ApprovalDisposition, expectedIsInitiationRequest: Boolean) {
        assertEquals(expectedDisposition, approvalDetailsViewModel.state.approvalDispositionState?.approvalDisposition?.data)
        assertEquals(expectedIsInitiationRequest, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isInitiationRequest)

        if (expectedDisposition == ApprovalDisposition.APPROVE) {
            assertEquals(true, approvalDetailsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        } else {
            assertEquals(false, approvalDetailsViewModel.state.shouldDisplayConfirmDisposition?.isApproving)
        }
    }
    //endregion

}