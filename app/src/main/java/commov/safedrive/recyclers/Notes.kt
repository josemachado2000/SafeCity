package commov.safedrive.recyclers

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import commov.safedrive.R
import commov.safedrive.recyclers.adapters.NoteAdapter

class Notes  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_notes)
        val adapter = NoteAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}