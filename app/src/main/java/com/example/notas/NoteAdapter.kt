package com.example.notas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onItemClick: (Note, Int) -> Unit,
    private val onLongClick: (Int) -> Unit


) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    // Lista para guardar las posiciones de los elementos seleccionados
    private val selectedItems = mutableSetOf<Int>()
    var selectionMode = false
        private set

    // Devuelve los elementos seleccionados
    fun getSelectedNotes(): List<Note> {
        return selectedItems.map { notes[it] }
    }

    // Limpia la selección y desactiva el modo de selección
    fun clearSelection() {
        selectedItems.clear()
        selectionMode = false
        notifyDataSetChanged() // Notifica para redibujar la lista
    }

    // Alterna la selección de un elemento
    fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
    }

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

        // Cambia el fondo si el elemento está seleccionado
        if (selectedItems.contains(position)) {
            holder.itemView.setBackgroundColor(Color.GRAY) // Color para item seleccionado
        } else {
            holder.itemView.setBackgroundColor(Color.DKGRAY) // Color por defecto
        }

        // Click largo para borrar
        holder.itemView.setOnLongClickListener {
            onLongClick(position)
            true
        }

        // Click normal para ver
        holder.itemView.setOnClickListener {
            onItemClick(note, position)
        }
    }

    override fun getItemCount() = notes.size

    // Activa el modo selección
    fun startSelectionMode(position: Int) {
        selectionMode = true
        toggleSelection(position)
    }
}