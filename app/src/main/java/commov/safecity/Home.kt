package commov.safecity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.LoginPostResponse
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Home : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.google_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getAnomalies()

        call.enqueue(object : Callback<List<Anomaly>> {
            override fun onResponse(call: Call<List<Anomaly>>, response: Response<List<Anomaly>>) {
                if (response.isSuccessful) {
                    val anomalies = response.body()!!
                    for (anomaly in anomalies) {
                        val latlng = LatLng(anomaly.location.lat, anomaly.location.lng)
                        map.addMarker(MarkerOptions().position(latlng).title(anomaly.local))
                    }


                }
            }

            override fun onFailure(call: Call<List<Anomaly>>, t: Throwable) {
                Toast.makeText(this@Home, "Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Location Permission
    private val REQUEST_LOCATION_PERMISSION = 1

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        val viana = LatLng(41.6918, -8.8344)
        // map.addMarker(MarkerOptions().position(viana).title("Marker in Viana do Castelo"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(viana, 15f))
        enableMyLocation()
    }

    // Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        return if (loginSharedPref.getBoolean("logged", false)) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.non_logged_menu, menu)
            true
        } else {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.logged_menu, menu)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.login -> {
                val intent = Intent(this@Home, Login::class.java)
                startActivity(intent)
                true
            }
            R.id.logout -> {
                val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
                with(loginSharedPref.edit()) {
                    clear()
                    apply()
                }
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
}