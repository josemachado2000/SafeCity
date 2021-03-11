package commov.safedrive.roomNotes.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import commov.safedrive.roomNotes.entities.Note

@Dao
interface NoteDao {
    //Create Queries
    @Query("SELECT * FROM note_table")
    fun getNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM note_table WHERE id == :id")
    suspend fun deleteNoteById(id: Int)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()
}