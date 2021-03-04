package com.dagu.android.util.intent

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes

class IntentUtils {
    companion object {
        fun openUrl(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            context.startActivity(intent)
        }

        fun openUrl(context: Context, @StringRes urlStringResId: Int) {
            val url = context.getString(urlStringResId)
            openUrl(context, url)
        }
    }
}