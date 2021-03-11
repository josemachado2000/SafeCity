package commov.safedrive.roomNotes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import commov.safedrive.roomNotes.dao.NoteDao
import commov.safedrive.roomNotes.entities.Note

// Annotates class to be a Room Database with a table (entity) of the Note class
@Database(entities = [Note::class], version = 1, exportSchema = false)
public abstract class NoteDB : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: NoteDB? = null

        fun getDatabase(context: Context): NoteDB {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDB::class.java,
                    "note_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}