package commov.safecity

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.api.EndPoints
import commov.safecity.api.ServiceBuilder
import commov.safecity.api.Type
import it.sauronsoftware.ftp4j.FTPClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class InsertAnomaly : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_anomaly)

        // Types Spinner
        val request = ServiceBuilder.buildService(EndPoints::class.java)
        val call = request.getTypes()

        val types = ArrayList<String>()
        val typesSpinner = findViewById<Spinner>(R.id.insert_anomaly_type_spinner)
        call.enqueue(object : Callback<List<Type>> {
            override fun onResponse(call: Call<List<Type>>, response: Response<List<Type>>) {
                if (response.isSuccessful) {
                    val responseTypes = response.body()!!
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
                Toast.makeText(this@InsertAnomaly, "Failed to load types", Toast.LENGTH_SHORT)
                        .show()
            }
        })

        var typeSelected: String = ""
        typesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Log.i("InsertAnomaly", types[position])
                typeSelected = types[position]
                // Toast.makeText(this@InsertAnomaly, types[position], Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }

        // Insert Anomaly
        val textInputLayoutTitle = findViewById<TextInputLayout>(R.id.insert_anomaly_title_textInputLayout)
        val textInputLayoutDesc = findViewById<TextInputLayout>(R.id.insert_anomaly_description_textInputLayout)
        val buttonUploadPhoto = findViewById<Button>(R.id.insert_anomaly_photo_button)
        val buttonSaveAnomaly = findViewById<Button>(R.id.insert_anomaly_saveAnomaly_button)

        buttonUploadPhoto.setOnClickListener {
            Toast.makeText(this@InsertAnomaly, "clicked photo", Toast.LENGTH_SHORT).show()
            //val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncherFileChooser.launch(Intent.createChooser(intent, "Select a file"))
        }
        
        buttonSaveAnomaly.setOnClickListener {
            // Input Validations
            val title = textInputLayoutTitle.editText?.text.toString().trim()
            var validTitle = false
            when {
                title.isEmpty() -> {
                    textInputLayoutTitle.error = getString(R.string.insert_note_ObligationError)
                }
                title.length > 50 -> {
                    textInputLayoutTitle.error = getString(R.string.insert_note_titleLengthError)
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
                    textInputLayoutDesc.error = getString(R.string.insert_note_ObligationError)
                }
                description.length > 160 -> {
                    textInputLayoutDesc.error = getString(R.string.insert_note_descLengthError)
                }
                else -> {
                    textInputLayoutDesc.error = ""
                    validDesc = true
                }
            }

            if (validTitle && validDesc) {
                Log.i("InsertAnomaly", "$title $description $typeSelected")

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var resultLauncherFileChooser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
//            arrayOf(
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (ContextCompat.checkSelfPermission(this@InsertAnomaly,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@InsertAnomaly, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 101)
            } else {
                val selectedPhoto: Uri = result?.data?.data!!
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor: Cursor = contentResolver.query(selectedPhoto, filePathColumn, null, null, null)!!
                cursor.moveToFirst()
                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                val picturePath: String = cursor.getString(columnIndex)
                cursor.close()

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

                        mFtpClient.upload(File(picturePath))

                        mFtpClient.disconnect(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.i("FTP", "Failed to upload image via FTP")
                    }
                }
            }
        }
    }
}