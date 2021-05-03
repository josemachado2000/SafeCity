package commov.safecity.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Anomaly(
    val id: Int,
    val title: String,
    val description: String,
    val location: AnomalyLocation,
    val photo: String?,
    val userID: Int,
    val typeID: Int,
    val type: String
) : Parcelable

@Parcelize
data class AnomalyLocation(
    val lat: Double,
    val lng: Double
) : Parcelable