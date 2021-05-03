package commov.safecity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VisualizeAnomaly : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualize_anomaly)

        val loginSharedPref: SharedPreferences = getSharedPreferences(applicationContext.getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        val userID = loginSharedPref.getInt("loggedUserID", 0)

        val anomaly = intent?.getParcelableArrayListExtra<Anomaly>("markerTag")

        val latTextView = findViewById<TextView>(R.id.visualize_anomaly_anomalyLat)
        latTextView.text = getString(R.string.visualize_anomaly_anomalyLat).plus("         ").plus(anomaly?.get(0)?.location?.lat)
        val lngTextView = findViewById<TextView>(R.id.visualize_anomaly_anomalyLng)
        lngTextView.text = getString(R.string.visualize_anomaly_anomalyLng).plus("     ").plus(anomaly?.get(0)?.location?.lng)

        val titleTextInputLayout = findViewById<TextInputLayout>(R.id.visualize_anomaly_anomalyTitle_textInputLayout)
        titleTextInputLayout.editText?.text = SpannableStringBuilder(anomaly?.get(0)?.title)
        val descTextInputLayout = findViewById<TextInputLayout>(R.id.visualize_anomaly_anomalyDesc_textInputLayout)
        descTextInputLayout.editText?.text = SpannableStringBuilder(anomaly?.get(0)?.description)

        val deleteAnomalyButton = findViewById<Button>(R.id.visualize_anomaly_deleteButton)
        val editAnomalyButton = findViewById<Button>(R.id.visualize_anomaly_editButton)
        if(userID == anomaly?.get(0)?.userID) {
            deleteAnomalyButton.isVisible = true
            editAnomalyButton.isVisible = true
        }
        deleteAnomalyButton.setOnClickListener {
            val builder = AlertDialog.Builder(window.context)
            builder.apply {
                setTitle(R.string.visualize_anomaly_delete_popup_title)
                setMessage(R.string.visualize_anomaly_delete_popup_message)
                setPositiveButton(R.string.visualize_anomaly_delete_popup_yes
                ) { dialog, _ ->
                    // User clicked Yes button
                    val request = ServiceBuilder.buildService(EndPoints::class.java)
                    val call = request.deleteAnomalyById(id = anomaly?.get(0)?.id!!)

                    call.enqueue(object : Callback<Anomaly> {
                        override fun onResponse(call: Call<Anomaly>, response: Response<Anomaly>) {
                            if (response.isSuccessful) {
                                Log.i("VisualizeAnomaly", "Delete anomaly successful")

                                val intent = Intent(this@VisualizeAnomaly, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                        override fun onFailure(call: Call<Anomaly>?, t: Throwable?) {
                            Log.i("VisualizeAnomaly", "Delete anomaly failed")
                            val intent = Intent(this@VisualizeAnomaly, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                    dialog.dismiss()
                }
                setNegativeButton(R.string.visualize_anomaly_delete_popup_cancel
                ) { dialog, _ ->
                    // User cancelled the dialog
                    dialog.dismiss()
                }
            }
            // Create the AlertDialog
            builder.create()
            builder.show()
        }
    }
}