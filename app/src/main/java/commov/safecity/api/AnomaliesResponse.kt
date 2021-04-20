package commov.safecity.api

import java.sql.Blob

data class Anomaly(
    val id: Int,
    val local: String,
    val description: String,
    val type: String,
    val location: AnomalyLocation,
    val photo: String,
    val userID: Int
)

data class AnomalyLocation(
    val lat: Double,
    val lng: Double
)