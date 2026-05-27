package com.dorian_bichet.essence_maintenant

import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.repository.StationRepository
import com.dorian_bichet.essence_maintenant.ui.screens.MapScreen
import com.russhwolf.settings.Settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun App() {
    val repository = remember { StationRepository() }

    // 💾 Initialisation de l'outil de stockage multiplateforme
    val settings = remember { Settings() }

    // 1. RESTAURATION des carburants sauvegardés (ou Gazole par défaut)
    val initialCarburants = remember {
        val savedString = settings.getStringOrNull("KEY_SELECTED_FUELS")
        if (!savedString.isNullOrBlank()) {
            savedString.split(",")
                .mapNotNull { name -> runCatching { CarburantType.valueOf(name) }.getOrNull() }
                .toSet()
        } else {
            setOf(CarburantType.GAZOLE)
        }
    }

    // 2. RESTAURATION de la position de la caméra (ou France par défaut)
    val initialLat = remember { settings.getDouble("KEY_CAM_LAT", 46.2276) }
    val initialLng = remember { settings.getDouble("KEY_CAM_LNG", 2.2137) }
    val initialZoom = remember { settings.getFloat("KEY_CAM_ZOOM", 5.5f) }

    // États Compose de l'application
    var stations by remember { mutableStateOf<List<Station>>(emptyList()) }
    var selectedCarburants by remember { mutableStateOf(initialCarburants) }
    var isLoading by remember { mutableStateOf(true) }

    // 📝 SAUVEGARDE AUTOMATIQUE : Dès que la sélection de carburants change
    LaunchedEffect(selectedCarburants) {
        val stringToSave = selectedCarburants.joinToString(",") { it.name }
        settings.putString("KEY_SELECTED_FUELS", stringToSave)
    }

    // Chargement initial de l'API
    LaunchedEffect(Unit) {
        try {
            stations = repository.getStations()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                MapScreen(
                    stations = stations,
                    selectedCarburants = selectedCarburants,
                    onCarburantsChanged = { nouveauxChoix ->
                        if (nouveauxChoix.isNotEmpty()) {
                            selectedCarburants = nouveauxChoix
                        }
                    },
                    repository = repository,
                    // 🛠️ On transmet les valeurs de caméra restaurées et l'accès aux settings
                    initialLat = initialLat,
                    initialLng = initialLng,
                    initialZoom = initialZoom,
                    settings = settings
                )
            }
        }
    }
}