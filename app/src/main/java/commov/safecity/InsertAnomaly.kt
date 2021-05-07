package commov.safecity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.api.*
import it.sauronsoftware.ftp4j.FTPClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class InsertAnomaly : AppCompatActivity() {
    private var photoPath: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_anomaly)

        val currentLocation = intent?.getParcelableExtra<LatLng>("currentLocation")!!
        Log.i("InsertAnomaly", currentLocation.toString())

        // Types Spinner
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getTypes()

        val types = ArrayList<String>()
        var responseTypes = listOf<Type>()
        val typesSpinner = findViewById<Spinner>(R.id.insert_anomaly_type_spinner)
        types.add(0, getString(R.string.insert_anomaly_spinnerDefaultValue))
        call.enqueue(object : Callback<List<Type>> {
            override fun onResponse(call: Call<List<Type>>, response: Response<List<Type>>) {
                if (response.isSuccessful) {
                    responseTypes = response.body()
                    for (T in responseTypes) {
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
                Toast.makeText(this@InsertAnomaly, "Failed to load types", Toast.LENGTH_SHORT).show()
            }
        })

        val typeSelected = object {
            var type_id = 0
            var type = ""
        }

        val typeEmptyError = findViewById<TextView>(R.id.insert_anomaly_emptySpinnerMessage)
        typesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                typeSelected.type = types[position]
                for (T in responseTypes) {
                    if(T.type == types[position]) {
                        typeSelected.type_id = T.id
                        // Log.i("Insert", "${T.type} ${types[position]} ${typeSelected.type_id} ${T.id}")
                    }

                }
                typeEmptyError.isInvisible = true
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.i("InsertAnomaly", getString(R.string.insert_anomaly_emptySpinnerMessage))
            }
        }

        // Insert Anomaly
        val textInputLayoutTitle = findViewById<TextInputLayout>(R.id.insert_anomaly_title_textInputLayout)
        val textInputLayoutDesc = findViewById<TextInputLayout>(R.id.insert_anomaly_description_textInputLayout)
        val buttonUploadPhoto = findViewById<ImageView>(R.id.insert_anomaly_uploadPhoto)
        val buttonSaveAnomaly = findViewById<Button>(R.id.insert_anomaly_saveAnomaly_button)

        buttonUploadPhoto.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                resultLauncherFileChooser.launch(Intent.createChooser(intent, getString(R.string.insert_anomaly_fileChooserMessage)))
            }
        }

        buttonSaveAnomaly.setOnClickListener {
            // Input Validations
            val title = textInputLayoutTitle.editText?.text.toString().trim()
            var validTitle = false
            when {
                title.isEmpty() -> {
                    textInputLayoutTitle.error = getString(R.string.insert_anomaly_ObligationError)
                }
                title.length > 50 -> {
                    textInputLayoutTitle.error = getString(R.string.insert_anomaly_titleLengthError)
                }
                else -> {
                    textInputLayoutTitle.error = ""
                    validTitle = true
                }
            }

            val description = textInputLayoutDesc.editText?.text.toString().trim()
            var validDesc = false
            when {
                description.isEmpty() -> {
                    textInputLayoutDesc.error = getString(R.string.insert_anomaly_ObligationError)
                }
                description.length > 160 -> {
                    textInputLayoutDesc.error = getString(R.string.insert_anomaly_descLengthError)
                }
                else -> {
                    textInputLayoutDesc.error = ""
                    validDesc = true
                }
            }

            if (typeSelected.type !== types[0] && validTitle && validDesc) {
                val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), MODE_PRIVATE)

                Log.i("InsertAnomaly", "$title $description ${currentLocation.latitude} ${currentLocation.longitude}" +
                    "$photoPath ${loginSharedPref.getInt("loggedUserID", 0)} ${typeSelected.type_id} ${typeSelected.type}")

                val photo = File(photoPath)
                val requestPostAnomaly = ServiceBuilder.buildService(EndPoints::class.java)
                val callPostAnomaly = requestPostAnomaly.insertAnomaly(
                    title = title,
                    description = description,
                    lat = currentLocation.latitude,
                    lng = currentLocation.longitude,
                    photo = photo.name,
                    userID = loginSharedPref.getInt("loggedUserID", 0),
                    typeID = typeSelected.type_id
                )

                Log.i("InsertAnomaly", callPostAnomaly.request().toString())
                callPostAnomaly.enqueue(object : Callback<Anomaly> {
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
                            val intent = Intent(this@InsertAnomaly, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<Anomaly>, t: Throwable) {
                        Log.i("InsertAnomaly", "Failed to create anomaly")
                    }
                })
            } else {
                when {
                    typeSelected.type === types[0] -> {
                        typeEmptyError.isVisible = true
                    }
                    // !photo.exists() -> {
                    //     Log.i("InsertAnomaly", "Introduza uma foto")
                    // }
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