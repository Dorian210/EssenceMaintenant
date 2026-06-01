package com.dorian_bichet.essence_maintenant.utils

import androidx.compose.runtime.Composable

interface LocationPermissionState {
    val isGranted: Boolean
    fun launchPermissionRequest()
}

@Composable
expect fun rememberLocationPermissionState(): LocationPermissionState
