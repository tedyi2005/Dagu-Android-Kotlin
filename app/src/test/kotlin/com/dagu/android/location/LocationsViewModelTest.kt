package com.dagu.android.location

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asFlow
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.data.location.*
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
    fun `locationList LiveData should emit a UI success result when receiving a success result from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Pause the test dispatcher so we don't miss any updates to locationList's LiveData
            coroutineTestRule.dispatcher.pauseDispatcher {
                // Mock data for locations list

                val locationResult = Location(
                    1000,
                    16,
                    14631,
                    "82ca6502",
                    "INACTIVE",
                    "8",
                    "Demo vendor",
                    "26 Broadway",
                    "NY",
                    "New York",
                    1004,
                    "none",
                    40.5,
                    -74.3,
                    32,
                    "America New York",
                    "fake@fake.com",
                    "5555555555",
                    "test",
                    "Mask required",
                    "Heads up",
                    Capacity(14, 43, 434, 43, 43, 443, 43),
                    23,
                    32,
                    Bsp(false, 12),
                    "12/21/2020",
                    "12/21/2020",
                    listOf(
                        Images("test", "test", "test", "test", "test"),
                    ),
                    listOf(AvailableTimeSlots("12/21/2020", 3224343)),
                    Flags(true, true, true, true, true),
                    HandoffModes(
                        DineIn(false, "fjd", "fd"), Pickup(false, "fjdsk", "fd"),
                        Curbside(false, "1233456666", "test"), WalkUp(false, "323333333", "test"),
                        DriveUp(false, "23233233323", "test"),
                        DriveThru(false, "3243242342", "test"),
                        Delivery(false, "324342342342", "test")
                    ),
                    Channels(true, true, true, true, true, true, true, true),
                    ContactTracing(false, "nope"),
                    listOf(Hours(listOf("3-4"), listOf(Ranges("day", "end", "Monday"))))
                )

                val locationList = listOf(locationResult)

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
    fun `locationList LiveData should emit a sequence of UI loading results when receiving a sequence of loading results from LocationRepository`() =
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
    fun `locationList LiveData should emit a UI error result when receiving an error result from LocationRepository`() =
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

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `region LiveData should emit a UI success result when receiving a success result from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {

                val region = Region("122", "New York", "test", 12, 12)
                val regionData = RegionResponse(listOf(region))

                val mockLocationRepository = mock<LocationRepository> {
                    val successResult = Result.Success(regionData)

                    onBlocking { getRegions() } doReturn flowOf(successResult)
                }
                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                locationsViewModel.regions.asFlow().test {
                    val expectedResult = UIResult.Success(regionData.regions)

                    assertThat(expectItem(), equalTo(expectedResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `region LiveData should emit a sequence of UI loading results when receiving a sequence of loading results from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockLocationRepository = mock<LocationRepository> {
                    val loadingResult = Result.Loading(true)
                    val notLoadingResult = Result.Loading(false)

                    onBlocking { getRegions() } doReturn flowOf(loadingResult, notLoadingResult)
                }

                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                locationsViewModel.regions.asFlow().test {
                    val loadingUiResult = UIResult.Loading(true)
                    val notLoadingUiResult = UIResult.Loading(false)

                    assertThat(expectItem(), equalTo(loadingUiResult))
                    assertThat(expectItem(), equalTo(notLoadingUiResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }

    @ExperimentalTime
    @FlowPreview
    @Test
    fun `region LiveData should emit a UI error result when receiving an error result from LocationRepository`() =
        coroutineTestRule.dispatcher.runBlockingTest {
            coroutineTestRule.dispatcher.pauseDispatcher {
                val mockLocationRepository = mock<LocationRepository> {
                    val exception = Exception("test")
                    val errorResult = Result.Error(exception)
                    onBlocking { getRegions() } doReturn flowOf(errorResult)
                }

                val locationsViewModel =
                    LocationsViewModel(mockLocationRepository, coroutineTestRule.dispatcher)

                val errorUIResult = UIResult.Error("Error fetching regions: test")

                locationsViewModel.regions.asFlow().test {
                    assertThat(expectItem(), equalTo(errorUIResult))
                    expectNoEvents()
                }
            }

            coroutineTestRule.dispatcher.resumeDispatcher()
        }
}