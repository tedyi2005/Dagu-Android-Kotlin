package com.dagu.android.util.file

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.dagu.android.BuildConfig

class FileUtils {
    companion object {
        fun downloadFile(context: Context, url: String) {
            val uri = Uri.parse(url)
            val downloadManager: DownloadManager = context.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(uri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "allergens")
                .setTitle("Downloading")
                .setDescription("Downloading allergens info")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            if (BuildConfig.FLAVOR != "prod") {
                request.addRequestHeader("Authorization", "Basic U2hha2VTaGFjazpidXJnZXJz")
            }

            downloadManager.enqueue(request)
        }

        fun downloadFile(context: Context, @StringRes urlStringResId: Int) {
            val url = context.getString(urlStringResId)
            downloadFile(context, url)
        }
    }
}
