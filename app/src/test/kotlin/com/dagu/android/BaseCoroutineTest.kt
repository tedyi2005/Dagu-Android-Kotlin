package com.dagu.android

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule

@ExperimentalCoroutinesApi
open class BaseCoroutineTest {
    // Get the CoroutineTestRule to change from Main dispatcher to test dispatcher
    @get:Rule
    val coroutineTestRule = CoroutineTestRule()
}