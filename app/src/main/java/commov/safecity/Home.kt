package commov.safecity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_map)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val fabInsertAnomaly = findViewById<FloatingActionButton>(R.id.home_fab_insertAnomaly)
        fabInsertAnomaly.setOnClickListener {
            enableMyLocation()
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.i("InsertAnomaly", location.toString())
                    val currentLocation = LatLng(location.latitude, location.longitude)

                    val intent = Intent(this@Home, InsertAnomaly::class.java).apply { putExtra("currentLocation", currentLocation) }
                    startActivity(intent)
                }
            }
        }

        val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        if(!loginSharedPref.getBoolean("logged", false)) {
            fabInsertAnomaly.isInvisible = true
        }
    }

    // Location Permission
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        } else { map.isMyLocationEnabled = true }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    private lateinit var anomalies: List<Anomaly>
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true

        val loginSharedPref: SharedPreferences = getSharedPreferences(applicationContext.getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        val userID = loginSharedPref.getInt("loggedUserID", 0)

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getAnomalies()

        call.enqueue(object : Callback<List<Anomaly>> {
            override fun onResponse(call: Call<List<Anomaly>>, response: Response<List<Anomaly>>) {
                if (response.isSuccessful) {
                    Log.i("Response", response.body().toString())
                    anomalies = response.body()!!
                    for (anomaly in anomalies) {
                        if(anomaly.userID == userID) {
                            val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                            map.addMarker(MarkerOptions()
                                    .position(markerLatLng)
                                    .title(anomaly.type)
                                    .snippet(anomaly.photo)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )
                        } else {
                            val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                            map.addMarker(MarkerOptions()
                                    .position(markerLatLng)
                                    .title(anomaly.type)
                                    .snippet(anomaly.photo)
                            )
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Anomaly>>, t: Throwable) {
                Toast.makeText(this@Home, "Failed to get Anomalies", Toast.LENGTH_SHORT).show()
            }
        })

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        } else {
            map.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.i("Location", location.toString())
                    val currentLocation = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 16f))
                }
            }
        }

        map.setOnMarkerClickListener {
            map.setInfoWindowAdapter(MarkerInfoWindow(this))
            onMarkerClick()
            map.setOnInfoWindowCloseListener {
                val fabInsertAnomaly = findViewById<FloatingActionButton>(R.id.home_fab_insertAnomaly)
                fabInsertAnomaly.isVisible = true
            }
            false
        }
    }

    private fun onMarkerClick(): Boolean {
        val fabInsertAnomaly = findViewById<FloatingActionButton>(R.id.home_fab_insertAnomaly)
        fabInsertAnomaly.isInvisible = true

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false
    }

    // Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        return if (loginSharedPref.getBoolean("logged", false)) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.home_menu, menu)
            true
        } else {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.home_menu, menu)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        val userID = loginSharedPref.getInt("loggedUserID", 0)
        return when (item.itemId) {
//            R.id.login -> {
//                val intent = Intent(this@Home, Login::class.java)
//                startActivity(intent)
//                true
//            }

            // Filter options
            R.id.home_menu_typeFilter_accident -> {
                map.clear()
                val filteredAnomalies = anomalies.filter { it.type == "Acidente" }
                for (anomaly in filteredAnomalies) {
                    if(anomaly.userID == userID) {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    } else {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                        )
                    }
                }
                true
            }
            R.id.home_menu_typeFilter_roadWork -> {
                map.clear()
                val filteredAnomalies = anomalies.filter { it.type == "Obra na via" }
                for (anomaly in filteredAnomalies) {
                    if(anomaly.userID == userID) {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    } else {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                        )
                    }
                }
                true
            }
            R.id.home_menu_typeFilter_roadObstacle -> {
                map.clear()
                val filteredAnomalies = anomalies.filter { it.type == "Obstáculo na via" }
                for (anomaly in filteredAnomalies) {
                    if(anomaly.userID == userID) {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    } else {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                        )
                    }
                }
                true
            }
            R.id.home_menu_typeFilter_traffic -> {
                map.clear()
                val filteredAnomalies = anomalies.filter { it.type == "Trânsito" }
                for (anomaly in filteredAnomalies) {
                    if(anomaly.userID == userID) {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    } else {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                        )
                    }
                }
                true
            }
            R.id.home_menu_typeFilter_roadPothole -> {
                map.clear()
                val filteredAnomalies = anomalies.filter { it.type == "Buraco na via" }
                for (anomaly in filteredAnomalies) {
                    if(anomaly.userID == userID) {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        )
                    } else {
                        val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions()
                                .position(markerLatLng)
                                .title(anomaly.type)
                        )
                    }
                }
                true
            }

            R.id.logout -> {
                with(loginSharedPref.edit()) {
                    clear()
                    apply()
                }

                val intent = Intent(this@Home, Login::class.java)
                startActivity(intent)
                finishAffinity()
                true
            }
            R.id.notes -> {
                val intent = Intent(this@Home, Notes::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            val checked = view.isChecked

            when (view.getId()) {
                R.id.home_satellite_radioButton -> if (checked) { map.mapType = GoogleMap.MAP_TYPE_SATELLITE }
                R.id.home_terrain_radioButton -> if (checked) { map.mapType = GoogleMap.MAP_TYPE_NORMAL }
            }
        }
    }
}