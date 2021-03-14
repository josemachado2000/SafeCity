package commov.safecity.roomNotes.dao

import androidx.room.*
import commov.safecity.roomNotes.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    //Create Queries
    @Query("SELECT * FROM note_table ORDER BY title DESC")
    fun getNotesOrderByDateDesc(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

//    @Update
//    suspend fun updateNote(note: Note)

//    @Query("DELETE FROM note_table WHERE id == :id")
//    suspend fun deleteNoteById(id: Int)

    @Query("DELETE FROM note_table")
    suspend fun deleteAllNotes()
}