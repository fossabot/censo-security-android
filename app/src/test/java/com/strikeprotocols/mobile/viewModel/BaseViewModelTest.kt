package com.strikeprotocols.mobile.viewModel

import org.junit.Before
import org.mockito.MockitoAnnotations

open class BaseViewModelTest {

    @Before
    open fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

}