package com.dorian_bichet.essence_maintenant.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openMapRouting(latitude: Double, longitude: Double, label: String) {
    // iOS utilise nativement Apple Maps via l'Uri "maps://"
    val urlString = "http://maps.apple.com/?daddr=$latitude,$longitude&ll=$latitude,$longitude"
    val nsUrl = NSURL.URLWithString(urlString)
    if (nsUrl != null && UIApplication.sharedApplication.canOpenURL(nsUrl)) {
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}