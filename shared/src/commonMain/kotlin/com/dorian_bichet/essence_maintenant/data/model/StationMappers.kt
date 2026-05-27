package com.dorian_bichet.essence_maintenant.data.model

import com.dorian_bichet.essence_maintenant.data.repository.StationRepository
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.Position

fun Station.toGeoJsonFeature(
    selectedCarburants: Set<CarburantType>,
    repository: StationRepository
): Feature<*, *> {
    val default = Feature(
        geometry = Point(Position(latitude = latitude, longitude = longitude)),
        properties = buildJsonObject {
            put("label_prix", "")
            put("fuel", "")
            put("couleur_marqueur", "#808080")
            put("must_show", "never")
            put("sort_depth", 10f)
        }
    )

    // 1. Trouver le meilleur prix parmi les carburants sélectionnés dispo dans la station
    val meilleurChoix = selectedCarburants
        .mapNotNull { carburant ->
            val prix = prix[carburant]
            if (prix != null) Pair(carburant, prix) else null
        }
        .minByOrNull { pair -> pair.second } // On cherche le prix le plus bas

    if (meilleurChoix != null) {
        val (carburantGagnant, prixGagnant) = meilleurChoix

        // 2. On demande le statut couleur au repo pour CE carburant précis
        val statut = repository.getStatutPrix(this, carburantGagnant)
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

            return Feature(
                geometry = Point(Position(latitude = latitude, longitude = longitude)),
                properties = buildJsonObject {
                    // On affiche le prix le plus bas trouvé !
                    val parts = prixGagnant.toString().split(".")
                    val integerPart = parts[0]
                    val decimalPart = if (parts.size > 1) parts[1].take(2).padEnd(2, '0') else "00"
                    val formattedPrice = "$integerPart.$decimalPart"

                    put("label_prix", "${formattedPrice}€")
                    put("fuel", carburantGagnant.toString())
                    put("couleur_marqueur", couleurHex)
                    put("must_show", overlap)
                    put("sort_depth", prixGagnant)
                }
            )
        }
    }
    return default
}
