package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>

    // manejar el resultado de AddNoteActivity
    private val addNoteLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val noteTitle = data?.getStringExtra("note_title")
                val noteDescription = data?.getStringExtra("note_description")

                if (!noteTitle.isNullOrEmpty() && noteDescription != null) {
                    notes.add(Note(noteTitle, noteDescription))
                    adapter.notifyItemInserted(notes.size - 1)
                    saveNotes()
                }
            }
        }

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
            adapter.notifyItemRangeChanged(position, notes.size)
            saveNotes()
        }, { note ->
            // Lógica para ver la nota con sus detalles
            val intent = Intent(this, NoteDetailActivity::class.java).apply {
                putExtra(NoteDetailActivity.EXTRA_NOTE_TITULO, note.text)
                putExtra(NoteDetailActivity.EXTRA_NOTE_DESCIPCION, note.description)
            }
            startActivity(intent)
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        añadirNotaBoton.setOnClickListener {
            // Usamos el nuevo launcher
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }
    }

    private fun saveNotes() {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val editor = prefs.edit()
        // Guardamos título y descripción separados por ":::" y las notas por "|||"
        val noteStrings = notes.joinToString("|||") { "${it.text}:::${it.description}" }
        editor.putString("lista", noteStrings)
        editor.apply()
    }

    private fun loadNotes(): MutableList<Note> {
        val prefs = getSharedPreferences("notas", MODE_PRIVATE)
        val data = prefs.getString("lista", null) ?: return mutableListOf()
        if (data.isEmpty()) return mutableListOf()

        return data.split("|||").mapNotNull { noteString ->
            val parts = noteString.split(":::")
            if (parts.size == 2) {
                Note(text = parts[0], description = parts[1])
            } else {
                null // Ignora las notas con formato incorrecto
            }
        }.toMutableList()
    }
}