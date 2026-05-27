package com.dorian_bichet.essence_maintenant.utils

import android.app.Application
import android.content.Intent
import android.net.Uri

actual fun openMapRouting(latitude: Double, longitude: Double, label: String) {
    runCatching {
        val activityThreadClass = Class.forName("android.app.ActivityThread")
        val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        val getApplicationMethod = activityThreadClass.getMethod("getApplication")
        val context = getApplicationMethod.invoke(currentActivityThread) as Application

        val uriString = "geo:$latitude,$longitude?q=$latitude,$longitude($label)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }.onFailure { error ->
        println("MapLauncher Android Error: ${error.message}")
        error.printStackTrace()
    }
}