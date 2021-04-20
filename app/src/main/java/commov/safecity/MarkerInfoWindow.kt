package commov.safecity

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindow(context: Activity) : GoogleMap.InfoWindowAdapter {
        @SuppressLint("InflateParams")
        private var window = context.layoutInflater.inflate(R.layout.marker_infowindow, null)

        private fun bindWindowText(marker: Marker, view: View){
            val markerTitle = view.findViewById<TextView>(R.id.marker_title)
            val markerSnippet = view.findViewById<TextView>(R.id.marker_snippet)

            markerTitle.text = marker.title
            markerSnippet.text = marker.snippet
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