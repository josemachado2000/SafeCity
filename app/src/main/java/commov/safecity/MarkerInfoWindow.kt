package commov.safecity

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import commov.safecity.api.Anomaly

class MarkerInfoWindow(context: Activity, marker: Marker) : GoogleMap.InfoWindowAdapter {
    @SuppressLint("InflateParams")
    private var window = context.layoutInflater.inflate(R.layout.marker_infowindow, null)

    private fun bindWindowText(marker: Marker, view: View) {
        val markerType = view.findViewById<TextView>(R.id.marker_type)
        val markerTitle = view.findViewById<TextView>(R.id.marker_title)
        val markerSnippet = view.findViewById<TextView>(R.id.marker_snippet)

        // Log.i("Marker", marker.tag!!.toString())
        val anomaly: Anomaly = marker.tag as Anomaly

        markerType.text = anomaly.type
        markerTitle.text = anomaly.title
        markerSnippet.text = anomaly.description
    }

    override fun getInfoContents(marker: Marker): View {
        bindWindowText(marker, window)
        return window
    }

    override fun getInfoWindow(marker: Marker): View? {
        bindWindowText(marker, window)
        return window
    }
}