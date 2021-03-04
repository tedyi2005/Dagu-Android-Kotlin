package com.dagu.android.application

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onesignal.OneSignal
import com.dagu.android.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShakeShackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Only collect for Crashlytics when building the "prodRelease" variant:
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.RELEASE)

        // OneSignal initialization...
        if (!BuildConfig.RELEASE) {
            OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        }
        OneSignal.startInit(this)
            .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
            .unsubscribeWhenNotificationsAreDisabled(true)
            .init()

        // TODO: update/delete this depending on the client's decision to use night mode
        // This is for setting the night mode depending on the Android Version, if it's equal or
        // greater than Android Q it would use the system defined theme, else will change to night
        // mode when battery saver mode is on
        val nightMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
