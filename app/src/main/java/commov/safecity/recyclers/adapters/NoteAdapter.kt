package commov.safecity.recyclers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import commov.safecity.R
import commov.safecity.roomNotes.entities.Note
import kotlin.coroutines.coroutineContext

class NoteAdapter(
        private val listener: OnNoteClickListener
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NotesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_note_line, parent, false)

        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val current = getItem(position)
        holder.bindTitle(current.title)
        holder.bindDescription(current.description)
        holder.bindDate(current.date)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val noteItemViewTitle: TextView = itemView.findViewById(R.id.note_title)
        private val noteItemViewDesc: TextView = itemView.findViewById(R.id.note_description)
        private val noteItemViewDate: TextView = itemView.findViewById(R.id.note_date)

        fun bindTitle(title: String?) {
            noteItemViewTitle.text = title?.toUpperCase()
        }
        fun bindDescription(desc: String?) {
            noteItemViewDesc.text = desc
        }
        fun bindDate(date: String?) {
            noteItemViewDate.text = date
        }

        init {
            itemView.findViewById<Button>(R.id.button_deleteNote).setOnClickListener(this)
            itemView.findViewById<Button>(R.id.button_editNote).setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            if(v?.findViewById<Button>(R.id.button_deleteNote)?.isClickable == true) {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener.onNoteDeleteClick(position)
                }
            }
            if(v?.findViewById<Button>(R.id.button_editNote)?.isClickable == true) {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    listener.onNoteEditClick(position)
                }
            }
        }
    }

    interface OnNoteClickListener {
        fun onNoteDeleteClick(position: Int)
        fun onNoteEditClick(position: Int)
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
