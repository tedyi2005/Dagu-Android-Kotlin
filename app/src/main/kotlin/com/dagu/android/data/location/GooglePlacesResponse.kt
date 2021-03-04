package com.dagu.android.data.location

import com.google.gson.annotations.SerializedName

data class GooglePlacesResponse(@SerializedName("result") val result: List<GooglePlacesContainer>)

data class GooglePlacesContainer(

    @SerializedName("places_provider") val places_provider: String,
    @SerializedName("result") val predictionsContainer: GooglePredictionsContainer
)

data class GooglePredictionsContainer(

    @SerializedName("predictions") val predictions: List<Prediction>
)

data class MainTextMatchedSubstrings(

    @SerializedName("length") val length: Int,
    @SerializedName("offset") val offset: Int
)

data class MatchedSubstrings(

    @SerializedName("length") val length: Int,
    @SerializedName("offset") val offset: Int
)

data class Prediction(

    @SerializedName("description") val description: String,
    @SerializedName("matched_substrings") val matchedSubstrings: List<MatchedSubstrings>,
    @SerializedName("place_id") val place_id: String,
    @SerializedName("reference") val reference: String,
    @SerializedName("structured_formatting") val structuredFormatting: List<StructuredFormatting>,
    @SerializedName("terms") val terms: List<Terms>,
    @SerializedName("types") val types: List<String>
)

data class SecondaryTextMatchedSubstring(

    @SerializedName("length") val length: Int,
    @SerializedName("offset") val offset: Int
)

data class StructuredFormatting(

    @SerializedName("main_text") val mainText: String,
    @SerializedName("main_text_matched_substrings") val mainTextMatchedSubstrings: List<MainTextMatchedSubstrings>,
    @SerializedName("secondary_text") val secondaryText: String,
    @SerializedName("secondary_text_matched_substrings") val secondaryTextMatchedSubstrings: List<SecondaryTextMatchedSubstring>
)

data class Terms(

    @SerializedName("offset") val offset: Int,
    @SerializedName("value") val value: String
)