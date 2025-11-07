package com.example.notas

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NoteAdapter
    private lateinit var notes: MutableList<Note>
    private lateinit var botonBorrarSeleccionados: Button
    private lateinit var añadirNotaBoton: Button
    private lateinit var onBackPressedCallback: OnBackPressedCallback

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

        onBackPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                // Esta es la lógica que se ejecutará cuando el callback esté habilitado
                // y el usuario presione "Atrás".
                exitSelectionMode()
            }
        }
        // 2. Añadir el callback al dispatcher.
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        recyclerView = findViewById(R.id.recyclerView)
        añadirNotaBoton = findViewById(R.id.añadirNotaBoton)
        botonBorrarSeleccionados = findViewById(R.id.botonBorrarSeleccionados)

        notes = loadNotes()
        adapter = NoteAdapter(notes, { note, position ->
            // Si estamos en modo de selección, el clic normal alterna la selección
            if (adapter.selectionMode) {
                adapter.toggleSelection(position)
                // Oculta el botón de borrar si no queda nada seleccionado
                if (adapter.getSelectedNotes().isEmpty()) {
                    exitSelectionMode()
                }
            } else {
                // Lógica para ver la nota con sus detalles (clic normal)
                val intent = Intent(this, NoteDetailActivity::class.java).apply {
                    putExtra(NoteDetailActivity.EXTRA_NOTE_TITULO, note.text)
                    putExtra(NoteDetailActivity.EXTRA_NOTE_DESCIPCION, note.description)
                }
                startActivity(intent)
            }
        }, { position ->
            // El clic largo inicia el modo de selección
            if (!adapter.selectionMode) {
                adapter.startSelectionMode(position)
                enterSelectionMode()
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        añadirNotaBoton.setOnClickListener {
            // Usamos el nuevo launcher
            val intent = Intent(this, AddNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        botonBorrarSeleccionados.setOnClickListener {
            // Lógica para borrar las notas seleccionadas
            val selectedNotes = adapter.getSelectedNotes()
            notes.removeAll(selectedNotes.toSet())
            saveNotes()
            exitSelectionMode()
            // Se usa notifyDataSetChanged porque pueden ser múltiples borrados
            adapter.notifyDataSetChanged()
        }
    }

    private fun enterSelectionMode() {
        botonBorrarSeleccionados.visibility = View.VISIBLE
        añadirNotaBoton.visibility = View.GONE
        onBackPressedCallback.isEnabled = true
    }

    private fun exitSelectionMode() {
        adapter.clearSelection()
        botonBorrarSeleccionados.visibility = View.GONE
        añadirNotaBoton.visibility = View.VISIBLE
        onBackPressedCallback.isEnabled = false
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