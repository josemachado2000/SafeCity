package commov.safecity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.LayoutInflater
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Home : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    private val checkedItems = booleanArrayOf(false, false, false, false, false)
    private lateinit var thumbView: View

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
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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

        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                lastLocation = p0.lastLocation
                val location = LatLng(lastLocation.latitude, lastLocation.longitude)
                Log.i("Location", location.latitude.toString() + "   " + location.longitude.toString())
            }
        }
        thumbView = LayoutInflater.from(this@Home).inflate(R.layout.seekbar_thumb, null, false)
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

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun startLocationUpdates() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        Log.i("Distance", results[0].toString())
        return results[0]
    }

    private lateinit var anomalies: List<Anomaly>
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnInfoWindowClickListener(this)
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
                        if (anomaly.userID == userID) {
                            val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                            val marker: Marker? = map.addMarker(MarkerOptions()
                                    .position(markerLatLng)
                                    .title(anomaly.type)
                                    .snippet(anomaly.photo)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )
                            marker?.tag = anomaly
                        } else {
                            val markerLatLng = LatLng(anomaly.location.lat, anomaly.location.lng)
                            val marker: Marker? = map.addMarker(MarkerOptions()
                                    .position(markerLatLng)
                                    .title(anomaly.type)
                                    .snippet(anomaly.photo)
                            )
                            marker?.tag = anomaly
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
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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

        fun getThumb(progress: Int): Drawable {
            when (progress) {
                0 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_all) }
                1 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_500m) }
                2 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_1km) }
                3 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_5km) }
                4 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_10km) }
                5 -> { (thumbView.findViewById(R.id.seekBar_distance_progress) as TextView).text = getString(R.string.thumb_more10km) }
            }
            thumbView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            val bitmap: Bitmap = Bitmap.createBitmap(thumbView.measuredWidth, thumbView.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            thumbView.layout(0, 0, thumbView.measuredWidth, thumbView.measuredHeight)
            thumbView.draw(canvas)
            return BitmapDrawable(resources, bitmap)
        }

        val distanceSeekBar = findViewById<SeekBar>(R.id.home_distance_seekBar)
        distanceSeekBar.thumb = getThumb(0)
        distanceSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                // write custom code for progress is changed
                Log.i("Distance", progress.toString())
                if (progress == 0) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    onMapReady(map)
                }
                if (progress == 1) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    for (A in anomalies) {
                        if (calculateDistance(lastLocation.latitude, lastLocation.longitude, A.location.lat, A.location.lng) <= 500) {
                            if (A.userID == userID) {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )
                            } else {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                )
                            }
                        }
                    }
                }
                if (progress == 2) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    for (A in anomalies) {
                        if (calculateDistance(lastLocation.latitude, lastLocation.longitude, A.location.lat, A.location.lng) <= 1000) {
                            if (A.userID == userID) {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )
                            } else {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                )
                            }
                        }
                    }
                }
                if (progress == 3) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    for (A in anomalies) {
                        if (calculateDistance(lastLocation.latitude, lastLocation.longitude, A.location.lat, A.location.lng) <= 5000) {
                            if (A.userID == userID) {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )
                            } else {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                )
                            }
                        }
                    }
                }
                if (progress == 4) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    for (A in anomalies) {
                        if (calculateDistance(lastLocation.latitude, lastLocation.longitude, A.location.lat, A.location.lng) <= 10000) {
                            if (A.userID == userID) {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )
                            } else {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                )
                            }
                        }
                    }
                }
                if (progress == 5) {
                    seek.thumb = getThumb(progress)
                    map.clear()
                    for (A in anomalies) {
                        if (calculateDistance(lastLocation.latitude, lastLocation.longitude, A.location.lat, A.location.lng) > 10000) {
                            if (A.userID == userID) {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                )
                            } else {
                                val markerLatLng = LatLng(A.location.lat, A.location.lng)
                                map.addMarker(MarkerOptions()
                                        .position(markerLatLng)
                                        .title(A.type)
                                )
                            }
                        }
                    }
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })
    }

    override fun onInfoWindowClick(marker: Marker) {
        val anomaly = arrayListOf<Anomaly>()
        anomaly.add(marker.tag as Anomaly)
        val intent = Intent(this@Home, VisualizeAnomaly::class.java).apply { putExtra("markerTag", anomaly) }
        startActivity(intent)
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
            R.id.home_menu_typeFilter -> {
                // Set up the alert builder
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Choose Anomaly Type")

                // Add a checkbox list
                val types = arrayOf(getString(R.string.home_menu_typeFilter_accident),
                        getString(R.string.home_menu_typeFilter_roadWork),
                        getString(R.string.home_menu_typeFilter_roadObstacle),
                        getString(R.string.home_menu_typeFilter_traffic),
                        getString(R.string.home_menu_typeFilter_roadPothole)
                )

                val checkedAnomalies = arrayListOf<Anomaly>()
                Log.i("Filter",
                        "${checkedItems[0]}" +
                                "     ${checkedItems[1]}" +
                                "     ${checkedItems[2]}" +
                                "     ${checkedItems[3]}" +
                                "     ${checkedItems[4]}"
                )

                builder.setMultiChoiceItems(types, checkedItems) { dialog, which, isChecked ->
                    // The user checked or unchecked a box
                    Log.i("Filter", dialog.toString())
                    Log.i("Filter", which.toString())
                    Log.i("Filter", isChecked.toString())

                    if (which == 0 && isChecked) {
                        checkedItems[0] = true
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_accident) }
                        checkedAnomalies += filteredAnomalies
                    } else if (which == 0 && !isChecked) {
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_accident) }
                        checkedAnomalies -= filteredAnomalies
                    }
                    if (which == 1 && isChecked) {
                        checkedItems[1] = true
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadWork) }
                        checkedAnomalies += filteredAnomalies
                    } else if (which == 1 && !isChecked) {
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadWork) }
                        checkedAnomalies -= filteredAnomalies
                    }
                    if (which == 2 && isChecked) {
                        checkedItems[2] = true
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadObstacle) }
                        checkedAnomalies += filteredAnomalies
                    } else if (which == 2 && !isChecked) {
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadObstacle) }
                        checkedAnomalies -= filteredAnomalies
                    }
                    if (which == 3 && isChecked) {
                        checkedItems[3] = true
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_traffic) }
                        checkedAnomalies += filteredAnomalies
                    } else if (which == 3 && !isChecked) {
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_traffic) }
                        checkedAnomalies -= filteredAnomalies
                    }
                    if (which == 4 && isChecked) {
                        checkedItems[4] = true
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadPothole) }
                        checkedAnomalies += filteredAnomalies
                    } else if (which == 4 && !isChecked) {
                        val filteredAnomalies = anomalies.filter { it.type == getString(R.string.home_menu_typeFilter_roadPothole) }
                        checkedAnomalies -= filteredAnomalies
                    }
                }

                // Add OK and Cancel buttons
                builder.setPositiveButton("OK") { dialog, which ->
                    // The user clicked OK
                    Log.i("Filter", dialog.toString())
                    Log.i("Filter", which.toString())
                    Log.i("Filter", checkedAnomalies.toString())

                    map.clear()
                    for (anomaly in checkedAnomalies) {
                        if (anomaly.userID == userID) {
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
                }
                builder.setNegativeButton("Cancel") { _, _ ->
                    onMapReady(map)
                }

                // Create and show the alert dialog
                val dialog = builder.create()
                dialog.show()
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
            R.id.sensors -> {
                val intent = Intent(this@Home, Sensors::class.java)
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
                R.id.home_satellite_radioButton -> if (checked) {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                }
                R.id.home_terrain_radioButton -> if (checked) {
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
            }
        }
    }


}