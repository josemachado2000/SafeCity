package commov.safecity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val loginSharedPref: SharedPreferences = getSharedPreferences(getString(R.string.login_preference_file), Context.MODE_PRIVATE)
//        return if (loginSharedPref.getBoolean("logged", false)) {
//            val inflater: MenuInflater = menuInflater
//            inflater.inflate(R.menu.non_logged_menu, menu)
//            true
//        } else {
//            val inflater: MenuInflater = menuInflater
//            inflater.inflate(R.menu.logged_menu, menu)
//            true
//        }
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        // Handle item selection
//        return when (item.itemId) {
//            R.id.login -> {
//                val intent = Intent(this@Home, Login::class.java)
//                startActivity(intent)
//                true
//            }
//            R.id.logout -> {
//                true
//            }
//            R.id.notes -> {
//                val intent = Intent(this@Home, Notes::class.java)
//                startActivity(intent)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}