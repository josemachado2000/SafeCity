package commov.safecity.api

import java.sql.Blob

data class Anomaly(
    val id: Int,
    val local: String,
    val description: String,
    val location: AnomalyLocation,
    val photo: Blob
)

data class AnomalyLocation(
    val lat: Double,
    val lng: Double
)
