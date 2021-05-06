package commov.safecity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.wajahatkarim3.easyvalidation.core.view_ktx.nonEmpty
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
        val loginFailedError = findViewById<TextView>(R.id.login_failedLoginError)
        loginFailedError.isInvisible = true

        val buttonLogin = findViewById<Button>(R.id.login_buttonLogin)
        buttonLogin.setOnClickListener {
            // Login Validations
            val username = usernameTextInputLayout.editText?.text.toString().trim()
            var validUsername = false
            when {
                username.isEmpty() -> {
                    usernameTextInputLayout.error = getString(R.string.login_username_ObligationError)
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
                    passwordTextInputLayout.error = getString(R.string.login_password_ObligationError)
                }
                else -> {
                    passwordTextInputLayout.error = ""
                    validPassword = true
                }
            }

            if (validUsername && validPassword) {
                val request = ServiceBuilder.buildService(EndPoints::class.java)
                val call = request.login(username = username, password = password)

                call.enqueue(object : Callback<LoginPostResponse> {
                    override fun onResponse(call: Call<LoginPostResponse>, response: Response<LoginPostResponse>) {
                        if (response.isSuccessful) {
                            loginFailedError.isInvisible = true
                            val user: LoginPostResponse = response.body()!!
//                            Toast.makeText(this@Login, user.id.toString().plus("   ->   ").plus(user.username), Toast.LENGTH_SHORT).show()

                            val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
                            with(loginSharedPref.edit()) {
                                putBoolean(getString(R.string.logged), true)
                                putString(getString(R.string.loggedUsername), username)
                                putInt(getString(R.string.loggedUserID), user.id)
                                apply()
                            }

                            val intent = Intent(this@Login, Home::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onFailure(call: Call<LoginPostResponse>, t: Throwable) {
                        loginFailedError.isVisible = true
                    }
                })
            }
        }
    }

    // Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.non_logged_menu, menu)
        val loginItem = menu.findItem(R.id.login)
        loginItem.isVisible = false
        val anomaliesItem = menu.findItem(R.id.anomaliesMap)
        anomaliesItem.isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.anomaliesMap -> {
                val intent = Intent(this@Login, Home::class.java)
                startActivity(intent)
                true
            }
            R.id.notes -> {
                val intent = Intent(this@Login, Notes::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val loginFailedError: TextView = findViewById(R.id.login_failedLoginError)
        val textInputLayoutUsername: TextInputLayout = findViewById(R.id.login_username_TextInputLayout)
        val textInputLayoutPassword: TextInputLayout = findViewById(R.id.login_password_TextInputLayout)
        outState.putInt("LOGIN_FAILED_ERROR_VISIBILITY", loginFailedError.visibility)
        outState.putString("LOGIN_USERNAME", textInputLayoutUsername.editText?.text.toString())
        outState.putString("LOGIN_PASSWORD", textInputLayoutPassword.editText?.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val loginFailedError: TextView = findViewById(R.id.login_failedLoginError)
        val textInputLayoutUsername: TextInputLayout = findViewById(R.id.login_username_TextInputLayout)
        val textInputLayoutPassword: TextInputLayout = findViewById(R.id.login_password_TextInputLayout)
//        Toast.makeText(this, loginFailedError.visibility.toString(), Toast.LENGTH_SHORT).show()
        if(loginFailedError.visibility == 4 && savedInstanceState.getString("LOGIN_USERNAME")?.nonEmpty() == true &&
                savedInstanceState.getString("LOGIN_PASSWORD")?.nonEmpty() == true) { loginFailedError.isVisible = true }
        textInputLayoutUsername.editText?.setText(savedInstanceState.getString("LOGIN_USERNAME"))
        textInputLayoutPassword.editText?.setText(savedInstanceState.getString("LOGIN_PASSWORD"))
    }
}