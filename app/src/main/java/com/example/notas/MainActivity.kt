package com.example.notas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        val añadirNotaBoton = findViewById<Button>(R.id.añadirNotaBoton)

        notes = loadNotes()
        adapter = NoteAdapter(notes, { position ->
            // Lógica para borrar
            notes.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(
                position,
                notes.size
            ) // Buena práctica para actualizar posiciones
            saveNotes()
        }, { note ->
            // Lógica para el clic normal (abrir en nueva pantalla)
            // 1. Crear un Intent para ir de MainActivity a NoteDetailActivity
            val intent = Intent(this, NoteDetailActivity::class.java)

            // 2. "Empaquetar" el texto de la nota en el Intent usando una clave y un valor.
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_TEXT, note.text)

            // 3. Iniciar la nueva actividad.
            startActivity(intent)
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        añadirNotaBoton.setOnClickListener {
            val intent = Intent(this, AddNoteActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val noteText = data?.getStringExtra("note")
            if (!noteText.isNullOrEmpty()) {
                notes.add(Note(noteText))
                adapter.notifyItemInserted(notes.size - 1)
                saveNotes()
            }
        }
    }

    private fun saveNotes() {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val editor = prefs.edit()
        val noteTexts = notes.joinToString("||") { it.text }
        editor.putString("lista", noteTexts)
        editor.apply()
    }

    private fun loadNotes(): MutableList<Note> {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val data = prefs.getString("lista", "") ?: ""
        if (data.isEmpty()) return mutableListOf()
        return data.split("||").map { Note(it) }.toMutableList()
    }
}
