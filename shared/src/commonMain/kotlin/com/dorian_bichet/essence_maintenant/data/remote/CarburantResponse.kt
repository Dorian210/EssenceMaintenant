package com.dorian_bichet.essence_maintenant.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CarburantResponse(
    @SerialName("total_count") val totalCount: Int,
    val results: List<ApiStation>
)

@Serializable
data class ApiStation(
    val id: Int,
    val adresse: String? = "",
    val ville: String? = "",
    val geom: ApiGeom,
    @SerialName("gazole_prix") val gazolePrix: Double? = null,
    @SerialName("sp95_prix") val sp95Prix: Double? = null,
    @SerialName("sp98_prix") val sp98Prix: Double? = null,
    @SerialName("e10_prix") val e10Prix: Double? = null,
    @SerialName("e85_prix") val e85Prix: Double? = null,
    @SerialName("gplc_prix") val gplcPrix: Double? = null
)

@Serializable
data class ApiGeom(
    val lat: Double,
    val lon: Double
)