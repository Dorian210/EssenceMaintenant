package com.dorian_bichet.essence_maintenant.data.remote

// On importe notre modèle propre pour le type de retour
import com.dorian_bichet.essence_maintenant.data.model.Station
import com.dorian_bichet.essence_maintenant.data.model.toStation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class CarburantApi {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun fetchStations(): List<Station> {
        val url = "https://data.economie.gouv.fr/api/explore/v2.1/catalog/datasets/prix-des-carburants-en-france-flux-instantane-v2/exports/json"
        val apiStations: List<ApiStation> = httpClient.get(url).body()
        return apiStations.map { it.toStation() }
//        val response: CarburantResponse = httpClient.get(url).body()
//        return response.results.map { apiStation -> apiStation.toStation() }
    }
}