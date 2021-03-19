package commov.safecity

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import commov.safecity.recyclers.adapters.NoteAdapter
import commov.safecity.roomNotes.NotesApplication
import commov.safecity.roomNotes.entities.Note
import commov.safecity.roomNotes.viewModel.NoteViewModel
import commov.safecity.roomNotes.viewModel.NoteViewModelFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Notes : AppCompatActivity(), NoteAdapter.OnNoteClickListener {
    private val ADD_NOTE = "ADD_NOTE"
    private val EDIT_NOTE = "EDIT_NOTE"
    private val DELETE_NOTE = "DELETE_NOTE"
    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_notes)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_notes)
        val adapter = NoteAdapter(this)
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

                Log.i(DELETE_NOTE, "----- Notes.kt ----------------------------------")
                Log.i(DELETE_NOTE, "${noteViewModel.allNotes.value}")
                Log.i(DELETE_NOTE, "${noteViewModel.allNotes.value?.size}")
                if(noteViewModel.allNotes.value?.size!! <= 0) {
                    val textViewEmptyNotes = findViewById<TextView>(R.id.recycler_notes_emptyNotes)
                    val textViewDeleteAllNotes = findViewById<TextView>(R.id.recycler_notes_deleteAllNotes)
                    textViewEmptyNotes.isVisible = true
                    textViewDeleteAllNotes.isInvisible = true
                } else {
                    val textViewEmptyNotes = findViewById<TextView>(R.id.recycler_notes_emptyNotes)
                    val textViewDeleteAllNotes = findViewById<TextView>(R.id.recycler_notes_deleteAllNotes)
                    textViewEmptyNotes.isInvisible = true
                    textViewDeleteAllNotes.isVisible = true
                }
            }
        })

        val fabInsertNote = findViewById<FloatingActionButton>(R.id.floatingActionButton_insertNote)
        fabInsertNote.setOnClickListener {
            val intent = Intent(this@Notes, InsertNote::class.java)
            resultLauncherInsertNote.launch(intent)
        }

        val textViewDeleteAllNotes = findViewById<TextView>(R.id.recycler_notes_deleteAllNotes)
        textViewDeleteAllNotes.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(R.string.notes_delete_popup_title)
                setMessage(R.string.notes_delete_popup_message)
                setPositiveButton(R.string.notes_delete_popup_yes
                ) { dialog, _ ->
                    // User clicked Yes button
                    noteViewModel.deleteAllNotes()
                    dialog.dismiss()
                }
                setNegativeButton(R.string.notes_delete_popup_cancel
                ) { dialog, _ ->
                    // User cancelled the dialog
                    dialog.dismiss()
                }
            }
            // Create the AlertDialog
            builder.create()
            builder.show()
        }
    }

    override fun onNoteDeleteClick(position: Int) {
        Log.i(ADD_NOTE, "----- Notes.kt ----------------------------------")
        Log.i(DELETE_NOTE, "Note Delete Position: $position")
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(getString(R.string.note_delete_popup_title).plus(" ").plus("${noteViewModel.allNotes.value?.get(position)?.title}?"))
            setMessage(R.string.note_delete_popup_message)
            setPositiveButton(R.string.note_delete_popup_yes
            ) { dialog, _ ->
                // User clicked Yes button
                noteViewModel.allNotes.value?.get(position)?.let { noteViewModel.deleteNote(it) }
                dialog.dismiss()
            }
            setNegativeButton(R.string.note_delete_popup_cancel
            ) { dialog, _ ->
                // User cancelled the dialog
                dialog.dismiss()
            }
        }
        // Create the AlertDialog
        builder.create()
        builder.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNoteEditClick(position: Int) {
        val intent = Intent(this@Notes, EditNote::class.java).apply { putExtra("EXTRA_POSITION", position) }
        resultLauncherEdit.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var resultLauncherInsertNote = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            data?.getStringExtra(InsertNote.EXTRA_REPLY_TITLE)?.let { title ->
                data.getStringExtra(InsertNote.EXTRA_REPLY_DESC)?.let { desc ->
                    val date = LocalDateTime.now()
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    val dateFormatted = date.format(dateFormatter)

                    Log.i(ADD_NOTE, "----- Notes.kt ----------------------------------")
                    Log.i(ADD_NOTE, "Title: $title")
                    Log.i(ADD_NOTE, "Desc: $desc")
                    Log.i(ADD_NOTE, "Date: $date")

                    val note = Note(title = title, description = desc, date = dateFormatted)
                    noteViewModel.insertNote(note)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private var resultLauncherEdit = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            data?.getStringExtra(EditNote.EXTRA_REPLY_NEW_TITLE)?.let { title ->
                data.getStringExtra(EditNote.EXTRA_REPLY_NEW_DESC)?.let { desc ->
                    data.getIntExtra(EditNote.EXTRA_REPLY_RETURN_POSITION, 0).let { position ->
                        val date = LocalDateTime.now()
                        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val dateFormatted = date.format(dateFormatter)

                        Log.i(EDIT_NOTE, "----- Notes.kt ----------------------------------")
                        Log.i(EDIT_NOTE, "New Title: $title")
                        Log.i(EDIT_NOTE, "New Desc: $desc")
                        Log.i(EDIT_NOTE, "New Date: $date")

                        Toast.makeText(this, "Note $position clicked", Toast.LENGTH_SHORT).show()
                        val note = Note(id = noteViewModel.allNotes.value?.get(position)?.id, title = title, description = desc, date = dateFormatted)
                        noteViewModel.updateNote(note)
                    }
                }
            }
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
