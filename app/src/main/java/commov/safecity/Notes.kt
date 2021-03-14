package commov.safecity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import commov.safecity.recyclers.adapters.NoteAdapter
import commov.safecity.roomNotes.NotesApplication
import commov.safecity.roomNotes.entities.Note
import commov.safecity.roomNotes.viewModel.NoteViewModel
import commov.safecity.roomNotes.viewModel.NoteViewModelFactory

class Notes : AppCompatActivity() {
    private val newNoteActivityRequestCode = 1
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
    }
//    val resultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result: ActivityResult? ->
//        if(result?.resultCode == Activity.RESULT_OK) {
//            text_
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_notes)
        val adapter = NoteAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        noteViewModel.allNotes.observe(this, { notes ->
            // Update the cached copy of the notes in the adapter.
            notes.let {
                // adapter.setNotes(it)
                adapter.submitList(it)
            }
        })

        val buttonInsertNote = findViewById<Button>(R.id.button_insertNote)
        buttonInsertNote.setOnClickListener {
            val intent = Intent(this@Notes, InsertNote::class.java)
            resultLauncher.launch(intent)
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            data?.getStringExtra(InsertNote.EXTRA_REPLY_TITLE)?.let { title ->
                data.getStringExtra(InsertNote.EXTRA_REPLY_DESC)?.let { desc ->
                val note = Note(title = title, description = desc)
                noteViewModel.insertNote(note)
            }}
        }
    }
}

// DEPRECATED CODE
//    val intent = Intent(this@Notes, InsertNote::class.java)
//    startActivityForResult(intent, newNoteActivityRequestCode)
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
//        super.onActivityResult(requestCode, resultCode, intentData)
//
//        if (requestCode == newNoteActivityRequestCode && resultCode == Activity.RESULT_OK) {
//            intentData?.getStringExtra(InsertNote.EXTRA_REPLY)?.let {
//                val note = Note(title = it, description = it)
//                noteViewModel.insertNote(note)
//            }
//        } else {
//            Toast.makeText(
//                applicationContext,
//                R.string.empty_not_saved,
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
