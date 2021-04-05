package commov.safecity

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
            val intent = Intent(this, Notes::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}