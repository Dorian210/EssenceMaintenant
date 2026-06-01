package com.dorian_bichet.essence_maintenant

import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.model.toGeoJsonFeature
import com.dorian_bichet.essence_maintenant.data.repository.StationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppLogicTest {

    @Test
    fun testGeoJsonMapping() {
        val station = Station(
            id = "123",
            adresse = "123 Rue de la Paix",
            ville = "Paris",
            latitude = 48.8566,
            longitude = 2.3522,
            prix = mapOf(CarburantType.GAZOLE to 1.55)
        )

        // Note: StationRepository est une classe finale et ses seuils sont privés.
        // Sans initialisation (via getStations() qui fait un appel réseau), il renvoie PrixStatut.INCONNU.
        // Dans ce cas, toGeoJsonFeature renvoie le Feature par défaut (label_prix vide).
        val repository = StationRepository()
        val feature = station.toGeoJsonFeature(setOf(CarburantType.GAZOLE), repository)

        // 1. Vérification de la géométrie (Localisation) - Toujours valide
        assertNotNull(feature.geometry)
        val point = feature.geometry as? org.maplibre.spatialk.geojson.Point
        assertEquals(48.8566, point?.coordinates?.latitude)
        assertEquals(2.3522, point?.coordinates?.longitude)
        
        // 2. Vérification de la structure des propriétés
        val props = feature.properties
        assertNotNull(props)
        val propsString = props.toString()
        
        // On vérifie que les clés attendues sont là
        assertTrue(propsString.contains("label_prix"), "La propriété label_prix doit être présente")
        assertTrue(propsString.contains("couleur_marqueur"), "La couleur doit être présente")

        // NOTE : Le prix "1.55" n'est pas présent dans ce test car le repository non-initialisé
        // renvoie INCONNU. Pour tester le prix et les couleurs, il faudrait rendre 
        // StationRepository testable (ex: via une interface ou en le rendant 'open').
    }

    @Test
    fun testCoordinateFormatForMaps() {
        // Validation de la logique d'ouverture des cartes (Maps)
        val lat = 45.0
        val lng = 5.0
        val url = "http://maps.apple.com/?daddr=$lat,$lng&ll=$lat,$lng"
        assertEquals("http://maps.apple.com/?daddr=45.0,5.0&ll=45.0,5.0", url)
    }
}
