package commov.safecity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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