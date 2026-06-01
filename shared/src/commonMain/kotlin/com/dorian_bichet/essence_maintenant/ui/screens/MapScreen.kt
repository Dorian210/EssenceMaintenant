package com.dorian_bichet.essence_maintenant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.model.toGeoJsonFeature
import com.dorian_bichet.essence_maintenant.data.repository.StationRepository
import com.dorian_bichet.essence_maintenant.ui.components.FuelSelector
import com.dorian_bichet.essence_maintenant.ui.components.MapSettingsDrawer
import com.dorian_bichet.essence_maintenant.utils.LocationPermissionState
import com.dorian_bichet.essence_maintenant.utils.openMapRouting
import com.dorian_bichet.essence_maintenant.utils.rememberLocationPermissionState
import com.russhwolf.settings.Settings
import essencemaintenant.shared.generated.resources.Res
import essencemaintenant.shared.generated.resources.layers
import essencemaintenant.shared.generated.resources.loc
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.camera.CameraMoveReason
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.format
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.span
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.location.BearingUpdate
import org.maplibre.compose.location.LocationPuck
import org.maplibre.compose.location.LocationTrackingEffect
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberNullLocationProvider
import org.maplibre.compose.location.rememberUserLocationState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Position

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
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentMapStyle by remember {
        val savedStyleName = settings.getStringOrNull("KEY_MAP_STYLE")
        val savedStyle = savedStyleName?.let { name ->
            runCatching { MapStyle.valueOf(name) }.getOrNull()
        }
        mutableStateOf(savedStyle ?: MapStyle.POSITRON)
    }

    val camera = rememberCameraState(
        firstPosition = CameraPosition(
            target = Position(latitude = initialLat, longitude = initialLng),
            zoom = initialZoom.toDouble()
        )
    )

    // GPS & Localisation
    val permissionState = rememberLocationPermissionState()
    val locationProvider = if (permissionState.isGranted) {
        rememberDefaultLocationProvider()
    } else {
        rememberNullLocationProvider()
    }
    val userLocationState = rememberUserLocationState(
        locationProvider = locationProvider,
        samplePeriod = 2000.milliseconds // On réduit la fréquence (2s) pour les performances
    )
    var isTrackingUser by remember { mutableStateOf(false) }

    // On arrête de suivre si l'utilisateur déplace la carte manuellement (GESTURE)
    LaunchedEffect(camera.moveReason) {
        if (camera.moveReason == CameraMoveReason.GESTURE) {
            isTrackingUser = false
        }
    }

    // Effet de suivi pour recentrer la caméra automatiquement sur chaque mise à jour GPS
    LocationTrackingEffect(
        locationState = userLocationState,
        enabled = isTrackingUser,
        trackBearing = false, // On ne suit pas l'orientation du téléphone
    ) {
        camera.updateFromLocation(
            animationDuration = 500.milliseconds,
            updateBearing = BearingUpdate.IGNORE // On conserve l'angle de vue actuel
        )
    }

    LaunchedEffect(currentMapStyle) {
        settings.putString("KEY_MAP_STYLE", currentMapStyle.name)
    }

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
        stations.map { it.toGeoJsonFeature(selectedCarburants, repository) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            MapSettingsDrawer(
                currentMapStyle = currentMapStyle,
                onStyleSelected = { style ->
                    currentMapStyle = style
                    scope.launch { drawerState.close() }
                },
                onClose = {
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            MaplibreMap(
                modifier = Modifier.fillMaxSize(),
                cameraState = camera,
                baseStyle = BaseStyle.Uri(currentMapStyle.url)
            ) {
                // On isole la lecture de la location dans un bloc pour éviter de recomposer tout MapScreen
                val currentLocation = userLocationState.location
                
                // Marqueur de position utilisateur (le "puck" bleu)
                LocationPuck(
                    idPrefix = "user-pos",
                    location = currentLocation,
                    cameraState = camera
                )

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
                    textField = format(
                        span(feature["label_prix"].asString()),
                        span("\n"),
                        span(feature["fuel"].asString(), textSize = const(.6f.em)),
                    ),
                    textAllowOverlap = const(true),
                    textSize = const(11f.sp),
                    textFont = const(listOf("Noto Sans Regular")),
                    textKeepUpright = const(true),
                    textOverlap = feature["must_show"].cast(),
                    textOffset = offset(0f.em, -2.2f.em),
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

            // Top controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.layers),
                            contentDescription = "Style de carte"
                        )
                    }

                    FuelSelector(
                        selectedCarburants = selectedCarburants,
                        onCarburantsChanged = onCarburantsChanged
                    )
                }
            }

            // Bouton Ma Position
            FloatingActionButton(
                onClick = {
                    if (permissionState.isGranted) {
                        // 1. On active le suivi automatique
                        isTrackingUser = true
                        
                        // 2. On force un recentrage immédiat si une position est déjà connue
                        userLocationState.location?.let { loc ->
                            scope.launch {
                                camera.animateTo(
                                    finalPosition = camera.position.copy(target = loc.position.value),
                                    duration = 500.milliseconds
                                )
                            }
                        }
                    } else {
                        permissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp),
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    painter = painterResource(Res.drawable.loc),
                    contentDescription = "Ma position"
                )
            }
        }
    }
}
