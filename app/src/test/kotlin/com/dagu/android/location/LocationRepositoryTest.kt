package com.dagu.android.location

import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.*
import com.dagu.android.BaseCoroutineTest
import com.dagu.android.data.location.*
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.sameInstance
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class LocationRepositoryTest : BaseCoroutineTest() {

    @ExperimentalTime
    @Test
    fun `getLocations should emit a sequence of loading results and a success result with a list of locations from API if locations are not cached`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Mock data for the API call
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
            val locationList = LocationListResponse(listOf(locationResult))

            val successResult = Result.Success(locationList)
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            // Mock the API service using Mockito simulating a successful call
            val mockLocationApi = mock<LocationApi> {
                onBlocking { getLocations(any()) } doReturn locationList
            }

            // Instantiate the LocationRepository passing the test dispatcher and mock apiService as parameters
            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            // Test the returned Flow from the getLocations method
            val flow: Flow<Result<List<Location>>> = locationRepository.getLocations()
            flow.test {
                assertEquals(expectItem(), loadingResult)
                assertEquals(expectItem(), notLoadingResult)
                assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }

    @ExperimentalTime
    @Test
    fun `getLocations should emit a sequence of loading results and an error result when an exception occurs on the API call`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            // Mock data for the API call
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            // Mock the API service using Mockito simulating a failed call
            val mockLocationApi = mock<LocationApi>()
            val exception = IOException()
            whenever(mockLocationApi.getLocations(any())) doAnswer {
                throw exception
            }

            // Instantiate the LocationRepository passing the test dispatcher and mock apiService as parameters
            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            // Test the returned Flow from the getLocations method
            val flow: Flow<Result<List<Location>>> = locationRepository.getLocations()
            flow.test {
                assertThat(expectItem(), equalTo(loadingResult))
                assertThat(expectItem(), equalTo(notLoadingResult))
                val resultError = expectItem() as Result.Error
                assertThat(resultError.exception, sameInstance(exception))
                expectComplete()
            }

        }

    @ExperimentalTime
    @Test
    fun `getRegions should emit a sequence of loading results and a success result with a list of regions from API`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            val region = Region("122", "New York", "test", 12, 12)
            val regionData = RegionResponse(listOf(region))

            val successResult = Result.Success(regionData)
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            val mockLocationApi = mock<LocationApi> {
                onBlocking { getRegions(any()) } doReturn regionData
            }

            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            val flow: Flow<Result<RegionResponse>> = locationRepository.getRegions()
            flow.test {
                assertEquals(expectItem(), loadingResult)
                assertEquals(expectItem(), notLoadingResult)
                assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }

    @ExperimentalTime
    @Test
    fun `getRegions should emit a sequence of loading results and an error result when an exception occurs on the API call`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            val mockLocationApi = mock<LocationApi>()
            val exception = IOException()
            whenever(mockLocationApi.getRegions(any())) doAnswer {
                throw exception
            }

            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            val flow: Flow<Result<RegionResponse>> = locationRepository.getRegions()
            flow.test {
                assertThat(expectItem(), equalTo(loadingResult))
                assertThat(expectItem(), equalTo(notLoadingResult))
                val resultError = expectItem() as Result.Error
                assertThat(resultError.exception, sameInstance(exception))
                expectComplete()
            }

        }

    @ExperimentalTime
    @Test
    fun `getSingleLocation should emit a sequence of loading results and a success result with one location from API`() =
        coroutineTestRule.dispatcher.runBlockingTest {

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

            val successResult = Result.Success(locationResult)
            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            val mockLocationApi = mock<LocationApi> {
                onBlocking {
                    getSingleLocation(
                        any(),
                        any(),
                        any()
                    )
                } doReturn LocationSingleResponse(locationResult)
            }

            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            val flow: Flow<Result<Location>> = locationRepository.getSingleLocation(1, "")
            flow.test {
                assertEquals(expectItem(), loadingResult)
                assertEquals(expectItem(), notLoadingResult)
                assertEquals(expectItem(), successResult)
                expectComplete()
            }
        }

    @ExperimentalTime
    @Test
    fun `getSingleLocation should emit a sequence of loading results and an error result when an exception occurs on the API call`() =
        coroutineTestRule.dispatcher.runBlockingTest {

            val loadingResult = Result.Loading(true)
            val notLoadingResult = Result.Loading(false)

            val mockLocationApi = mock<LocationApi>()
            val exception = IOException()
            whenever(mockLocationApi.getSingleLocation(any(), any(), any())) doAnswer {
                throw exception
            }

            val locationRepository =
                LocationRepository(mockLocationApi, coroutineTestRule.dispatcher)

            val flow: Flow<Result<Location>> = locationRepository.getSingleLocation(1, "")
            flow.test {
                assertThat(expectItem(), equalTo(loadingResult))
                assertThat(expectItem(), equalTo(notLoadingResult))
                val resultError = expectItem() as Result.Error
                assertThat(resultError.exception, sameInstance(exception))
                expectComplete()
            }

        }
}
