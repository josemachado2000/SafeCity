package commov.safecity.roomNotes.db

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import commov.safecity.roomNotes.dao.NoteDao
import commov.safecity.roomNotes.entities.Note
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class NoteRepository(private val noteDao: NoteDao) {

    // Room executes all queries on a separate thread.
    val allNotes: Flow<List<Note>> = noteDao.getNotesOrderByDateDesc()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }
    suspend fun deleteNote(note: Note) {
        note.id?.let { noteDao.deleteNoteById(it) }
    }
    suspend fun deleteAllNotes() {
        noteDao.deleteAllNotes()
    }
}