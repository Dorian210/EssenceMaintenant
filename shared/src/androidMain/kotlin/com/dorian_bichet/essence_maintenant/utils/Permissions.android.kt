package com.dorian_bichet.essence_maintenant.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class AndroidLocationPermissionState(
    private val context: Context,
    private val requestLauncher: (Array<String>) -> Unit
) : LocationPermissionState {
    
    override var isGranted by mutableStateOf(checkPermission())
        private set

    private fun checkPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    override fun launchPermissionRequest() {
        requestLauncher(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun updatePermission(granted: Boolean) {
        isGranted = granted || checkPermission()
    }
}

@Composable
actual fun rememberLocationPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    val state = remember(context) { AndroidLocationPermissionState(context) { } }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        (state as AndroidLocationPermissionState).updatePermission(granted)
    }

    // Update the launcher in the state
    SideEffect {
        val launcherField = state::class.java.getDeclaredField("requestLauncher")
        launcherField.isAccessible = true
        // This is a bit hacky because of the constructor, but works for the demo.
        // Better: use a mutable state for the launcher lambda.
    }
    
    // Let's rewrite it cleaner
    return rememberAndroidPermissionState()
}

@Composable
private fun rememberAndroidPermissionState(): LocationPermissionState {
    val context = LocalContext.current
    var isGranted by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isGranted = permissions.values.any { it }
    }
    
    return remember(isGranted) {
        object : LocationPermissionState {
            override val isGranted: Boolean = isGranted
            override fun launchPermissionRequest() {
                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}
