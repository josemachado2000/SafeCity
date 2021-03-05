package commov.safedrive

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.welcome)

        Handler(Looper.getMainLooper()).postDelayed({
            var intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}