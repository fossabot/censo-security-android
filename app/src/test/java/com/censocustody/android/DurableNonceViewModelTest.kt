package com.censocustody.android

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.censocustody.android.common.Resource
import com.censocustody.android.data.SolanaRepository
import com.censocustody.android.data.models.MultipleAccountsResponse
import com.censocustody.android.data.models.Nonce
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel
import com.censocustody.android.presentation.durable_nonce.DurableNonceViewModel.Companion.UNABLE_TO_RETRIEVE_VALID_NONCE
import com.censocustody.android.viewModel.BaseViewModelTest
import junit.framework.Assert.assertEquals
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@OptIn(ExperimentalCoroutinesApi::class)
class DurableNonceViewModelTest : BaseViewModelTest() {

    //region Testing data
    private val dispatcher = TestCoroutineDispatcher()

    lateinit var durableNonceViewModel: DurableNonceViewModel

    @Mock
    lateinit var solanaRepository: SolanaRepository

    private val minimumSlotInfo = 5

    private val nonceAccountAddresses = listOf(getNonce())

    private val validMultipleResponseData =
        MultipleAccountsResponse(
            nonces = listOf(Nonce("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")),
            slot = minimumSlotInfo + 1
        )

    private val invalidMultipleResponseData =
        MultipleAccountsResponse(
            nonces = listOf(Nonce("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")),
            slot = minimumSlotInfo - 1
        )
    //endregion

    //region Setup + tearDown
    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockitoAnnotations.openMocks(this)

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    //region Focused testing
    @Test
    fun `setting initial data then view model state should reflect that data`() {
        //Assert state with default initialization
        TestCase.assertTrue(durableNonceViewModel.state.nonceAccountAddresses.isEmpty())
        TestCase.assertEquals(0, durableNonceViewModel.state.minimumNonceAccountAddressesSlot)

        durableNonceViewModel.setInitialData(
            nonceAccountAddresses = nonceAccountAddresses,
            minimumNonceAccountAddressesSlot = minimumSlotInfo
        )

        //Assert state is set with data
        TestCase.assertTrue(durableNonceViewModel.state.nonceAccountAddresses.isNotEmpty())
        TestCase.assertEquals(minimumSlotInfo, durableNonceViewModel.state.minimumNonceAccountAddressesSlot)

    }

    @Test
    fun `resetting data then view model state should reflect default initialization data`() {
        durableNonceViewModel.setInitialData(
            nonceAccountAddresses = nonceAccountAddresses,
            minimumNonceAccountAddressesSlot = minimumSlotInfo
        )

        //Assert state has data set
        TestCase.assertTrue(durableNonceViewModel.state.nonceAccountAddresses.isNotEmpty())
        TestCase.assertEquals(minimumSlotInfo, durableNonceViewModel.state.minimumNonceAccountAddressesSlot)

        durableNonceViewModel.resetState()

        //Assert state has default data
        TestCase.assertTrue(durableNonceViewModel.state.nonceAccountAddresses.isEmpty())
        TestCase.assertEquals(0, durableNonceViewModel.state.minimumNonceAccountAddressesSlot)
    }

    @Test
    fun `resetting multipleAccountsResult then view model state should reflect uninitialized for property`() = runBlocking {
        setupMultipleAccountsResultWithSuccessData()

        assert(durableNonceViewModel.state.multipleAccountsResult is Resource.Success)
        assertEquals(
            (durableNonceViewModel.state.multipleAccountsResult as Resource.Success).data,
            validMultipleResponseData
        )

        durableNonceViewModel.resetMultipleAccountsResource()

        //Assert multipleAccountsResult has been reset
        TestCase.assertTrue(durableNonceViewModel.state.multipleAccountsResult is Resource.Uninitialized)
    }
    //endregion

    //region Flow/Functional testing
    @Test
    fun testRetrievesValidNonceData() = runBlocking {
        setupMultipleAccountsResultWithSuccessData()

        verify(solanaRepository, times(1)).getMultipleAccounts(any())

        assert(durableNonceViewModel.state.multipleAccountsResult is Resource.Success)
        assertEquals(
            (durableNonceViewModel.state.multipleAccountsResult as Resource.Success).data,
            validMultipleResponseData
        )
    }

    @Test
    fun testCannotRetrieveValidNonceData() = runBlocking {
        whenever(solanaRepository.getMultipleAccounts(any())).then {
            Resource.Success(invalidMultipleResponseData)
        }

        durableNonceViewModel.setInitialData(
            minimumNonceAccountAddressesSlot = 5,
            nonceAccountAddresses = listOf("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")
        )

        verify(
            solanaRepository,
            times(DurableNonceViewModel.NONCE_RETRIES + 1)
        ).getMultipleAccounts(any())

        assert(durableNonceViewModel.state.multipleAccountsResult is Resource.Error)
        assert(durableNonceViewModel.state.multipleAccountsResult.exception?.message == UNABLE_TO_RETRIEVE_VALID_NONCE)
    }
    //endregion

    //region Helper methods
    private suspend fun setupMultipleAccountsResultWithSuccessData() {
        whenever(solanaRepository.getMultipleAccounts(any())).then {
            Resource.Success(validMultipleResponseData)
        }

        durableNonceViewModel.setInitialData(
            minimumNonceAccountAddressesSlot = minimumSlotInfo,
            nonceAccountAddresses = listOf("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")
        )
    }
    //endregion
}