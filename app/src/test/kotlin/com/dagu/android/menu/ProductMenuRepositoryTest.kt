package com.dagu.android.menu

import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.*
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.data.menu.MenuCategory
import com.dagu.android.data.menu.ProductMenuApi
import com.dagu.android.data.menu.ProductMenuRepository
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.lang.Exception
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class ProductMenuRepositoryTest : BaseCoroutineTest() {

    @ExperimentalTime
    @Test
    fun `productMenu StateFlow should emit a sequence of loading results and a success result with a list of menu categories from API`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Mock data for the API call
            val productMenuResult = MenuCategory("ImageUrl", 0, 0, "BURGERS", "Burgers description", "")
            val productMenuList = listOf(productMenuResult)

            val successResult = Result.Success(productMenuList)
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            // Mock the API service using Mockito simulating a successful call
            val mockProductMenuApi = mock<ProductMenuApi> {
                onBlocking { getMenusForLocation(any(), any()) } doReturn productMenuList
            }

            // Instantiate the ProductMenuRepository
            val productMenuRepository =
                ProductMenuRepository(mockProductMenuApi)


            // Test the returned Flow from the getProductMenu method
            productMenuRepository.productMenu.test {

                // trigger the call to the API
                productMenuRepository.getProductMenu("")

                assertEquals(loadingResult, expectItem())
                assertEquals(notLoadingResult, expectItem())
                assertEquals(successResult, expectItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @ExperimentalTime
    @Test
    fun `productMenu StateFlow should emit a sequence of loading results and throw an exception when an error occurs on the API call`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Mock data for the API call
            val loadingResult = Result.Loading(true)

            // Mock the API service using Mockito simulating a failed call
            val mockProductMenuApi = mock<ProductMenuApi>()
            val exception = IOException()
            whenever(mockProductMenuApi.getMenusForLocation(any(), any())) doAnswer {
                throw exception
            }

            // Instantiate the ProductMenuRepository
            val productMenuRepository =
                ProductMenuRepository(mockProductMenuApi)

            // Test the returned Flow from the getLocations method
            productMenuRepository.productMenu.test {

                try {
                    productMenuRepository.getProductMenu("")
                } catch (e: Exception) {
                    assertThat(e, sameInstance(exception))
                }

                assertEquals(loadingResult, expectItem())

                cancelAndIgnoreRemainingEvents()
            }

        }
}
