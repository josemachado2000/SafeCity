package commov.safecity.recyclers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import commov.safecity.R
import commov.safecity.roomNotes.entities.Note

class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NotesComparator()) {
    private var notes = emptyList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = getItem(position)
        holder.bindTitle(current.title)
        holder.bindDescription(current.description)
    }

    internal fun setNotes(notes: List<Note>) {
        this.notes = notes
        notifyDataSetChanged()
    }
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val noteItemViewTitle: TextView = itemView.findViewById(R.id.note_title)
        private val noteItemViewDesc: TextView = itemView.findViewById(R.id.note_description)

        fun bindTitle(title: String?) {
            noteItemViewTitle.text = title
        }
        fun bindDescription(desc: String?) {
            noteItemViewDesc.text = desc
        }

        companion object {
            fun create(parent: ViewGroup): NoteViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_note_line, parent, false)
                return NoteViewHolder(view)
            }
        }
    }

    class NotesComparator : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }
    }
}