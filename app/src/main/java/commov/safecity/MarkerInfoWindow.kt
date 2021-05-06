package commov.safecity

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.Button
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindow(context: Activity) : GoogleMap.InfoWindowAdapter {
    @SuppressLint("InflateParams")
    private var window = context.layoutInflater.inflate(R.layout.marker_infowindow, null)

    private fun bindWindowText(marker: Marker, view: View) {
//        val markerType = view.findViewById<TextView>(R.id.marker_type)
//        val markerPhoto = view.findViewById<ImageView>(R.id.marker_photo)
        val visualizeButton = view.findViewById<Button>(R.id.marker_visualize_button)
        val deleteButton = view.findViewById<Button>(R.id.marker_delete_button)

//        Log.i("Marker", marker.tag!!.toString())
//        val anomaly: Anomaly = marker.tag as Anomaly
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