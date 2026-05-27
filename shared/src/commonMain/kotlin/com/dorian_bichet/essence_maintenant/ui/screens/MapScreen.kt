package com.dorian_bichet.essence_maintenant.ui.screens

import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import com.dorian_bichet.essence_maintenant.data.model.PrixStatut
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.repository.StationRepository
import com.dorian_bichet.essence_maintenant.ui.components.FuelSelector
import com.dorian_bichet.essence_maintenant.utils.openMapRouting

import essencemaintenant.shared.generated.resources.Res
import essencemaintenant.shared.generated.resources.loc

import com.russhwolf.settings.Settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.double

import org.jetbrains.compose.resources.painterResource

// Imports de base MapLibre validés
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.camera.CameraPosition

// Imports géométriques spatiaux validés
import org.maplibre.spatialk.geojson.Position
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point

// Imports pour les sources et calques validés
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.expressions.value.SymbolAnchor

// Import du DSL d'expression complet
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset

@Composable
fun MapScreen(
    stations: List<Station>,
    selectedCarburants: Set<CarburantType>,
    onCarburantsChanged: (Set<CarburantType>) -> Unit,
    repository: StationRepository,
    initialLat: Double,
    initialLng: Double,
    initialZoom: Float,
    settings: Settings
) {
    val markerIcon = painterResource(Res.drawable.loc)

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = initialLat, longitude = initialLng),
            zoom = initialZoom.toDouble()
        )
    )

    LaunchedEffect(camera) {
        snapshotFlow { camera.position }
            .distinctUntilChanged()
            .collect { position ->
                position?.let {
                    settings.putDouble("KEY_CAM_LAT", it.target.latitude)
                    settings.putDouble("KEY_CAM_LNG", it.target.longitude)
                    settings.putFloat("KEY_CAM_ZOOM", it.zoom.toFloat())
                }
            }
    }

    val geoJsonFeatures = remember(stations, selectedCarburants) {
        stations.mapNotNull { station ->

            // 1. Trouver le meilleur prix parmi les carburants sélectionnés dispo dans la station
            val meilleurChoix = selectedCarburants
                .mapNotNull { carburant ->
                    val prix = station.prix[carburant]
                    if (prix != null) Pair(carburant, prix) else null
                }
                .minByOrNull { pair -> pair.second } // On cherche le prix le plus bas

            if (meilleurChoix != null) {
                val (carburantGagnant, prixGagnant) = meilleurChoix

                // 2. On demande le statut couleur au repo pour CE carburant précis
                val statut = repository.getStatutPrix(station, carburantGagnant)
                if (statut != PrixStatut.INCONNU) {

                    val couleurHex = when (statut) {
                        PrixStatut.VERT -> "#2ECC71"
                        PrixStatut.ORANGE -> "#E67E22"
                        PrixStatut.ROUGE -> "#E74C3C"
                    }

                    val overlap = when (statut) {
                        PrixStatut.VERT -> "always"
                        PrixStatut.ORANGE -> "cooperative"
                        PrixStatut.ROUGE -> "never"
                    }

                    Feature(
                        geometry = Point(Position(latitude = station.latitude, longitude = station.longitude)),
                        properties = buildJsonObject {
                            // On affiche le prix le plus bas trouvé !
                            val parts = prixGagnant.toString().split(".")
                            val integerPart = parts[0]
                            val decimalPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"
                            val formattedPrice = "$integerPart.$decimalPart"

                            put("label_prix", "${formattedPrice}€")
                            put("couleur_marqueur", couleurHex)
                            put("must_show", overlap)
                            put("sort_depth", prixGagnant)
                        }
                    )
                } else null
            } else null // La station ne propose aucun des carburants sélectionnés
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = camera,
            baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/positron")
        ) {
            val sourceDonnees = rememberGeoJsonSource(
                data = GeoJsonData.Features(FeatureCollection(geoJsonFeatures))
            )

            SymbolLayer(
                id = "couche-prix-essence",
                source = sourceDonnees,
                sortKey = feature["sort_depth"].cast(),
                iconImage = image(markerIcon, drawAsSdf = true),
                iconColor = feature["couleur_marqueur"].cast(),
                iconSize = const(2f),
                iconAnchor = const(SymbolAnchor.Bottom),
                iconKeepUpright = const(true),
                iconOverlap = feature["must_show"].cast(),
                textField = feature["label_prix"].cast(),
                textAllowOverlap = const(true),
                textSize = const(11f.sp),
                textFont = const(listOf("Noto Sans Regular")),
                textKeepUpright = const(true),
                textOverlap = feature["must_show"].cast(),
                textOffset = offset(0f.em, -2.5f.em),
                textColor = const(Color.White),

                onClick = { features ->
                    val stationCliquee = features.firstOrNull()
                    val pointGeometrique = stationCliquee?.geometry as? org.maplibre.spatialk.geojson.Point
                    val position = pointGeometrique?.coordinates
                    if (position != null) {
                        openMapRouting(
                            latitude = position.latitude,
                            longitude = position.longitude
                        )
                    }
                    ClickResult.Consume
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            FuelSelector(
                selectedCarburants = selectedCarburants,
                onCarburantsChanged = onCarburantsChanged
            )
        }
    }
}