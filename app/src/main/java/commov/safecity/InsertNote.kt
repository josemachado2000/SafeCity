package commov.safecity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout

class InsertNote : AppCompatActivity() {
    private val ADD_NOTE = "ADD_NOTE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.insert_note)

        val textInputLayoutTitle = findViewById<TextInputLayout>(R.id.insert_note_noteTitle_textInputLayout)
        val textInputLayoutDesc = findViewById<TextInputLayout>(R.id.insert_note_noteDesc_textInputLayout)

        val buttonSaveNote = findViewById<Button>(R.id.insert_anomaly_saveAnomaly_button)
        buttonSaveNote.setOnClickListener {
            val replyIntent = Intent()

            // Input Validations
            val title = textInputLayoutTitle.editText?.text.toString().trim()
            var validTitle = false
            when {
                title.isEmpty() -> {
                    textInputLayoutTitle.error = getString(R.string.insert_note_ObligationError)
                }
                title.length > 50 -> {
                    textInputLayoutTitle.error = getString(R.string.insert_note_titleLengthError)
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
                    textInputLayoutDesc.error = getString(R.string.insert_note_ObligationError)
                }
                description.length > 160 -> {
                    textInputLayoutDesc.error = getString(R.string.insert_note_descLengthError)
                }
                else -> {
                    textInputLayoutDesc.error = ""
                    validDesc = true
                }
            }

            if (validTitle && validDesc) {
                val noteTitle = textInputLayoutTitle.editText?.text.toString()
                val noteDesc = textInputLayoutDesc.editText?.text.toString()

                Log.i(ADD_NOTE, "----- InsertNote.kt -------------------------------\n")
                Log.i(ADD_NOTE, "Title: $noteTitle")
                Log.i(ADD_NOTE, "Desc: $noteDesc")

                replyIntent.putExtra(EXTRA_REPLY_TITLE, noteTitle)
                replyIntent.putExtra(EXTRA_REPLY_DESC, noteDesc)
                setResult(Activity.RESULT_OK, replyIntent)
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val textInputLayoutTitle: TextInputLayout = findViewById(R.id.insert_note_noteTitle_textInputLayout)
        val textInputLayoutDesc: TextInputLayout = findViewById(R.id.insert_note_noteDesc_textInputLayout)
        outState.putString("INSERT_TITLE", textInputLayoutTitle.editText?.text.toString())
        outState.putString("INSERT_DESC", textInputLayoutDesc.editText?.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val textInputLayoutTitle: TextInputLayout = findViewById(R.id.insert_note_noteTitle_textInputLayout)
        val textInputLayoutDesc: TextInputLayout = findViewById(R.id.insert_note_noteDesc_textInputLayout)
        textInputLayoutTitle.editText?.setText(savedInstanceState.getString("INSERT_TITLE"))
        textInputLayoutDesc.editText?.setText(savedInstanceState.getString("INSERT_DESC"))
    }

    companion object {
        const val EXTRA_REPLY_TITLE = "com.example.android.noteTitle.REPLY"
        const val EXTRA_REPLY_DESC = "com.example.android.noteDesc.REPLY"
    }
}