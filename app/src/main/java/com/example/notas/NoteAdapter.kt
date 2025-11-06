package com.example.notas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onLongClick: (Int) -> Unit,
    private val onItemClick: (Note) -> Unit

) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {


    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemNota: TextView = itemView.findViewById(R.id.itemNota)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.itemNota.text = note.text

        // Click largo para borrar
        holder.itemView.setOnLongClickListener {
            onLongClick(position)
            true
        }

        // Click normal para ver
        holder.itemView.setOnClickListener {
            onItemClick(note)
        }
    }

    override fun getItemCount() = notes.size


}