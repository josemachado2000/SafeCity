package commov.safecity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import commov.safecity.roomNotes.NotesApplication
import commov.safecity.roomNotes.viewModel.NoteViewModel
import commov.safecity.roomNotes.viewModel.NoteViewModelFactory
import org.w3c.dom.Text

class EditNote : AppCompatActivity() {
    private val EDIT_NOTE = "EDIT_NOTE"

    private val noteViewModel: NoteViewModel by viewModels {
        NoteViewModelFactory((application as NotesApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_note)

        val position = intent.getIntExtra("EXTRA_POSITION", 0)

        val textInputLayoutTitle = findViewById<TextInputLayout>(R.id.edit_note_noteTitle_textInputLayout)
        val textInputLayoutDesc = findViewById<TextInputLayout>(R.id.edit_note_noteDesc_textInputLayout)

        noteViewModel.allNotes.observe(this, { notes ->
            // Update the cached copy of the notes in the adapter.
            textInputLayoutTitle.editText?.text = SpannableStringBuilder(notes[position].title)
            textInputLayoutDesc.editText?.text = SpannableStringBuilder(notes[position].description)
        })

        val buttonSaveEditNote = findViewById<Button>(R.id.button_saveEditNote)
        buttonSaveEditNote.setOnClickListener {
            val replyIntent = Intent()
            // Input Validations
            val title = textInputLayoutTitle.editText?.text.toString().trim()
            var validTitle = false
            when {
                title.isEmpty() -> {
                    textInputLayoutTitle.error = getString(R.string.edit_note_ObligationError)
                }
                title.length > 50 -> {
                    textInputLayoutTitle.error = getString(R.string.edit_note_titleLengthError)
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
                    textInputLayoutDesc.error = getString(R.string.edit_note_ObligationError)
                }
                description.length > 160 -> {
                    textInputLayoutDesc.error = getString(R.string.edit_note_descLengthError)
                }
                else -> {
                    textInputLayoutDesc.error = ""
                    validDesc = true
                }
            }

            if (validTitle && validDesc) {
                val noteTitle = textInputLayoutTitle.editText?.text.toString()
                val noteDesc = textInputLayoutDesc.editText?.text.toString()

                Log.i(EDIT_NOTE, "----- EditNote.kt -------------------------------")
                Log.i(EDIT_NOTE, "Note Position: $position")
                Log.i(EDIT_NOTE, "New Title: $noteTitle")
                Log.i(EDIT_NOTE, "New Desc: $noteDesc")

                replyIntent.putExtra(EXTRA_REPLY_NEW_TITLE, noteTitle)
                replyIntent.putExtra(EXTRA_REPLY_NEW_DESC, noteDesc)
                replyIntent.putExtra(EXTRA_REPLY_RETURN_POSITION, position)
                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    companion object {
        const val EXTRA_REPLY_NEW_TITLE = "com.example.android.newNoteTitle.REPLY"
        const val EXTRA_REPLY_NEW_DESC = "com.example.android.newNoteDesc.REPLY"
        const val EXTRA_REPLY_RETURN_POSITION = "com.example.android.returnNotePosition.REPLY"
    }
}

