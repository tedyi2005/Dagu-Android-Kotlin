package com.dagu.android.data.authentication.model

import com.google.gson.annotations.SerializedName
import com.dagu.android.data.location.Location


data class UserData(
    @SerializedName("dob")
    val dob: Any?, // null
    @SerializedName("encrypted_magical_immutable")
    val encryptedMagicalImmutable: String?, // o+V/r0VwG0/PJnlDEn+4+A==
    @SerializedName("fav_item_chain_id")
    val favItemChainId: Any?, // null
    @SerializedName("gender")
    val gender: Any?, // null
    @SerializedName("grant_flow_type")
    val grantFlowType: String?, // password
    @SerializedName("id")
    val id: Int?, // 11388
    @SerializedName("is_area_manager")
    val isAreaManager: Boolean?, // false
    @SerializedName("is_staff")
    val isStaff: Boolean?, // true
    @SerializedName("is_store_manager")
    val isStoreManager: Boolean?, // false
    @SerializedName("is_superuser")
    val isSuperuser: Boolean?, // true
    @SerializedName("is_team_member")
    val isTeamMember: Boolean?, // false
    @SerializedName("kids")
    val kids: Any?, // null
    @SerializedName("kiosk")
    val kiosk: Any?, // null
    @SerializedName("locations")
    val locations: List<Location?>?,
    @SerializedName("loyalty_providers")
    val loyaltyProviders: List<LoyaltyProvider?>?,
    @SerializedName("magical_immutable_iv")
    val magicalImmutableIv: String?, // NTAwVUhid3NudFM2aXlJSw==
    @SerializedName("needs_to_enter_new_password")
    val needsToEnterNewPassword: Boolean?, // false
    @SerializedName("olo_provider_token")
    val oloProviderToken: String?, // hxoEoWsUlaOcnaEFMvQbgROQgo66cj
    @SerializedName("optout_email")
    val optoutEmail: Boolean?, // true
    @SerializedName("optout_messages")
    val optoutMessages: Boolean?, // true
    @SerializedName("pets")
    val pets: Any?, // null
    @SerializedName("referral_code")
    val referralCode: Any?, // null
    @SerializedName("type")
    val type: String?, // cms-user
    @SerializedName("user")
    val user: User?,
    @SerializedName("userprofile_allergens")
    val userprofileAllergens: List<ApiAllergen?>?,
    @SerializedName("zip")
    val zip: String? // null
) {
    data class Image(
        @SerializedName("image")
        val image: String?, // https://ssapp-stage.s3.amazonaws.com/locations/708/221/carousel_21.jpg
        @SerializedName("image_lg")
        val imageLg: String?, // https://ssapp-stage.s3.amazonaws.com/carousel_21_lg1523456942.jpeg
        @SerializedName("image_md")
        val imageMd: String?, // https://ssapp-stage.s3.amazonaws.com/carousel_21_md1523456942.jpeg
        @SerializedName("image_sm")
        val imageSm: String?, // https://ssapp-stage.s3.amazonaws.com/carousel_21_sm1523456942.jpeg
        @SerializedName("image_xlg")
        val imageXlg: String? // https://ssapp-stage.s3.amazonaws.com/carousel_21_xlg1523456942.jpeg
    )


    data class LoyaltyProvider(
        @SerializedName("name")
        val name: String?, // Shake Shack SessionM
        @SerializedName("provider")
        val provider: String?, // SessionM
        @SerializedName("scheme_id")
        val schemeId: Int? // 72
    )

    data class User(
        @SerializedName("email")
        val email: String?, // casey.whalen@bounteous.com
        @SerializedName("first_name")
        val firstName: String?, // null
        @SerializedName("last_name")
        val lastName: String?, // null
        @SerializedName("username")
        val username: String? // casey.whalen@bounteous.com
    )

    data class ApiAllergen(
        @SerializedName("id")
        val id: Int?,
        @SerializedName("name")
        val name: String?
    )

}