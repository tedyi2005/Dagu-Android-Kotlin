package com.dagu.android.data.user

import com.dagu.android.data.pdp.SingleViewItem

data class UserProfile(
    var id: Int?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val birthday: String?,
    val email: String?,
    val receiveSmsFromShakeShack: Boolean?,
    val receiveMarketingEmails: Boolean?,
    val genderSelection: SingleViewItem?,
    val kidsSelection: SingleViewItem?,
    val petsSelection: List<SingleViewItem>?,
) {
    fun getShortSummaryForAccountOverview(): String {
        var summary = "$firstName $lastName"
        phoneNumber?.let {
            summary += ", $phoneNumber"
        }
        return summary
    }

}