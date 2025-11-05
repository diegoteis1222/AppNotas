package com.example.notas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
        val btnAdd = findViewById<Button>(R.id.btnAdd)

        notes = loadNotes()
        adapter = NoteAdapter(notes) { position ->
            notes.removeAt(position)
            adapter.notifyItemRemoved(position)
            saveNotes()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAdd.setOnClickListener {
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
