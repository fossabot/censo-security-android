package com.strikeprotocols.mobile

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.common.StrikeError
import com.strikeprotocols.mobile.data.SolanaRepository
import com.strikeprotocols.mobile.data.models.MultipleAccountsResponse
import com.strikeprotocols.mobile.data.models.Nonce
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel
import com.strikeprotocols.mobile.presentation.durable_nonce.DurableNonceViewModel.Companion.UNABLE_TO_RETRIEVE_VALID_NONCE
import junit.framework.Assert.assertEquals
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
class DurableNonceViewModelTest {

    lateinit var durableNonceViewModel: DurableNonceViewModel

    @Mock
    lateinit var solanaRepository: SolanaRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    private val dispatcher = TestCoroutineDispatcher()

    private val minimumSlotInfo = 5

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

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        MockitoAnnotations.openMocks(this)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testRetrievesValidNonceData() = runBlocking {
        whenever(solanaRepository.getMultipleAccounts(any())).then {
            Resource.Success(validMultipleResponseData)
        }

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)

        durableNonceViewModel.setInitialData(
            minimumNonceAccountAddressesSlot = minimumSlotInfo,
            nonceAccountAddresses = listOf("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")
        )

        durableNonceViewModel.setUserBiometricVerified(true)

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

        durableNonceViewModel = DurableNonceViewModel(solanaRepository)

        durableNonceViewModel.setInitialData(
            minimumNonceAccountAddressesSlot = 5,
            nonceAccountAddresses = listOf("7Zpss7rbtz6qU71ywcjcuANnVyQWJrqsZ3oekkR9Hknn")
        )

        durableNonceViewModel.setUserBiometricVerified(true)

        verify(
            solanaRepository,
            times(DurableNonceViewModel.NONCE_RETRIES + 1)
        ).getMultipleAccounts(any())

        assert(durableNonceViewModel.state.multipleAccountsResult is Resource.Error)
        assert(durableNonceViewModel.state.multipleAccountsResult.exception?.message == UNABLE_TO_RETRIEVE_VALID_NONCE)
    }
}