package com.dagu.android.data.location

import com.dagu.android.BuildConfig
import com.dagu.android.data.di.module.IoDispatcher
import com.dagu.android.data.repository.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Credentials
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val locationApi: LocationApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    // In-memory cache for locations (gets refreshed after app restarts)
    private val locations: MutableList<Location> = mutableListOf()

    // In-memory cache for locations near the last placeId we looked for
    private var lastPlaceId: String? = null
    private val nearbyLocations: MutableList<Location> = mutableListOf()

    private val basicAuthHeader: String by lazy {
        Credentials.basic(BuildConfig.SSMA_CLIENT_ID, BuildConfig.SSMA_CLIENT_SECRET)
    }

    suspend fun getLocations(lat: Double?, lng: Double?): Flow<Result<List<Location>>> {
        if (locations.isEmpty()) {
            return flow {
                emit(Result.Loading(true))

                val result = try {
                    val response = locationApi.getLocations(
                        basicAuthHeader,
                        lat = lat,
                        lng = lng
                    )
                    locations.addAll(response.locations)
                    Result.Success(locations)
                } catch (e: Exception) {
                    Result.Error(e)
                }

                emit(Result.Loading(false))
                emit(result)
            }.flowOn(dispatcher)
        } else {
            return flow {
                emit(Result.Success(locations))
            }.flowOn(dispatcher)
        }
    }

    suspend fun getLocations(placeId: String): Flow<Result<List<Location>>> {
        if (placeId != lastPlaceId) {
            // Hit API if there's no cached nearby locations for the provided place Id:
            lastPlaceId = placeId
            return flow {
                emit(Result.Loading(true))

                val result = try {
                    val response = locationApi.getLocations(
                        basicAuthHeader,
                        placeId = placeId,
                        channel = "ANDROID"
                    )
                    nearbyLocations.addAll(response.locations)
                    Result.Success(nearbyLocations)
                } catch (e: Exception) {
                    Result.Error(e)
                }

                emit(Result.Loading(false))
                emit(result)
            }.flowOn(dispatcher)
        } else {
            // Return cached results for the provided placeId if available:
            return flow {
                emit(Result.Success(nearbyLocations))
            }.flowOn(dispatcher)
        }
    }

    suspend fun getSingleLocation(locationID: Int, channels: String): Flow<Result<Location>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response = locationApi.getSingleLocation(basicAuthHeader, locationID, channels)
                val locations = response.locations
                Result.Success(locations)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun getRegions(): Flow<Result<RegionResponse>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val response = locationApi.getRegions(basicAuthHeader)
                Result.Success(response)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))
            emit(result)
        }.flowOn(dispatcher)
    }

    suspend fun searchLocation(input: String): Flow<Result<GooglePlacesResponse>> {
        return flow {
            emit(Result.Loading(true))
            val result = try {
                val googleResponse = locationApi.searchLocation(basicAuthHeader, input)
                Result.Success(googleResponse)
            } catch (e: Exception) {
                Result.Error(e)
            }
            emit(Result.Loading(false))
            emit(result)
        }.flowOn(dispatcher)
    }
}
