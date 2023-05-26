package com.censocustody.android.viewModel

import com.censocustody.android.common.Resource
import com.censocustody.android.common.wrapper.CensoCountDownTimer
import com.censocustody.android.data.models.WalletConnectPairingResponse
import com.censocustody.android.data.repository.ApprovalsRepository
import com.censocustody.android.presentation.scan_qr.ScanQRViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock

@OptIn(ExperimentalCoroutinesApi::class)
class ScanQRCodeViewModelTest : BaseViewModelTest() {

    private lateinit var scanQRViewModel: ScanQRViewModel

    @Mock
    lateinit var approvalsRepository: ApprovalsRepository

    @Mock
    lateinit var countdownTimer: CensoCountDownTimer

    private val dispatcher = TestCoroutineDispatcher()

    private val validURI = "wc:8eeb2d71daf1a3a57933a64d8f3ed3412dfdde6fcc5ff56a6372ece5b82f6b97@2?relay-protocol=irn&symKey=90d8cb943c73bb86771784cf4950d9c9c50645013806b1972e9d6fc40aa19a47"
    private val topic = "8eeb2d71daf1a3a57933a64d8f3ed3412dfdde6fcc5ff56a6372ece5b82f6b97"
    private val nullURI = null
    private val emptyURI = ""

    @Before
    override fun setUp() = runTest {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        scanQRViewModel =
            ScanQRViewModel(
                approvalsRepository = approvalsRepository,
                timer = countdownTimer
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun mockSuccessfulResponse() {
        whenever(approvalsRepository.sendWcUri(any(), any())).then {
            Resource.Success(WalletConnectPairingResponse(topic))
        }
    }

    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `after retrieving valid value from scanning QR, should make call to upload uri`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
        }

    @Test
    fun `after retrieving null value from scanning QR, should not make call to upload uri`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(nullURI)

            verify(approvalsRepository, times(0)).sendWcUri(any(), any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Loading)
        }

    @Test
    fun `after retrieving empty value from scanning QR, should not make call to upload uri`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(emptyURI)

            verify(approvalsRepository, times(0)).sendWcUri(any(), any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Loading)
        }

    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `Sending URI to backend shows user success state`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Success)
            assert(scanQRViewModel.state.topic == topic)
        }

    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `Save topic from URI on Send URI Success to backend`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Success)
            assert(scanQRViewModel.state.topic == topic)
        }

    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `Failing to send URI to backend shows user failure state`() =
        runTest {

            whenever(approvalsRepository.sendWcUri(any(), any())).then {
                Resource.Error<ResponseBody>()
            }

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Error)
        }


    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `User can retry scanning after failing to send URI to backend`() =
        runTest {

            whenever(approvalsRepository.sendWcUri(any(), any())).then {
                Resource.Error<ResponseBody>()
            }

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Error)

            mockSuccessfulResponse()

            scanQRViewModel.retryScan()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(2)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Success)
        }

    @Test
    fun `User failing QR scan shows error state for QR code and does not attempt call to backend`() =
        runTest {

            val exception = Exception("Failed to scan.")

            mockSuccessfulResponse()

            scanQRViewModel.failedToScan(exception)

            verify(approvalsRepository, times(0)).sendWcUri(any(), any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Error)
            assert(scanQRViewModel.state.scanQRCodeResult.exception == exception)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Uninitialized)
        }

    @Test
    fun `User failing to get session does not show anything to user`() =
        runTest {

            val exception = Exception("Failed to scan.")

            mockSuccessfulResponse()

            scanQRViewModel.failedToScan(exception)

            verify(approvalsRepository, times(0)).sendWcUri(any(), any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Error)
            assert(scanQRViewModel.state.scanQRCodeResult.exception == exception)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Uninitialized)
        }


    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `User failing QR scan can retry scan`() =
        runTest {

            val exception = Exception("Failed to scan.")

            mockSuccessfulResponse()

            scanQRViewModel.failedToScan(exception)

            verify(approvalsRepository, times(0)).sendWcUri(any(), any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Error)
            assert(scanQRViewModel.state.scanQRCodeResult.exception == exception)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Uninitialized)

            scanQRViewModel.retryScan()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
        }

    @Ignore("Tests need update after adding wallet selection")
    @Test
    fun `After successfully completing flow, user can leave screen`() =
        runTest {

            mockSuccessfulResponse()

            scanQRViewModel.receivedWalletConnectUri(validURI)

            verify(approvalsRepository, times(1)).sendWcUri(validURI, any())

            assert(scanQRViewModel.state.scanQRCodeResult is Resource.Success)
            assert(scanQRViewModel.state.uploadWcUri is Resource.Success)

            scanQRViewModel.userFinished()

            assert(scanQRViewModel.state.exitScreen)
        }


}