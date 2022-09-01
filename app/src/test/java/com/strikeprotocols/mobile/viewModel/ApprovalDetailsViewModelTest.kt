package com.strikeprotocols.mobile.viewModel

import androidx.biometric.BiometricPrompt
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.*
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeCountDownTimer
import com.strikeprotocols.mobile.data.ApprovalsRepository
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.SolanaRepository
import com.strikeprotocols.mobile.data.models.ApprovalDisposition
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.data.models.approval.SolanaApprovalRequestDetails
import com.strikeprotocols.mobile.data.models.approval.WalletApproval
import com.strikeprotocols.mobile.presentation.approval_detail.ApprovalDetailsViewModel
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertEquals
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

    @Mock
    lateinit var solanaRepository: SolanaRepository

    @Mock
    lateinit var durableNonceViewModel: DurableNonceViewModel

    @Mock
    lateinit var approvalsRepository: ApprovalsRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var cipher: Cipher

    lateinit var cryptoObject: BiometricPrompt.CryptoObject

    private lateinit var approvalDetailsViewModel: ApprovalDetailsViewModel

    private val dispatcher = StandardTestDispatcher()

    @Mock
    lateinit var countdownTimer: StrikeCountDownTimer

    //region Testing data
    private val testLoginApproval = getLoginApproval()
    private val testRemoveDAppBookEntryApproval = getRemoveDAppBookEntryApproval()
    private val testMultiSigBalanceAccountCreationWalletApproval =
        getMultiSigBalanceAccountCreationWalletApproval()

    private lateinit var testMultipleAccounts: DurableNonceViewModel.MultipleAccounts

    //These are not used in any way other than to fill method parameters
    private val mockDialogTitle = "Title"
    private val mockDialogText = "Text"
    //endregion

    //region
    @Before
    override fun setUp() = runBlocking {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(keyRepository.getCipherForDecryption()).thenAnswer {
            cipher
        }
        approvalDetailsViewModel =
            ApprovalDetailsViewModel(approvalsRepository = approvalsRepository, keyRepository, countdownTimer)

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)

        testMultipleAccounts =  durableNonceViewModel.MultipleAccounts(nonces = listOf(Nonce(getNonce())))

        cryptoObject = BiometricPrompt.CryptoObject(cipher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

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
        approvalDetailsViewModel.setArgsToState(testLoginApproval)

        assertExpectedApprovalInState(expectedApproval = testLoginApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testLoginApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        approvalDetailsViewModel.setArgsToState(testRemoveDAppBookEntryApproval)

        assertExpectedApprovalInState(expectedApproval = testRemoveDAppBookEntryApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testRemoveDAppBookEntryApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        approvalDetailsViewModel.setArgsToState(testMultiSigBalanceAccountCreationWalletApproval)

        assertExpectedApprovalInState(expectedApproval = testMultiSigBalanceAccountCreationWalletApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigBalanceAccountCreationWalletApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        approvalDetailsViewModel.setArgsToState(testMultiSigBalanceAccountCreationWalletApproval)

        assertExpectedApprovalInState(expectedApproval = testMultiSigBalanceAccountCreationWalletApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigBalanceAccountCreationWalletApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        approvalDetailsViewModel.setArgsToState(testRemoveDAppBookEntryApproval)

        assertExpectedApprovalInState(expectedApproval = testRemoveDAppBookEntryApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testRemoveDAppBookEntryApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = false,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
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
        approvalDetailsViewModel.setArgsToState(testMultiSigBalanceAccountCreationWalletApproval)

        assertExpectedApprovalInState(expectedApproval = testMultiSigBalanceAccountCreationWalletApproval)

        //Grab data to know if approval is initiation or regular
        val isInitiationRequest =
            testMultiSigBalanceAccountCreationWalletApproval.details is SolanaApprovalRequestDetails.MultiSignOpInitiationDetails

        //Set isApproving in state
        approvalDetailsViewModel.setShouldDisplayConfirmDispositionDialog(
            isApproving = true,
            isInitiationRequest = isInitiationRequest,
            dialogTitle = mockDialogTitle,
            dialogText = mockDialogText
        )

        assertExpectedDispositionAndExpectedInitiation(ApprovalDisposition.APPROVE, isInitiationRequest)

        triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState()

        advanceUntilIdle()

        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.initiationDispositionResult is Resource.Error)
        assertEquals(true, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isApproving)
        assertEquals(isInitiationRequest, approvalDetailsViewModel.state.approvalDispositionState?.approvalRetryData?.isInitiationRequest)
    }

    //Helper method
    private fun triggerRegisterDispositionCallAndAssertNonceDataAndBioPromptState() = runTest {
        assertEquals(null, approvalDetailsViewModel.state.multipleAccounts)
        assertTrue(approvalDetailsViewModel.state.bioPromptTrigger is Resource.Uninitialized)

        approvalDetailsViewModel.setMultipleAccounts(testMultipleAccounts)

        advanceUntilIdle()

        assertEquals(testMultipleAccounts, approvalDetailsViewModel.state.multipleAccounts)
        assertTrue(approvalDetailsViewModel.state.bioPromptTrigger is Resource.Success)

        approvalDetailsViewModel.biometryApproved(cryptoObject)
    }

    //Custom Asserts
    private fun assertApprovalInStateIsNull() {
        assertEquals(null, approvalDetailsViewModel.state.approval)
    }

    private fun assertExpectedApprovalInState(expectedApproval: WalletApproval) {
        assertEquals(expectedApproval, approvalDetailsViewModel.state.approval)
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

}