package commov.safecity.api

data class Anomaly(
    val id: Int,
    val title: String,
    val description: String,
    val location: AnomalyLocation,
    val photo: String,
    val userID: Int,
    val typeID: Int,
    val type: String
)

data class AnomalyLocation(
    val lat: Double,
    val lng: Double
)