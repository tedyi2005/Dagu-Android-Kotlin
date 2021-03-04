package com.dagu.android.data.location

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationListResponse(@SerializedName("result") val locations: List<Location>)

data class LocationSingleResponse(@SerializedName("result") val locations: Location)

data class Location(

    @SerializedName("locationId") val locationId: Int? = null,
    @SerializedName("ssmaId") val ssmaId: Int? = null,
    @SerializedName("oloId") val oloId: Int? = null,
    @SerializedName("ncrId") val ncrId: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("zeroInId") val zeroInId: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("streetAddress") val streetAddress: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("zip") val zip: Int? = null,
    @SerializedName("crossStreet") val crossStreet: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("regionId") val regionId: String? = null,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("emailAddress") val emailAddress: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("additionalMessage") val additionalMessage: String? = null,
    @SerializedName("alertMessage") val alertMessage: String? = null,
    @SerializedName("orderConfirmationMessage") val orderConfirmationMessage: String? = null,
    @SerializedName("capacity") val capacity: Capacity? = null,
    @SerializedName("timeslotLengthMinutes") val timeslotLengthMinutes: Int? = null,
    @SerializedName("bufferLengthMinutes") val bufferLengthMinutes: Int? = null,
    @SerializedName("bsp") val bsp: Bsp? = null,
    @SerializedName("updateDate") val updateDate: String? = null,
    @SerializedName("ssmaUpdateDate") val ssmaUpdateDate: String? = null,
    @SerializedName("images") val images: List<Images>? = null,
    @SerializedName("availableTimeslots") val availableTimeslots: List<AvailableTimeSlots>? = null,
    @SerializedName("flags") val flags: Flags? = null,
    @SerializedName("handoffModes") val handoffModes: HandoffModes? = null,
    @SerializedName("channels") val channels: Channels? = null,
    @SerializedName("contactTracing") val contactTracing: ContactTracing? = null,
    @SerializedName("hours") val hours: List<Hours>? = null
) : Serializable

data class AvailableTimeSlots(

    @SerializedName("startDate") val startDate: String,
    @SerializedName("julianTime") val julianTime: Int
)

data class Bsp(

    @SerializedName("fulfillmentEnabled") val fulfillmentEnabled: Boolean,
    @SerializedName("fallbackPrepTime") val fallbackPrepTime: Int
)

data class Capacity(

    @SerializedName("dinein") val dinein: Int,
    @SerializedName("pickup") val pickup: Int,
    @SerializedName("curbside") val curbside: Int,
    @SerializedName("walkup") val walkup: Int,
    @SerializedName("driveup") val driveup: Int,
    @SerializedName("drivethru") val drivethru: Int,
    @SerializedName("delivery") val delivery: Int
)

data class Channels(

    @SerializedName("isIOSAvailable") val isIOSAvailable: Boolean,
    @SerializedName("isAndroidAvailable") val isAndroidAvailable: Boolean,
    @SerializedName("isWebAvailable") val isWebAvailable: Boolean,
    @SerializedName("isKioskAvailable") val isKioskAvailable: Boolean,
    @SerializedName("isUberAvailable") val isUberAvailable: Boolean,
    @SerializedName("isGrubhubAvailable") val isGrubhubAvailable: Boolean,
    @SerializedName("isDoorDashAvailable") val isDoorDashAvailable: Boolean,
    @SerializedName("isPostmatesAvailable") val isPostmatesAvailable: Boolean
)

data class ContactTracing(

    @SerializedName("isContactTracingAvailable") val isContactTracingAvailable: Boolean,
    @SerializedName("contactTracingMessage") val contactTracingMessage: String
)

data class Curbside(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class Delivery(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class DineIn(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class DriveThru(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class DriveUp(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class Flags(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("isOnlineOrderingAvailable") val isOnlineOrderingAvailable: Boolean,
    @SerializedName("isAsapOrderingAvailable") val isAsapOrderingAvailable: Boolean,
    @SerializedName("isOrderAheadAvailable") val isOrderAheadAvailable: Boolean,
    @SerializedName("isDemo") val isDemo: Boolean
)

data class HandoffModes(

    @SerializedName("dinein") val dineIn: DineIn,
    @SerializedName("pickup") val pickup: Pickup,
    @SerializedName("curbside") val curbside: Curbside,
    @SerializedName("walkup") val walkUp: WalkUp,
    @SerializedName("driveup") val driveUp: DriveUp,
    @SerializedName("drivethru") val driveThru: DriveThru,
    @SerializedName("delivery") val delivery: Delivery
)

data class Hours(

    @SerializedName("handoffModes") val handoffModes: List<String>,
    @SerializedName("ranges") val ranges: List<Ranges>
)

data class Images(

    @SerializedName("image") val image: String,
    @SerializedName("image_sm") val image_sm: String,
    @SerializedName("image_md") val image_md: String,
    @SerializedName("image_lg") val image_lg: String,
    @SerializedName("image_xlg") val image_xlg: String
)

data class Pickup(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)

data class Ranges(

    @SerializedName("end") val end: String,
    @SerializedName("start") val start: String,
    @SerializedName("weekday") val weekday: String
)

data class WalkUp(

    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("customMessage") val customMessage: String
)