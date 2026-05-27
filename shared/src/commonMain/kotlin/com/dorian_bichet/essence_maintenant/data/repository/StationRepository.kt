package com.dorian_bichet.essence_maintenant.data.repository

import com.dorian_bichet.essence_maintenant.data.model.CarburantType
import com.dorian_bichet.essence_maintenant.data.model.PrixStatut
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.remote.CarburantApi

class StationRepository(private val api: CarburantApi = CarburantApi()) {

    private var cachedStations: List<Station> = emptyList()

    private val seuilsVerts = mutableMapOf<CarburantType, Double>()
    private val seuilsRouges = mutableMapOf<CarburantType, Double>()

    suspend fun getStations(forceRefresh: Boolean = false): List<Station> {
        if (cachedStations.isEmpty() || forceRefresh) {
            cachedStations = api.fetchStations()
            computeGlobalThresholds()
        }
        return cachedStations
    }

    private fun computeGlobalThresholds() {
        seuilsVerts.clear()
        seuilsRouges.clear()

        CarburantType.entries.forEach { carburant ->
            val tousLesPrix = cachedStations
                .mapNotNull { it.prix[carburant] }
                .sorted()

            if (tousLesPrix.isNotEmpty()) {
                val indexTiersBas = (tousLesPrix.size * 0.33).toInt()
                val indexTiersHaut = (tousLesPrix.size * 0.66).toInt()

                seuilsVerts[carburant] = tousLesPrix.getOrNull(indexTiersBas) ?: tousLesPrix.first()
                seuilsRouges[carburant] = tousLesPrix.getOrNull(indexTiersHaut) ?: tousLesPrix.last()
            }
        }
    }

    fun getStatutPrix(station: Station, carburant: CarburantType): PrixStatut {
        val prixStation = station.prix[carburant] ?: return PrixStatut.INCONNU
        val seuilVert = seuilsVerts[carburant] ?: return PrixStatut.INCONNU
        val seuilRouge = seuilsRouges[carburant] ?: return PrixStatut.INCONNU

        return when {
            prixStation <= seuilVert -> PrixStatut.VERT
            prixStation >= seuilRouge -> PrixStatut.ROUGE
            else -> PrixStatut.ORANGE
        }
    }
}