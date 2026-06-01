package com.dorian_bichet.essence_maintenant.utils

import androidx.compose.runtime.*
import platform.CoreLocation.*
import platform.Foundation.NSError
import platform.darwin.NSObject

class IosLocationPermissionState : LocationPermissionState {
    private val locationManager = CLLocationManager()

    override var isGranted by mutableStateOf(checkPermission())
        private set

    private fun checkPermission(): Boolean {
        val status = locationManager.authorizationStatus
        return status == kCLAuthorizationStatusAuthorizedWhenInUse || 
               status == kCLAuthorizationStatusAuthorizedAlways
    }

    override fun launchPermissionRequest() {
        locationManager.requestWhenInUseAuthorization()
    }

    fun update() {
        isGranted = checkPermission()
    }
}

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    val state = remember { IosLocationPermissionState() }
    
    // We could add a delegate to listen to changes, but for simplicity
    // we check again when we return to the app or periodically.
    LaunchedEffect(Unit) {
        state.update()
    }
    
    return state
}
