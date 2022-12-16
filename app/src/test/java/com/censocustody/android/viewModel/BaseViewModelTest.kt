package com.censocustody.android.viewModel

import org.junit.Before
import org.mockito.MockitoAnnotations

open class BaseViewModelTest {

    @Before
    open fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

}