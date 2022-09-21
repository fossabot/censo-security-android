package com.strikeprotocols.mobile.viewModel

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.*
import com.strikeprotocols.mobile.common.BioPromptReason
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.EncryptionManagerImpl.Companion.BIO_KEY_NAME
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.PhraseException
import com.strikeprotocols.mobile.data.PhraseValidator
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.IndexedPhraseWord
import com.strikeprotocols.mobile.presentation.key_management.*
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementState.Companion.NO_PHRASE_ERROR
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementViewModel.Companion.CHANGE_AMOUNT
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementViewModel.Companion.FIRST_WORD_INDEX
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementViewModel.Companion.LAST_WORD_INDEX
import com.strikeprotocols.mobile.presentation.key_management.KeyManagementViewModel.Companion.LAST_WORD_RANGE_SET_INDEX
import com.strikeprotocols.mobile.presentation.key_management.flows.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import junit.framework.TestCase.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import javax.crypto.Cipher

@OptIn(ExperimentalCoroutinesApi::class)
class KeyManagementViewModelTest : BaseViewModelTest() {

    //region Mocks and testing objects
    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    @Mock
    lateinit var phraseValidator: PhraseValidator

    @Mock
    lateinit var cipher: Cipher

    private lateinit var keyMgmtViewModel: KeyManagementViewModel

    private val dispatcher = StandardTestDispatcher()
    //endregion

    //region Testing data
    private val testCreationInitialData = getCreationFlowInitialData()

    private val testRecoveryInitialData = getRecoveryFlowInitialData()

    private val testRegenerationInitialData = getRegenerationFlowInitialData()

    private val testMigrationInitialData = getMigrationFlowInitialData()

    private val testValidPhrase = getValidTestingPhrase()

    private val testInvalidPhrase = getInvalidTestingPhrase()

    private val testWalletSigner = getWalletSigner()

    private val defaultErrorMessage = "test_err_message"
    //endregion

    //region Before After Work
    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        whenever(keyRepository.generatePhrase()).thenAnswer {
            testValidPhrase
        }

        whenever(keyRepository.getCipherForEncryption(BIO_KEY_NAME)).thenAnswer {
            cipher
        }

        whenever(keyRepository.regenerateDataAndUploadToBackend()).thenAnswer {
            Resource.Success(null)
        }

        keyMgmtViewModel =
            KeyManagementViewModel(
                userRepository = userRepository,
                keyRepository = keyRepository,
                phraseValidator = phraseValidator
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    //region Flow testing

    //region Phrase logic methods
    @Test
    fun `as the user types a phrase word ensure that the input is saved properly`() {
        assertEquals("", keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        var wordInput = "p"

        keyMgmtViewModel.updateWordInput(wordInput)

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        wordInput = "ph"

        keyMgmtViewModel.updateWordInput(wordInput )

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        wordInput = "phr"

        keyMgmtViewModel.updateWordInput(wordInput)

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        wordInput = "phra"

        keyMgmtViewModel.updateWordInput(wordInput)

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        wordInput = "phras"

        keyMgmtViewModel.updateWordInput(wordInput)

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        wordInput = "phrase"

        keyMgmtViewModel.updateWordInput(wordInput)

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        //Test that whitespace is removed before updating state
        keyMgmtViewModel.updateWordInput("$wordInput ")

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)

        //Test that capitalized letters are lowercased before updating state
        keyMgmtViewModel.updateWordInput(wordInput.uppercase())

        assertEquals(wordInput, keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)
    }

    @Test
    fun `as the user submits valid phrase words during key creation then ensure that each word is verified`() = runTest {
        //Setup confirm words state
        setCreationFlowDataInStateForConfirmWordsProcessAndAssertChangesInState()

        val phraseWords = testValidPhrase.split(" ")
        var wordsVerified = FIRST_WORD_INDEX

        for ((wordIndex, word) in phraseWords.withIndex()) {
            //Set word in state
            keyMgmtViewModel.updateWordInput(word)

            if (wordIndex != LAST_WORD_INDEX) {
                //Grab next word for assertions
                val nextWordToVerify =
                    keyMgmtViewModel.getPhraseWordAtIndex(
                        phrase = testValidPhrase,
                        index = wordIndex + 1
                    )

                wordsVerified += 1

                assertConfirmPhraseWordsStateAfterCurrentWordIsVerified(
                    nextWordToVerify = nextWordToVerify,
                    wordsVerified = wordsVerified
                )
            }
        }
    }

    @Test
    fun `after the user has submitted all 24 words during creation flow ensure that the bioPrompt is triggered`() = runTest {
        //Setup confirm words state
        setCreationFlowDataInStateForConfirmWordsProcessAndAssertChangesInState()

        val phraseWords = testValidPhrase.split(" ")

        for ((wordIndex, word) in phraseWords.withIndex()) {
            //Set word in state
            keyMgmtViewModel.updateWordInput(word)

            if (wordIndex == LAST_WORD_INDEX) {
                advanceUntilIdle()
                //Assert for all words being verified
                assertEquals(
                    ConfirmPhraseWordsState(),
                    keyMgmtViewModel.state.confirmPhraseWordsState
                )

                assertEquals(
                    KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ALL_SET_STEP),
                    keyMgmtViewModel.state.keyManagementFlowStep
                )
                assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
                assertTriggerBioPromptIsSuccessAndHasCipherData()
            }
        }
    }



    @Test
    fun `after the user pastes a valid phrase during creation flow ensure bioPrompt is triggered`() = runTest {
        whenever(phraseValidator.isPhraseValid(any())).thenAnswer {
            true
        }
        setCreationFlowDataInStateForConfirmWordsProcessAndAssertChangesInState()

        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertEquals("", keyMgmtViewModel.state.pastedPhrase)

        keyMgmtViewModel.verifyPastedPhrase(testValidPhrase)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.pastedPhrase)
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertEquals(BioPromptReason.CREATE_KEY, keyMgmtViewModel.state.bioPromptReason)
        assertTriggerBioPromptIsSuccessAndHasCipherData()
    }

    @Test
    fun `after the user pastes an invalid phrase during creation flow ensure bioPrompt is not triggered`() = runTest {
        whenever(phraseValidator.isPhraseValid(testInvalidPhrase)).thenAnswer {
            false
        }

        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertEquals("", keyMgmtViewModel.state.pastedPhrase)

        keyMgmtViewModel.verifyPastedPhrase(testInvalidPhrase)

        advanceUntilIdle()

        assertEquals(testInvalidPhrase, keyMgmtViewModel.state.pastedPhrase)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
    }

    @Test
    fun `when the user continues to paste an invalid phrase during creation flow ensure that the error state stays set`() = runTest {
        whenever(phraseValidator.isPhraseValid(testInvalidPhrase)).thenAnswer {
            false
        }

        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertEquals("", keyMgmtViewModel.state.pastedPhrase)

        keyMgmtViewModel.verifyPastedPhrase(testInvalidPhrase)

        advanceUntilIdle()

        assertEquals(testInvalidPhrase, keyMgmtViewModel.state.pastedPhrase)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)

        //Attempt to verify the same phrase, and assert that the flowStep state has not changed
        keyMgmtViewModel.verifyPastedPhrase(testInvalidPhrase)

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.CONFIRM_KEY_ERROR_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
    }

    @Test
    fun `after the user pastes a phrase during recovery flow ensure bioPrompt is triggered`() = runTest {
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertNull(keyMgmtViewModel.state.phrase)

        keyMgmtViewModel.verifyPhraseToRecoverKeyPair(testValidPhrase)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.phrase)
        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        assertEquals(BioPromptReason.RECOVER_KEY, keyMgmtViewModel.state.bioPromptReason)
        assertEquals(PhraseInputMethod.PASTED, keyMgmtViewModel.state.inputMethod)
        assertTriggerBioPromptIsSuccessAndHasCipherData()
    }

    @Test
    fun `after the user biometry is approved during recovery flow ensure key recovery is a success`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testRecoveryInitialData)

        advanceUntilIdle()

        assertEquals(testRecoveryInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_RECOVERY, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.recoverKey(cipher)

        advanceUntilIdle()

        verify(keyRepository, times(1)).regenerateAuthDataAndSaveKeyToUser(
            phrase = any(),
            backendPublicKey = any(),
            cipher = any()
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Success)
    }

    @Test
    fun `after the user biometry is approved during creation flow ensure key creation is a success`() = runTest {
        whenever(keyRepository.generateInitialAuthDataAndSaveKeyToUser(any(), any())).thenAnswer {
            testWalletSigner
        }

        whenever(userRepository.addWalletSigner(any())).thenAnswer {
            Resource.Success(data = testWalletSigner)
        }

        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testCreationInitialData)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.phrase)
        assertEquals(testCreationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_CREATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.createAndSaveKey(cipher)

        advanceUntilIdle()

        verify(keyRepository, times(1)).generateInitialAuthDataAndSaveKeyToUser(any(), any())
        verify(userRepository, times(1)).addWalletSigner(any())

        assertEquals(testWalletSigner, keyMgmtViewModel.state.walletSignerToAdd)

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Success)
        assertEquals(testWalletSigner, keyMgmtViewModel.state.finalizeKeyFlow.data)
        assertNull(keyMgmtViewModel.state.phrase)
    }

    //endregion

    //endregion

    //region Focused testing
    @Test
    fun `call onStart and pass in creation flow initial data then view model should reflect that data in state`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testCreationInitialData)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.phrase)
        assertEquals(testCreationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_CREATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call onStart and pass in recovery flow initial data then view model should reflect that data in state`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testRecoveryInitialData)

        advanceUntilIdle()

        assertEquals(testRecoveryInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_RECOVERY, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call onStart and pass in regeneration flow initial data then view model should reflect that data in state`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testRegenerationInitialData)

        advanceUntilIdle()

        assertEquals(testRegenerationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_REGENERATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.RegenerationFlow(KeyRegenerationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call onStart and pass in migration flow initial data then view model should reflect that data in state`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testMigrationInitialData)

        advanceUntilIdle()

        assertEquals(testMigrationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_MIGRATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
    }

    @Test
    fun `call onResume and state is not on phrase copied creation step then view model should not update the state`() {
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.onResume()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call exitPhraseFlow then view model should update goToAccount state property`() {
        callExitPhraseFlowAndAssertChangesInState()
    }

    @Test
    fun `call sendUserBackToStartKeyCreationState then view model should return a key management state object`() {
        val keyManagementState = keyMgmtViewModel.retrieveInitialKeyCreationState()

        //Assert for showToast and keyManagementFlowStep
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyManagementState.keyManagementFlowStep
        )
        assertTrue(keyManagementState.showToast is Resource.Success)
        assertTrue(keyManagementState.showToast.data == KeyManagementState.NO_PHRASE_ERROR)
    }

    @Test
    fun `call phraseFlowAction and pass in WordIndexChanged increasing then view model should increment the word index`() {
        assertEquals(FIRST_WORD_INDEX, keyMgmtViewModel.state.wordIndex)

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = PhraseFlowAction.WordIndexChanged(
                increasing = true
            )
        )

        assertEquals(FIRST_WORD_INDEX + CHANGE_AMOUNT, keyMgmtViewModel.state.wordIndex)
    }

    @Test
    fun `call phraseFlowAction and pass in WordIndexChanged decreasing then view model should decrement the word index`() {
        assertEquals(FIRST_WORD_INDEX, keyMgmtViewModel.state.wordIndex)

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = PhraseFlowAction.WordIndexChanged(
                increasing = false
            )
        )

        //Word index should get set to the last word range set index
        assertEquals(LAST_WORD_RANGE_SET_INDEX, keyMgmtViewModel.state.wordIndex)
    }

    @Test
    fun `call phraseFlowAction and pass in LaunchManualKeyCreation then view model should set creation flow step in state`() {
        assertEquals(FIRST_WORD_INDEX, keyMgmtViewModel.state.wordIndex)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = PhraseFlowAction.LaunchManualKeyCreation
        )

        assertEquals(FIRST_WORD_INDEX, keyMgmtViewModel.state.wordIndex)
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.WRITE_WORD_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call phraseFlowAction and pass in ChangeCreationFlowStep then view model should set new step in state`() {
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.ENTRY_STEP
            )
        )

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call phraseFlowAction and pass in ChangeRecoveryFlowStep then view model should new step in state`() {
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = PhraseFlowAction.ChangeRecoveryFlowStep(
                phraseGenerationFlowStep = KeyRecoveryFlowStep.ENTRY_STEP
            )
        )

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `when user is ready to confirm phrase words during creation flow generate a confirm words state object`() = runTest {
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testCreationInitialData)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.phrase)

        val confirmPhraseWordsState =
            keyMgmtViewModel.generateConfirmPhraseWordsStateForCreationFlow()

        assertEquals(testValidPhrase, confirmPhraseWordsState.phrase)
        assertEquals(testValidPhrase.split(" ")[0], confirmPhraseWordsState.phraseWordToVerify)
        assertEquals(FIRST_WORD_INDEX, confirmPhraseWordsState.wordIndex)
        assertEquals(0, confirmPhraseWordsState.wordsVerified)
        assertEquals("", confirmPhraseWordsState.wordInput)
        assertFalse(confirmPhraseWordsState.errorEnabled)
        assertTrue(confirmPhraseWordsState.isCreationKeyFlow)
    }

    @Test
    fun `when there is a null phrase in state generating a confirm words state object should throw an exception`() {
        try {
            keyMgmtViewModel.generateConfirmPhraseWordsStateForCreationFlow()
        } catch (e: Exception) {
            assertEquals(PhraseException.NULL_PHRASE_IN_STATE, e.message)
        }
    }

    @Test
    fun `when user is ready to confirm phrase words during recovery flow generate a confirm words state object`() {
        val confirmPhraseWordsState =
            keyMgmtViewModel.generateConfirmPhraseWordsStateForRecoveryFlow()

        assertEquals("", confirmPhraseWordsState.phrase)
        assertEquals("", confirmPhraseWordsState.phraseWordToVerify)
        assertEquals(FIRST_WORD_INDEX, confirmPhraseWordsState.wordIndex)
        assertEquals(0, confirmPhraseWordsState.wordsVerified)
        assertEquals("", confirmPhraseWordsState.wordInput)
        assertFalse(confirmPhraseWordsState.errorEnabled)
        assertFalse(confirmPhraseWordsState.isCreationKeyFlow)
    }

    //region Focused tests for flow navigation methods
    @Test
    fun `call createKeyNavigationForward and state is not in creation flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeRecoveryFlowStep(
                phraseGenerationFlowStep = KeyRecoveryFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyCreationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call createKeyNavigationForward then view model should set next step in state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyCreationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.COPY_PHRASE_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyCreationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.PHRASE_COPIED_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call createKeyNavigationBackward and state is not in creation flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeRecoveryFlowStep(
                phraseGenerationFlowStep = KeyRecoveryFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyCreationNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call createKeyNavigationBackward then view model should set previous step in state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.COPY_PHRASE_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.COPY_PHRASE_STEP)
        )

        keyMgmtViewModel.keyCreationNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyCreationNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call recoverKeyNavigationForward and state is not in recovery flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyRecoveryNavigateForward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call recoverKeyNavigationForward then view model should set next step in state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeRecoveryFlowStep(
                phraseGenerationFlowStep = KeyRecoveryFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyRecoveryNavigateForward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyRecoveryNavigateForward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call recoverKeyNavigationBackward and state is not in recovery flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.COPY_PHRASE_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.COPY_PHRASE_STEP)
        )

        keyMgmtViewModel.keyRecoveryNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.COPY_PHRASE_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call recoverKeyNavigationBackward then view model should set previous step in state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeRecoveryFlowStep(
                phraseGenerationFlowStep = KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.CONFIRM_KEY_ENTRY_STEP)
        )

        keyMgmtViewModel.keyRecoveryNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyRecoveryNavigateBackward()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call regenerateKeyNavigationForward and state is not in regeneration flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyRegenerationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call regenerateKeyNavigationForward then view model should set next step in state`() = runTest {
        //region setup code
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testRegenerationInitialData)

        advanceUntilIdle()

        assertEquals(testRegenerationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_REGENERATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.RegenerationFlow(KeyRegenerationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        //endregion

        keyMgmtViewModel.keyRegenerationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.RegenerationFlow(KeyRegenerationFlowStep.FINISHED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyRegenerationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.RegenerationFlow(KeyRegenerationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call migrateKeyNavigationForward and state is not in migration flow then view model should return early and not update state`() {
        callPhraseFlowActionAndAssertChangesInState(
            phraseFlowAction = PhraseFlowAction.ChangeCreationFlowStep(
                phraseVerificationFlowStep = KeyCreationFlowStep.ENTRY_STEP
            ),
            flowStep = KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP)
        )

        keyMgmtViewModel.keyMigrationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    @Test
    fun `call migrateKeyNavigationForward then view model should set next step in state`() = runTest {
        //region setup code
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testMigrationInitialData)

        advanceUntilIdle()

        assertEquals(testMigrationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_MIGRATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        //endregion

        keyMgmtViewModel.keyMigrationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.FINISHED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.keyMigrationNavigateForward()

        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }
    //endregion

    //region Reset state methods
    @Test
    fun `call resetGoToAccount then view model should set goToAccount property to default state`() {
        callExitPhraseFlowAndAssertChangesInState()

        keyMgmtViewModel.resetGoToAccount()

        assertTrue(keyMgmtViewModel.state.goToAccount is Resource.Uninitialized)
    }

    @Test
    fun `after key flow is finished or regenerate data fails, reset state property`() = runTest {
        //region Assert default state + setup finalizeKeyFlow in state
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testMigrationInitialData)

        advanceUntilIdle()

        assertEquals(testMigrationInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_MIGRATION, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        //endregion

        //call reset method
        keyMgmtViewModel.resetAddWalletSignersCall()

        //Assert reset state
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Uninitialized)
    }

    @Test
    fun `after toast is shown reset show toast state property`() = runTest {
        assertTrue(keyMgmtViewModel.state.showToast is Resource.Uninitialized)

        //This method should fail early because there is no phrase in state.
        // This will trigger the showToast property to be set in state
        keyMgmtViewModel.createAndSaveKey(cipher)

        assertTrue(keyMgmtViewModel.state.showToast is Resource.Success)
        assertEquals(NO_PHRASE_ERROR, keyMgmtViewModel.state.showToast.data)
    }

    @Test
    fun `after user agrees to biometry prompt then reset prompt trigger state property`() = runTest {
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)

        //Initial bio prompt trigger
        keyMgmtViewModel.triggerBioPromptForCreate()

        assertTriggerBioPromptIsSuccessAndHasCipherData()

        keyMgmtViewModel.resetPromptTrigger()

        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)

        //Trigger bio prompt again
        keyMgmtViewModel.triggerBioPromptForRecover(PhraseInputMethod.MANUAL)

        advanceUntilIdle()

        assertTriggerBioPromptIsSuccessAndHasCipherData()

        keyMgmtViewModel.resetPromptTrigger()

        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
    }

    @Test
    fun `when a key recovery failure occurs and the user dismisses the error ui reset the error state`() = runTest {
        //default state + setup
        assertTrue(keyMgmtViewModel.state.keyRecoveryManualEntryError is Resource.Uninitialized)

        keyMgmtViewModel.triggerBioPromptForRecover(PhraseInputMethod.MANUAL)

        advanceUntilIdle()

        assertTriggerBioPromptIsSuccessAndHasCipherData()

        //Set keyRecoveryManualEntryError in state
        keyMgmtViewModel.recoverKeyFailure()

        assertTrue(keyMgmtViewModel.state.keyRecoveryManualEntryError is Resource.Error)

        //Reset state
        keyMgmtViewModel.resetRecoverManualEntryError()

        assertTrue(keyMgmtViewModel.state.keyRecoveryManualEntryError is Resource.Uninitialized)
    }
    //endregion

    //region Retry methods
    @Test
    fun `after error occurs during key migration then ensure error state is set`() = runTest {
        whenever(keyRepository.migrateOldDataToBiometryProtectedStorage(any())).thenAnswer {
            throw Exception(defaultErrorMessage)
        }

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Uninitialized)
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertEquals(BioPromptReason.UNINITIALIZED, keyMgmtViewModel.state.bioPromptReason)

        keyMgmtViewModel.triggerBioPromptForMigration()

        advanceUntilIdle()

        assertTriggerBioPromptIsSuccessAndHasCipherData()

        //Calling this method will trigger
        // migrateOldDataToBiometryProtectedStorage() to be called and an exception to be thrown
        keyMgmtViewModel.biometryApproved(cipher)

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Error)
        assertEquals(defaultErrorMessage, keyMgmtViewModel.state.finalizeKeyFlow.exception?.message)
    }

    @Test
    fun `after error occurs during key migration then retrying should trigger bio prompt`() = runTest {
        whenever(keyRepository.migrateOldDataToBiometryProtectedStorage(any())).thenAnswer {
            throw Exception(defaultErrorMessage)
        }

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Uninitialized)
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Uninitialized)
        assertEquals(BioPromptReason.UNINITIALIZED, keyMgmtViewModel.state.bioPromptReason)

        keyMgmtViewModel.triggerBioPromptForMigration()

        advanceUntilIdle()

        assertTriggerBioPromptIsSuccessAndHasCipherData()

        //Calling this method will trigger
        // migrateOldDataToBiometryProtectedStorage() to be called and an exception to be thrown
        keyMgmtViewModel.biometryApproved(cipher)

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Error)
        assertEquals(defaultErrorMessage, keyMgmtViewModel.state.finalizeKeyFlow.exception?.message)

        //Now attempt retry and assert that bioPromptTrigger is set again
        keyMgmtViewModel.retryKeyMigration()

        advanceUntilIdle()

        assertEquals(
            KeyManagementFlowStep.MigrationFlow(KeyMigrationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        assertEquals(BioPromptReason.MIGRATE_BIOMETRIC_KEY, keyMgmtViewModel.state.bioPromptReason)
        assertTriggerBioPromptIsSuccessAndHasCipherData()
    }

    @Test
    fun `after error occurs during key recovery then retrying should trigger bio prompt`() = runTest {
        whenever(keyRepository.regenerateAuthDataAndSaveKeyToUser(any(), any(), any())).thenAnswer {
            throw Exception(defaultErrorMessage)
        }

        //assert default state then setup recovery flow initial state
        assertDefaultStateForInitialDataProperties()

        keyMgmtViewModel.onStart(testRecoveryInitialData)

        advanceUntilIdle()

        assertEquals(testRecoveryInitialData, keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.KEY_RECOVERY, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.keyRecoveryManualEntryError is Resource.Uninitialized)

        //Attempt to recover the key, an exception will be thrown
        keyMgmtViewModel.recoverKey(cipher)

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.keyRecoveryManualEntryError is Resource.Error)
        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ENTRY_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.retryKeyRecoveryFromPhrase()

        advanceUntilIdle()

        assertEquals(
            KeyManagementFlowStep.RecoveryFlow(KeyRecoveryFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        assertEquals(PhraseInputMethod.PASTED, keyMgmtViewModel.state.inputMethod)
        assertEquals(BioPromptReason.RECOVER_KEY, keyMgmtViewModel.state.bioPromptReason)
        assertTriggerBioPromptIsSuccessAndHasCipherData()
    }

    @Test
    fun `after error occurs during key creation then retrying should trigger bio prompt`() = runTest {
        whenever(keyRepository.generateInitialAuthDataAndSaveKeyToUser(any(), any())).thenAnswer {
            throw Exception(defaultErrorMessage)
        }

        setCreationFlowDataInStateForConfirmWordsProcessAndAssertChangesInState()

        keyMgmtViewModel.createAndSaveKey(cipher)

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Error)

        keyMgmtViewModel.retryKeyCreationFromPhrase()

        advanceUntilIdle()

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.ALL_SET_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )
        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Loading)
        assertEquals(BioPromptReason.CREATE_KEY, keyMgmtViewModel.state.bioPromptReason)
        assertTriggerBioPromptIsSuccessAndHasCipherData()
    }

    @Test
    fun `after error occurs during key regeneration then retrying should trigger bio prompt`() = runTest {
        whenever(keyRepository.regenerateDataAndUploadToBackend()).thenAnswer {
            Resource.Error(null)
        }

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Uninitialized)

        //Calling onStart with regeneration initial data will trigger the regenerateData method for us
        keyMgmtViewModel.onStart(testRegenerationInitialData)

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Error)

        whenever(keyRepository.regenerateDataAndUploadToBackend()).thenAnswer {
            Resource.Success(testWalletSigner)
        }

        keyMgmtViewModel.retryRegenerateData()

        advanceUntilIdle()

        assertTrue(keyMgmtViewModel.state.finalizeKeyFlow is Resource.Success)
        assertEquals(testWalletSigner, keyMgmtViewModel.state.finalizeKeyFlow.data)
    }
    //endregion

    //endregion

    //region Helper methods
    private fun assertDefaultStateForInitialDataProperties() {
        assertNull(keyMgmtViewModel.state.phrase)
        assertNull(keyMgmtViewModel.state.initialData)
        assertEquals(KeyManagementFlow.UNINITIALIZED, keyMgmtViewModel.state.keyManagementFlow)
        assertEquals(KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED), keyMgmtViewModel.state.keyManagementFlowStep)
    }

    private fun assertGoToAccountIsUninitialized() {
        assertTrue(keyMgmtViewModel.state.goToAccount is Resource.Uninitialized)
    }

    private fun assertGoToAccountIsSuccessAndHasData() {
        assertTrue(keyMgmtViewModel.state.goToAccount is Resource.Success)
        assertTrue(keyMgmtViewModel.state.goToAccount.data == true)
    }

    private fun assertTriggerBioPromptIsSuccessAndHasCipherData() {
        assertTrue(keyMgmtViewModel.state.triggerBioPrompt is Resource.Success)
        assertEquals(cipher, keyMgmtViewModel.state.triggerBioPrompt.data)
    }

    private fun callExitPhraseFlowAndAssertChangesInState() {
        assertGoToAccountIsUninitialized()

        keyMgmtViewModel.exitPhraseFlow()

        assertGoToAccountIsSuccessAndHasData()
    }

    private fun callPhraseFlowActionAndAssertChangesInState(phraseFlowAction: PhraseFlowAction, flowStep: KeyManagementFlowStep) {
        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.UNINITIALIZED),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        keyMgmtViewModel.phraseFlowAction(
            phraseFlowAction = phraseFlowAction
        )

        assertEquals(
            flowStep,
            keyMgmtViewModel.state.keyManagementFlowStep
        )
    }

    private fun setCreationFlowDataInStateForConfirmWordsProcessAndAssertChangesInState() = runTest {
        assertDefaultStateForInitialDataProperties()

        //Set initial data for creation flow
        keyMgmtViewModel.onStart(testCreationInitialData)

        advanceUntilIdle()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.phrase)

        //Set verify words step
        keyMgmtViewModel.phraseFlowAction(
            PhraseFlowAction.ChangeCreationFlowStep(
                KeyCreationFlowStep.VERIFY_WORDS_STEP
            )
        )

        assertEquals(
            KeyManagementFlowStep.CreationFlow(KeyCreationFlowStep.VERIFY_WORDS_STEP),
            keyMgmtViewModel.state.keyManagementFlowStep
        )

        //Trigger confirm phrase words state generation
        keyMgmtViewModel.keyCreationNavigateForward()

        assertEquals(testValidPhrase, keyMgmtViewModel.state.confirmPhraseWordsState.phrase)
        assertEquals(
            testValidPhrase.split(" ")[0],
            keyMgmtViewModel.state.confirmPhraseWordsState.phraseWordToVerify
        )
        assertEquals(FIRST_WORD_INDEX, keyMgmtViewModel.state.confirmPhraseWordsState.wordIndex)
        assertEquals(0, keyMgmtViewModel.state.confirmPhraseWordsState.wordsVerified)
        assertEquals("", keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)
        assertFalse(keyMgmtViewModel.state.confirmPhraseWordsState.errorEnabled)
        assertTrue(keyMgmtViewModel.state.confirmPhraseWordsState.isCreationKeyFlow)
    }

    private fun assertConfirmPhraseWordsStateAfterCurrentWordIsVerified(nextWordToVerify: IndexedPhraseWord, wordsVerified: Int) {
        assertEquals(
            nextWordToVerify.wordValue,
            keyMgmtViewModel.state.confirmPhraseWordsState.phraseWordToVerify
        )
        assertEquals(
            nextWordToVerify.wordIndex,
            keyMgmtViewModel.state.confirmPhraseWordsState.wordIndex
        )
        assertEquals(wordsVerified, keyMgmtViewModel.state.confirmPhraseWordsState.wordsVerified)
        assertEquals("", keyMgmtViewModel.state.confirmPhraseWordsState.wordInput)
    }
    //endregion
}