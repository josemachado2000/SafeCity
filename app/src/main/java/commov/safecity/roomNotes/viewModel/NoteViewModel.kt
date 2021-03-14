package commov.safecity.roomNotes.viewModel

import android.app.Application
import androidx.lifecycle.*
import commov.safecity.roomNotes.db.NoteDB
import commov.safecity.roomNotes.db.NoteRepository
import commov.safecity.roomNotes.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//class NoteViewModel(application: Application) : AndroidViewModel(application) {
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allNotes: LiveData<List<Note>> = repository.allNotes.asLiveData()

//    init {
//        val notesDao = NoteDB.getDatabase(application, viewModelScope).noteDao()
//        repository = NoteRepository(notesDao)
//        allNotes = repository.allNotes
//    }
    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
