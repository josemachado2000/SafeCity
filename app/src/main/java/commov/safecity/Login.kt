package commov.safecity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.api.EndPoints
import commov.safecity.api.LoginPostResponse
import commov.safecity.api.ServiceBuilder
import retrofit2.Call
import retrofit2.Callback

import retrofit2.Response

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val usernameTextInputLayout = findViewById<TextInputLayout>(R.id.login_username_TextInputLayout)
        val passwordTextInputLayout = findViewById<TextInputLayout>(R.id.login_password_TextInputLayout)

        val buttonLogin = findViewById<Button>(R.id.login_buttonLogin)
        buttonLogin.setOnClickListener {
            // Login Validations
            val username = usernameTextInputLayout.editText?.text.toString().trim()
            var validUsername = false
            when {
                username.isEmpty() -> {
                    usernameTextInputLayout.error = getString(R.string.insert_note_ObligationError)
                }
                else -> {
                    usernameTextInputLayout.error = ""
                    validUsername = true
                }
            }

            val password = passwordTextInputLayout.editText?.text.toString().trim()
            var validPassword = false
            when {
                password.isEmpty() -> {
                    passwordTextInputLayout.error = getString(R.string.insert_note_ObligationError)
                }
                else -> {
                    passwordTextInputLayout.error = ""
                    validPassword = true
                }
            }

            if (validUsername && validPassword) {
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.login(username = username, password = password)

                call.enqueue(object: Callback<LoginPostResponse> {
                    override fun onResponse(call: Call<LoginPostResponse>, response: Response<LoginPostResponse>) {
                        if (response.isSuccessful) {
                            val user: LoginPostResponse = response.body()!!
                            Toast.makeText(this@Login, user.id.toString().plus(" -> NICE"), Toast.LENGTH_SHORT).show()

                            val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
                            with (loginSharedPref.edit()) {
                                putBoolean(getString(R.string.logged), true)
                                putString(getString(R.string.loggedUsername), username)
                                apply()
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginPostResponse>, t: Throwable) {
                        Toast.makeText(this@Login, "${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }


        }
    }
}