package com.dagu.android

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.dagu.android.data.location.Location
import com.dagu.android.data.location.LocationRepository
import com.dagu.android.data.repository.Result
import com.dagu.android.data.repository.UIResult
import com.dagu.android.presentation.location.LocationsViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class LocationsViewModelTest : BaseCoroutineTest() {
    // Get the InstantTaskExecutorRule to test Architecture Components (such as the ViewModel)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `getAllLocations() should emit an UI success result when receiving a success result from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Pause the test dispatcher so we don't miss any updates to locationList's LiveData
            coroutineTestRule.dispatcher.pauseDispatcher {
                // Mock data for locations list
                val location = Location(
                    locationId = 2,
                    name = "Battery Park City",
                    streetAddress = "215 Murray Street",
                    latitude = 40.7152344,
                    longitude = -74.0149039
                )
                val locationList = listOf(location)

                val mockLocationRepository = mock<LocationRepository> {
                    val successResult = Result.Success(locationList)

                    onBlocking { getLocations() } doReturn flowOf(successResult)
                }

                // Instantiate the LocationsViewModel passing the test dispatcher as a parameter
                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                locationsViewModel.locationList.asFlow().test {
                    val expectedResult = UIResult.Success(locationList)

                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }
            }

            // Resume the test dispatcher to listen to LiveData changes
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `getAllLocations() should emit a sequence of UI loading results when receiving a sequence of loading results from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Pause the test dispatcher so we don't miss any updates to locationList's LiveData
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockLocationRepository = mock<LocationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)

                    onBlocking { getLocations() } doReturn flowOf(loadingResult, notLoadingResult)
                }

                // Instantiate the LocationsViewModel passing the test dispatcher as a parameter
                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                locationsViewModel.locationList.asFlow().test {
                    val loadingUiResult = UIResult.Loading(true)
                    val notLoadingUiResult = UIResult.Loading(false)

                    assertThat(expectItem(), equalTo(loadingUiResult))
                    assertThat(expectItem(), equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            // Resume the test dispatcher to listen to LiveData changes
            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `getAllLocations() should emit an UI error result when receiving an error result from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            // Pause the test dispatcher so we don't miss any updates to locationList's LiveData
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockLocationRepository = mock<LocationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { getLocations() } doReturn flowOf(errorResult)
                }

                // Instantiate the LocationsViewModel passing the test dispatcher as a parameter
                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                val errorUIResult = UIResult.Error("Error fetching locations: test")

                // Test the ViewModel's LiveData as Flow
                locationsViewModel.locationList.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }

            // Resume the test dispatcher to listen to LiveData changes
            coroutineTestRule.dispatcher.resumeDispatcher()
        }
}