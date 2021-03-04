package com.dagu.android.util

internal enum class PermissionStatus {
    CAN_ASK, // We either haven't asked for this permission or permission was revoked
    GRANTED, // No need to ask for this permission again
    DENIED_CAN_ASK, // It was denied once the last time we asked, show rationale
    DENIED_CANNOT_ASK // User denied with "Don't ask again" option. Only way to ask again is by opening App Settings in OS
}
