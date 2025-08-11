package com.victorhugo.boleiragem.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * Gerenciador de permissões de localização para uso com Compose
 */
class LocationPermissionManager(private val context: Context) {

    /**
     * Verifica se as permissões de localização estão concedidas
     */
    fun hasLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}

/**
 * Composable para solicitar permissões de localização
 */
@Composable
fun RequestLocationPermissions(
    onPermissionsResult: (Boolean) -> Unit
) {
    var permissionsRequested by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        onPermissionsResult(allGranted)
    }

    LaunchedEffect(key1 = permissionsRequested) {
        if (!permissionsRequested) {
            permissionLauncher.launch(LocationPermissionManager.LOCATION_PERMISSIONS)
            permissionsRequested = true
        }
    }
}
