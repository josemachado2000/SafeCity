package commov.safecity

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        val usernameEditText = findViewById<EditText>(R.id.login_username_editText)
        val passwordEditText = findViewById<EditText>(R.id.login_password_editText)

        val buttonLogin = findViewById<Button>(R.id.login_buttonLogin)
        buttonLogin.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            val request = ServiceBuilder.buildService(EndPoints::class.java)
            val call = request.login(username = username, password = password)

            call.enqueue(object: Callback<LoginPostResponse> {
                override fun onResponse(call: Call<LoginPostResponse>, response: Response<LoginPostResponse>) {
                    if (response.isSuccessful) {
                        val user: LoginPostResponse = response.body()!!
                        Toast.makeText(this@Login, user.id.toString().plus(" -> NICE"), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginPostResponse>, t: Throwable) {
                    Toast.makeText(this@Login, "${t.message}", Toast.LENGTH_LONG).show()
                }
            })

        }
    }
}