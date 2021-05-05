package commov.safecity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.api.Anomaly
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import commov.safecity.api.Type
import it.sauronsoftware.ftp4j.FTPClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class VisualizeAnomaly : AppCompatActivity() {
    private var photoPath: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.visualize_anomaly)

        val loginSharedPref: SharedPreferences = getSharedPreferences(applicationContext.getString(R.string.login_preference_file), Context.MODE_PRIVATE)
        val userID = loginSharedPref.getInt("loggedUserID", 0)

        val anomaly = intent?.getParcelableArrayListExtra<Anomaly>("markerTag")

        // Types Spinner
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getTypes()

        val types = ArrayList<String>()
        var responseTypes = listOf<Type>()
        val typesSpinner = findViewById<Spinner>(R.id.visualize_anomaly_type_spinner)
        typesSpinner.isEnabled = false
        types.add(0, anomaly?.get(0)?.type!!)
        call.enqueue(object : Callback<List<Type>> {
            override fun onResponse(call: Call<List<Type>>, response: Response<List<Type>>) {
                if (response.isSuccessful) {
                    responseTypes = response.body()
                    for (T in responseTypes) {
                        if(T.type != anomaly[0]?.type!!)
                            types.add(T.type)
                    }

                    if (typesSpinner != null) {
                        val adapter = ArrayAdapter(
                                applicationContext,
                                android.R.layout.simple_spinner_item,
                                types
                        )
                        typesSpinner.adapter = adapter
                    }
                }
            }

            override fun onFailure(call: Call<List<Type>>, t: Throwable) {
                Toast.makeText(this@VisualizeAnomaly, "Failed to load types", Toast.LENGTH_SHORT).show()
            }
        })

        val latTextView = findViewById<TextView>(R.id.visualize_anomaly_anomalyLat)
        latTextView.text = getString(R.string.visualize_anomaly_anomalyLat).plus("         ").plus(anomaly[0]?.location?.lat)
        val lngTextView = findViewById<TextView>(R.id.visualize_anomaly_anomalyLng)
        lngTextView.text = getString(R.string.visualize_anomaly_anomalyLng).plus("     ").plus(anomaly[0]?.location?.lng)

        val titleTextInputLayout = findViewById<TextInputLayout>(R.id.visualize_anomaly_anomalyTitle_textInputLayout)
        titleTextInputLayout.editText?.text = SpannableStringBuilder(anomaly[0]?.title)
        val descTextInputLayout = findViewById<TextInputLayout>(R.id.visualize_anomaly_anomalyDesc_textInputLayout)
        descTextInputLayout.editText?.text = SpannableStringBuilder(anomaly[0]?.description)

        val deleteAnomalyButton = findViewById<Button>(R.id.visualize_anomaly_deleteButton)
        val editAnomalyButton = findViewById<Button>(R.id.visualize_anomaly_editButton)
        if(userID == anomaly[0]?.userID) {
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
                    val requestAnomaly = ServiceBuilder.buildService(EndPoints::class.java)
                    val callAnomaly = requestAnomaly.deleteAnomalyById(id = anomaly[0]?.id!!)

                    callAnomaly.enqueue(object : Callback<Anomaly> {
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

        editAnomalyButton.setOnClickListener {
            val saveEditAnomalyButton = findViewById<Button>(R.id.visualize_anomaly_saveAnomaly_button)
            saveEditAnomalyButton.isVisible = true
            editAnomalyButton.isInvisible = true
            deleteAnomalyButton.isInvisible = true

            titleTextInputLayout.editText?.isEnabled = true
            descTextInputLayout.editText?.isEnabled = true
            typesSpinner.isEnabled = true

            val typeSelected = object {
                var type_id = 0
                var type = ""
            }

            typesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    typeSelected.type = types[position]
                    for (T in responseTypes) {
                        if(T.type === types[position]) {
                            typeSelected.type_id = T.id
                            // Log.i("Insert", "${T.type} ${types[position]} ${typeSelected.type_id} ${T.id}")
                        }

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Log.i("InsertAnomaly", getString(R.string.insert_anomaly_emptySpinnerMessage))
                }
            }

            val buttonUploadPhoto = findViewById<ImageView>(R.id.visualize_anomaly_anomalyPhoto)
            buttonUploadPhoto.setOnClickListener {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    resultLauncherFileChooser.launch(Intent.createChooser(intent, getString(R.string.visualize_anomaly_fileChooserMessage)))
                }
            }

            saveEditAnomalyButton.setOnClickListener {
                // Input Validations
                val title = titleTextInputLayout.editText?.text.toString().trim()
                var validTitle = false
                when {
                    title.isEmpty() -> {
                        titleTextInputLayout.error = getString(R.string.update_anomaly_ObligationError)
                    }
                    title.length > 50 -> {
                        titleTextInputLayout.error = getString(R.string.update_anomaly_titleLengthError)
                    }
                    else -> {
                        titleTextInputLayout.error = ""
                        validTitle = true
                    }
                }

                val description = descTextInputLayout.editText?.text.toString().trim()
                var validDesc = false
                when {
                    description.isEmpty() -> {
                        descTextInputLayout.error = getString(R.string.update_anomaly_ObligationError)
                    }
                    description.length > 160 -> {
                        descTextInputLayout.error = getString(R.string.update_anomaly_descLengthError)
                    }
                    else -> {
                        descTextInputLayout.error = ""
                        validDesc = true
                    }
                }

                if (validTitle && validDesc) {
                    Log.i("UpdateAnomaly", "${anomaly[0].id} $title $description ${anomaly[0].location.lat} ${anomaly[0].location.lng}" +
                            "$photoPath ${loginSharedPref.getInt("loggedUserID", 0)} ${typeSelected.type_id} ${typeSelected.type}")

                    val photo = File(photoPath)
                    val requestPutAnomaly = ServiceBuilder.buildService(EndPoints::class.java)
                    val callPutAnomaly = requestPutAnomaly.updateAnomaly(
                            id = anomaly[0].id,
                            title = title,
                            description = description,
                            lat = anomaly[0].location.lat,
                            lng = anomaly[0].location.lng,
                            photo = photo.name,
                            userID = userID,
                            typeID = typeSelected.type_id
                    )

                    Log.i("UpdateAnomaly", callPutAnomaly.request().toString())
                    callPutAnomaly.enqueue(object : Callback<Anomaly> {
                        override fun onResponse(call: Call<Anomaly>, response: Response<Anomaly>) {
                            if (response.isSuccessful) {
                                val SDK_INT = Build.VERSION.SDK_INT
                                if (SDK_INT > 8) {
                                    val policy = StrictMode
                                            .ThreadPolicy
                                            .Builder()
                                            .permitAll()
                                            .build()
                                    StrictMode.setThreadPolicy(policy)
                                    try {
                                        val mFtpClient = FTPClient()
                                        mFtpClient.connect("files.000webhost.com", 21)
                                        mFtpClient.login("safecity-commov", "Safecitypass-123")
                                        mFtpClient.type = FTPClient.TYPE_BINARY
                                        mFtpClient.changeDirectory("/public_html/SafeCity/images")
                                        mFtpClient.upload(photo)
                                        mFtpClient.disconnect(true)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Log.i("FTP", "Failed to upload image via FTP")
                                    }
                                }
                                val intent = Intent(this@VisualizeAnomaly, Home::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }

                        override fun onFailure(call: Call<Anomaly>, t: Throwable) {
                            Log.i("InsertAnomaly", "Failed to update anomaly")
                            val intent = Intent(this@VisualizeAnomaly, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var resultLauncherFileChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val selectedPhotoURI: Uri = result?.data?.data!!
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor: Cursor = contentResolver.query(selectedPhotoURI, filePathColumn, null, null, null)!!
            cursor.moveToFirst()
            val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
            photoPath = cursor.getString(columnIndex)
            cursor.close()

            val buttonUploadPhoto = findViewById<ImageView>(R.id.insert_anomaly_uploadPhoto)
            val uploadPhotoMessage = findViewById<TextView>(R.id.insert_anomaly_uploadPhotoMessage)
            buttonUploadPhoto.setImageURI(selectedPhotoURI)
            uploadPhotoMessage.isInvisible = true
        }
    }
}
