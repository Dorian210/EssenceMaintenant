package com.dorian_bichet.essence_maintenant.data.model

import com.dorian_bichet.essence_maintenant.data.remote.ApiStation

enum class PrixStatut {
    VERT, ORANGE, ROUGE, INCONNU
}

data class Station(
    val id: String,
    val adresse: String,
    val ville: String,
    val latitude: Double,
    val longitude: Double,
    val prix: Map<CarburantType, Double>,
    val distanceKm: Double? = null
)

fun ApiStation.toStation(): Station {
    val mapPrix = mutableMapOf<CarburantType, Double>()
    gazolePrix?.let { mapPrix[CarburantType.GAZOLE] = it }
    sp95Prix?.let { mapPrix[CarburantType.SP95] = it }
    sp98Prix?.let { mapPrix[CarburantType.SP98] = it }
    e10Prix?.let { mapPrix[CarburantType.E10] = it }
    e85Prix?.let { mapPrix[CarburantType.E85] = it }
    gplcPrix?.let { mapPrix[CarburantType.GPL] = it }

    return Station(
        id = this.id.toString(),
        adresse = this.adresse?.trim() ?: "Adresse inconnue",
        ville = this.ville?.trim() ?: "Ville inconnue",
        latitude = this.geom.lat,
        longitude = this.geom.lon,
        prix = mapPrix,
        distanceKm = null
    )
}