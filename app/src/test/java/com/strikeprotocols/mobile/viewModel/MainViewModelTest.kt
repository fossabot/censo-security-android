package com.strikeprotocols.mobile.viewModel

import com.nhaarman.mockitokotlin2.whenever
import com.strikeprotocols.mobile.BuildConfig
import com.strikeprotocols.mobile.common.Resource
import com.strikeprotocols.mobile.data.KeyRepository
import com.strikeprotocols.mobile.data.UserRepository
import com.strikeprotocols.mobile.data.models.OsVersion
import com.strikeprotocols.mobile.data.models.SemanticVersionResponse
import com.strikeprotocols.mobile.presentation.semantic_version_check.MainViewModel
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest : BaseViewModelTest() {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var keyRepository: KeyRepository

    private lateinit var semVerViewModel: MainViewModel

    private val dispatcher = StandardTestDispatcher()

    //region Test data
    private lateinit var currentVersionName: String

    private val higherMajorVersion = BuildConfig.versionNameMajor + 1
    private val higherMinorVersion = BuildConfig.versionNameMinor + 1
    private val higherPatchVersion = BuildConfig.versionNamePatch + 1

    private val currentMajorVersion = BuildConfig.versionNameMajor
    private val currentMinorVersion = BuildConfig.versionNameMinor
    private val currentPatchVersion = BuildConfig.versionNamePatch

    private val lowerMajorVersion = BuildConfig.versionNameMajor - 1
    private val lowerMinorVersion = BuildConfig.versionNameMinor - 1
    private val lowerPatchVersion = BuildConfig.versionNamePatch - 1

    private val testHighMinimumVersion = "100.0.0"
    private val testLowMinimumVersion = "0.0.0"
    //endregion

    //region Before After Work
    @Before
    override fun setUp() {
        super.setUp()
        Dispatchers.setMain(dispatcher)

        semVerViewModel = MainViewModel(
            userRepository = userRepository,
            keyRepository = keyRepository
        )

        currentVersionName = BuildConfig.VERSION_NAME
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    //endregion

    /**
     * Test that when we check the minimum app version and it is greater than the current app version,
     * the view model should enforce the app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success] and true
     */
    @Test
    fun `check minimum version is greater than current version then view model should enforce update`() = runTest {
        setMinimumVersionResponse(testHighMinimumVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        //Should be true since we have to enforce the update
        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate.data == true)
    }

    /**
     * Test that when we check the minimum app version and it is lower than the current app version,
     * the view model should do nothing and not enforce an app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is still [Resource.Uninitialized]
     */
    @Test
    fun `check minimum version is lower than current version then view model should not enforce update`() = runTest {
        setMinimumVersionResponse(testLowMinimumVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        //Should still be uninitialized since we do not have to enforce app update
        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    /**
     * Test that when we check the minimum version and it is the same as the current version,
     * the view model should do nothing and not enforce an app update
     *
     * Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is still [Resource.Uninitialized]
     */
    @Test
    fun `check minimum version is the same as current version then view model should not enforce update`() = runTest {
        setMinimumVersionResponse(currentVersionName)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    /**
     *  Test that when we check the minimum version and it has a higher major version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher major number than current version then view model should enforce app update`() = runTest {
        val testHigherMajorVersion = "$higherMajorVersion.$currentMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherMajorVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate.data == true)
    }

    /**
     *  Test that when we check the minimum version and it has a higher minor version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher minor number than current version then view model should enforce app update`() = runTest {
        val testHigherMinorVersion = "$currentMajorVersion.$higherMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherMinorVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate.data == true)
    }

    /**
     *  Test that when we check the minimum version and it has a higher patch version then the current version,
     *  the view model should enforce an app update
     *
     *  Assertions:
     * - assert that the shouldEnforceAppUpdate state property is [Resource.Uninitialized]
     * - after checking minimum version, assert that shouldEnforceAppUpdate state property is [Resource.Success]
     */
    @Test
    fun `check minimum version has higher patch number than current version then view model should enforce app update`() = runTest {
        val testHigherPatchVersion = "$currentMajorVersion.$currentMinorVersion.$higherPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testHigherPatchVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate.data == true)
    }

    @Test
    fun `check if minimum version has lower major number than current major number that view model does not enforce app update`() = runTest {
        val testLowerMajorVersion = "$lowerMajorVersion.$currentMinorVersion.$currentPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testLowerMajorVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    @Test
    fun `check if minimum version has lower minor number than current minimum number that view model does not enforce app update`() = runTest {
        val testLowerMinorVersion = "$currentMajorVersion.$lowerMinorVersion.$currentPatchVersion"

        setMinimumVersionResponse(minimumVersionResponse = testLowerMinorVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    @Test
    fun `check if minimum version has lower patch number than current patch number that view model does not enforce app update`() = runTest {
        val testLowerPatchVersion = "$currentMajorVersion.$currentMinorVersion.$lowerPatchVersion"
        setMinimumVersionResponse(minimumVersionResponse = testLowerPatchVersion)

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)

        semVerViewModel.checkMinimumVersion()

        advanceUntilIdle()

        TestCase.assertTrue(semVerViewModel.state.shouldEnforceAppUpdate is Resource.Uninitialized)
    }

    //Helper methods
    private fun setMinimumVersionResponse(minimumVersionResponse: String) = runTest {
        whenever(userRepository.checkMinimumVersion()).thenAnswer {
            Resource.Success(
                data = SemanticVersionResponse(
                    androidVersion = OsVersion(
                        minimumVersion = minimumVersionResponse
                    )
                )
            )
        }
    }

}