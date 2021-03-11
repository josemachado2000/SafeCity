package commov.safedrive.roomNotes.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Create Table
@Entity(tableName = "note_table")

class Note {
    //Create Columns
    @PrimaryKey(autoGenerate = true) val id: Int? = null
    @ColumnInfo(name = "title") val title: String = ""
    @ColumnInfo(name = "description") val description: String = ""
}