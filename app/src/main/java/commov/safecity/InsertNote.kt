package commov.safecity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import commov.safecity.roomNotes.entities.Note
import java.io.Serializable

class InsertNote : AppCompatActivity() {
    private lateinit var editNoteViewTitle: EditText
    private lateinit var editNoteViewDesc: EditText
    private val TAG = "addNote"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_note)

        editNoteViewTitle = findViewById(R.id.insert_note_noteTitle_editText)
        editNoteViewDesc = findViewById(R.id.insert_note_noteDesc_editText)

        val buttonSaveNote = findViewById<Button>(R.id.button_saveNote)
        buttonSaveNote.setOnClickListener {
            val replyIntent = Intent()
            if(TextUtils.isEmpty(editNoteViewTitle.text) && TextUtils.isEmpty(editNoteViewDesc.text)) {
                setResult(Activity.RESULT_CANCELED, replyIntent)
            } else {
                val noteTitle = editNoteViewTitle.text.toString()
                val noteDesc = editNoteViewDesc.text.toString()

                Log.i(TAG, "Title: $noteTitle")
                Log.i(TAG, "Desc: $noteDesc")

                replyIntent.putExtra(EXTRA_REPLY_TITLE, noteTitle)
                replyIntent.putExtra(EXTRA_REPLY_DESC, noteDesc)
                setResult(Activity.RESULT_OK, replyIntent)
            }
            finish()
        }
    }

    companion object {
        const val EXTRA_REPLY_TITLE = "com.example.android.noteTitle.REPLY"
        const val EXTRA_REPLY_DESC = "com.example.android.noteDesc.REPLY"
    }
}